import os
import time
import torch
from datetime import datetime
from safetensors.torch import load_file
from optimum.quanto import quantize, qfloat8, freeze
from diffusers import FluxPipeline, FlowMatchEulerDiscreteScheduler, AutoencoderKL
from diffusers.models.transformers.transformer_flux import FluxTransformer2DModel
from transformers import (
    CLIPTextModel,
    CLIPTokenizer,
    T5EncoderModel,
    T5TokenizerFast,
)
from modelscope import snapshot_download
from translator import QwenTranslator
from fp8checker import check_fp8_support, print_support_report
import json
import logging

# 配置日志系统
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(message)s",
    handlers=[logging.StreamHandler()]
)

logger = logging.getLogger("FluxPipeline")

# 设置环境变量
os.environ["PYTORCH_CUDA_ALLOC_CONF"] = "expandable_segments:True"
os.environ['CUDA_LAUNCH_BLOCKING'] = "1"
os.environ["TORCH_CUDA_ARCH_LIST"] = "8.6"

torch.set_default_device("cpu")  # 所有模型默认加载到 CPU
device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
dtype = torch.bfloat16

# 检查 FP8 支持
checker = check_fp8_support()
print_support_report(checker)
HARDWARE_FP8_SUPPORTED = checker['fp8_supported']
logger.info(f"硬件FP8支持: {'✅' if HARDWARE_FP8_SUPPORTED else '❌'}")

def print_gpu_memory(step):
    """打印当前 GPU 显存使用情况"""
    if torch.cuda.is_available():
        allocated = torch.cuda.memory_allocated(device=0) / 1024 ** 3
        cached = torch.cuda.memory_reserved(device=0) / 1024 ** 3
        formatted_now = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
        logger.info(f"{formatted_now} {step} ===> 已分配: {allocated:.2f} GB | 保留缓存: {cached:.2f} GB")

def measure_time(func):
    """装饰器：用于测量函数执行时间"""
    def wrapper(*args, **kwargs):
        start_time = time.time()
        logger.info(f"[开始] 正在执行 {func.__name__}")
        result = func(*args, **kwargs)
        end_time = time.time()
        logger.info(f"[完成] {func.__name__} 耗时 {end_time - start_time:.2f} 秒")
        return result
    return wrapper

@measure_time
def load_translator():
    logger.info("🧠 加载翻译模型...")
    translator = QwenTranslator()
    logger.info("🧠 翻译模型加载完毕")
    return translator

@measure_time
def download_models():
    logger.info("📥 正在下载基础模型 (black-forest-labs/FLUX.1-dev)...")
    bfl_repo = snapshot_download("black-forest-labs/FLUX.1-dev", cache_dir="/mnt/f/models")

    logger.info("📥 正在下载 FP8 模型...")
    fp8_model_id = snapshot_download("livehouse/flux1-dev-fp8")

    logger.info("📥 正在下载 LoRA 模型...")
    lora_model_id = snapshot_download("yiwanji/FLUX_xiao_hong_shu_ji_zhi_zhen_shi_V2")

    return {
        'bfl_repo': bfl_repo,
        'fp8_model_path': f"{fp8_model_id}/flux1-dev-fp8.safetensors",
        'lora_model_path': lora_model_id
    }

@measure_time
def init_scheduler_and_tokenizers(bfl_repo):
    logger.info("🧠 初始化 scheduler 和 tokenizer...")
    scheduler = FlowMatchEulerDiscreteScheduler.from_pretrained(bfl_repo, subfolder="scheduler")
    tokenizer = CLIPTokenizer.from_pretrained(bfl_repo, subfolder="tokenizer")
    tokenizer_2 = T5TokenizerFast.from_pretrained(bfl_repo, subfolder="tokenizer_2")
    return scheduler, tokenizer, tokenizer_2

@measure_time
def init_text_encoders(bfl_repo, dtype):
    logger.info("🧠 初始化 text_encoder (CLIP)...")
    text_encoder = CLIPTextModel.from_pretrained(bfl_repo, subfolder="text_encoder", torch_dtype=dtype)

    logger.info("🧠 初始化 text_encoder_2 (T5) 并量化冻结...")
    text_encoder_2 = T5EncoderModel.from_pretrained(bfl_repo, subfolder="text_encoder_2", torch_dtype=dtype)
    
    # 对文本编码器进行量化以节省内存
    if not HARDWARE_FP8_SUPPORTED:
        logger.info("🔧 text_encoder_2 使用 qfloat8 量化文本编码器...")
        quantize(text_encoder_2, weights=qfloat8)
        freeze(text_encoder_2)
    
    return text_encoder, text_encoder_2

@measure_time
def init_vae(bfl_repo, dtype):
    logger.info("🧠 初始化 vae...")
    vae = AutoencoderKL.from_pretrained(bfl_repo, subfolder="vae", torch_dtype=dtype)
    return vae

def patch_flux_transformer_forward():
    """动态修补 FluxTransformerBlock 的 forward 方法以解决 attn_output 错误"""
    logger.info("🔧 应用 FluxTransformerBlock 运行时补丁...")
    
    try:
        from diffusers.models.transformers.transformer_flux import FluxTransformerBlock
        
        # 保存原始 forward 方法
        original_forward = FluxTransformerBlock.forward
        
        # 创建简化的修补后的 forward 方法
        def patched_forward(self, hidden_states, encoder_hidden_states=None, temb=None, attention_mask=None, **kwargs):
            """
            简化的修补 forward 方法
            - 避免使用不存在的属性
            - 只执行基本的操作
            """
            # 调用 norm1 层
            try:
                # 尝试使用 temb 参数
                if temb is not None:
                    norm_hidden_states = self.norm1(hidden_states, temb)
                else:
                    norm_hidden_states = self.norm1(hidden_states)
            except TypeError:
                # 如果失败，只传入 hidden_states
                norm_hidden_states = self.norm1(hidden_states)
            
            # 调用注意力层
            attn_output = self.attn1(
                norm_hidden_states,
                encoder_hidden_states=encoder_hidden_states,
                attention_mask=attention_mask,
                **kwargs
            )
            
            # 残差连接
            hidden_states = attn_output + hidden_states
            
            # 返回结果
            return hidden_states
        
        # 应用补丁
        FluxTransformerBlock.forward = patched_forward
        logger.info("✅ FluxTransformerBlock.forward 修补完成 (简化版)")
        
    except Exception as e:
        logger.error(f"应用补丁失败: {e}")

@measure_time
def init_transformer(bfl_repo, fp8_model_path, lora_model_path, dtype):
    # 应用运行时补丁
    # patch_flux_transformer_forward()
    
    logger.info("🧠 初始化 transformer...")
    transformer = FluxTransformer2DModel.from_pretrained(
        bfl_repo, 
        subfolder="transformer", 
        torch_dtype=dtype
    )
    
    # 加载 FP8 权重（如果支持）
    if HARDWARE_FP8_SUPPORTED:
        logger.info("🔧 加载 FP8 量化权重...")
        state_dict = load_file(fp8_model_path)
        new_state_dict = {}
        for key, value in state_dict.items():
            new_key = key.replace("transformer.", "")
            new_state_dict[new_key] = value
        
        missing, unexpected = transformer.load_state_dict(new_state_dict, strict=False)
        if missing:
            logger.warning(f"缺失的键: {missing}")
        if unexpected:
            logger.warning(f"意外的键: {unexpected}")
    
    # 注入 LoRA 权重
    # logger.info("📥 加载 LoRA 权重...")
    # lora_state_dict = load_file(f'{lora_model_path}/苏-FLUX抖音小红书极致真实_苏-FLUX小红书极致真实V2.safetensors')
    # transformer.load_state_dict(lora_state_dict, strict=False)
    
    # 应用量化
    if HARDWARE_FP8_SUPPORTED:
        logger.info("✅ transformer 使用硬件原生 FP8")
        transformer = transformer.to(torch.float8_e4m3fn)
    else:
        logger.info("🔧 transformer 使用 qfloat8 模拟 FP8 量化...")
        quantize(transformer, weights=qfloat8)
        freeze(transformer)
        logger.info("🔒 模型已量化为 qfloat8 并冻结")
    
    return transformer

@measure_time
def build_pipeline(**kwargs):
    logger.info("🧠 构建 FluxPipeline...")
    pipe = FluxPipeline(**kwargs)
    
    # 使用原始调度器（避免兼容性问题）
    logger.info("🔧 使用原始 FlowMatchEulerDiscreteScheduler 调度器...")
    
    # 启用内存优化
    pipe.enable_model_cpu_offload()
    pipe.enable_vae_slicing()
    
    # 禁用可能引起问题的特性
    try:
        if hasattr(pipe, 'disable_xformers_memory_efficient_attention'):
            pipe.disable_xformers_memory_efficient_attention()
        if hasattr(pipe, 'disable_freeu'):
            pipe.disable_freeu()
    except Exception as e:
        logger.warning(f"无法禁用某些特性: {e}")
    
    return pipe

def translate_text(translator, input_text):
    """翻译文本"""
    try:
        result = translator.translate(input_text)
        return result
    except Exception as e:
        logger.error(f"翻译失败: {e}")
        return input_text

@measure_time
def init_pipe():
    logger.info("📥 正在下载基础模型...")
    model_paths = download_models()

    logger.info("🧠 初始化 scheduler 和 tokenizer...")
    scheduler, tokenizer, tokenizer_2 = init_scheduler_and_tokenizers(model_paths['bfl_repo'])

    logger.info("🧠 初始化 text_encoder (CLIP)...")
    text_encoder, text_encoder_2 = init_text_encoders(model_paths['bfl_repo'], dtype)

    logger.info("🧠 初始化 vae...")
    vae = init_vae(model_paths['bfl_repo'], dtype)

    logger.info("🧠 初始化 transformer ...")
    transformer = init_transformer(
        model_paths['bfl_repo'],
        model_paths['fp8_model_path'],
        model_paths['lora_model_path'],
        dtype
    )

    logger.info("🧠 构建 FluxPipeline 并启用自动卸载...")
    pipe = build_pipeline(
        scheduler=scheduler,
        text_encoder=text_encoder,
        tokenizer=tokenizer,
        text_encoder_2=text_encoder_2,
        tokenizer_2=tokenizer_2,
        vae=vae,
        transformer=transformer,
    )

    # 清理内存
    del text_encoder, text_encoder_2, vae, transformer
    torch.cuda.empty_cache()
    return pipe

def safe_generate_image(pipeline, prompt, steps=20, width=1024, height=1024, guidance=3.5, seed=-1):
    """安全生成图像，包含错误处理和重试机制"""
    max_retries = 3
    current_width, current_height = width, height
    
    for attempt in range(max_retries):
        try:
            # 创建生成器
            if seed == -1:
                generator = torch.Generator(device="cuda").manual_seed(torch.seed())
            else:
                generator = torch.Generator(device="cuda").manual_seed(seed)
            
            # 生成图像
            image = pipeline(
                prompt=prompt,
                width=current_width,
                height=current_height,
                num_inference_steps=steps,
                guidance_scale=guidance,
                generator=generator,
            ).images[0]
            
            return image
        
        except Exception as e:
            if "attn_output" in str(e) and attempt < max_retries - 1:
                logger.error(f"遇到 attn_output 错误，重试 {attempt+1}/{max_retries}")
                
                # 降低分辨率
                current_width = max(512, int(current_width * 0.8))
                current_height = max(512, int(current_height * 0.8))
                logger.info(f"新分辨率: {current_width}x{current_height}")
                
                # 清理内存
                torch.cuda.empty_cache()
            else:
                raise e

@measure_time
def run_pipeline(
    pipeline,
    translator,
    prompt: str,
    steps: int = 50,
    width: int = 1024,
    height: int = 1024,
    guidance: float = 3.5,
    seed: int = -1,
    output_path: str = None
):
    """
    运行图像生成流程
    :param pipeline: 图像生成管道
    :param translator: 翻译器
    :param prompt: 用户输入的提示词（中文）
    :param steps: 推理步数
    :param width: 图像宽度
    :param height: 图像高度
    :param guidance: 引导系数
    :param seed: 随机种子（默认为 -1 表示随机）
    :param output_path: 输出路径（默认为当前目录下的 test_output.png）
    """
    if output_path is None:
        current_working_directory = os.path.dirname(os.path.abspath(__file__))
        output_path = os.path.join(current_working_directory, "test_output.png")

    torch.cuda.empty_cache()
    print_gpu_memory("生成前显存状态")

    translated_prompt = translate_text(translator, prompt)
    logger.info(f"📝 Prompt: {prompt} → Translated: {translated_prompt}")

    logger.info("🧠 开始生成图片...")
    
    # 使用安全生成函数
    image = safe_generate_image(
        pipeline=pipeline,
        prompt=translated_prompt,
        steps=steps,
        width=width,
        height=height,
        guidance=guidance,
        seed=seed
    )

    print_gpu_memory("生成后显存状态")
    logger.info(f"📸 图像生成完成，正在保存至：{output_path}")
    image.save(output_path)
    return output_path

# 主程序
if __name__ == "__main__":
    try:
        logger.info("🧠 开始加载翻译模型...")
        translator = load_translator()
        
        logger.info("🚀 初始化图像生成管道...")
        pipeline = init_pipe()
        
        # 测试生成 - 使用更小的分辨率进行初始测试
        generated_image = run_pipeline(
            pipeline=pipeline,
            translator=translator,
            prompt="一只可爱的小猫",
            steps=20,
            width=768,  # 较小分辨率
            height=768,
            guidance=3.5,
            seed=42  # 固定种子
        )
        
        logger.info(f"✅ 图像生成成功: {generated_image}")
        
        # 成功后尝试更高分辨率
        generated_image = run_pipeline(
            pipeline=pipeline,
            translator=translator,
            prompt="一只可爱的小猫",
            steps=20,
            width=1280,
            height=720,
            guidance=3.5,
            seed=42
        )
        logger.info(f"✅ 高分辨率图像生成成功: {generated_image}")
    
    except Exception as e:
        logger.exception("🚨 主程序运行过程中发生异常")
        
        # 提供详细的诊断信息
        if "use_ada_layer_norm" in str(e):
            logger.error("检测到 'use_ada_layer_norm' 属性问题，请尝试以下解决方案：")
            logger.error("1. 升级 diffusers 库到最新版本: pip install -U diffusers")
            logger.error("2. 检查模型与库版本兼容性")
            logger.error("3. 使用更简单的补丁方法")
        elif "attn_output" in str(e):
            logger.error("检测到 attn_output 错误，请尝试以下解决方案：")
            logger.error("1. 使用更小的分辨率（如 768x768）")
            logger.error("2. 使用不同的调度器")
            logger.error("3. 检查模型权重完整性")
        else:
            logger.error(f"未知错误类型: {str(e)}")