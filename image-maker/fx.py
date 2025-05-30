import torch
from diffusers import FluxPipeline
from modelscope import snapshot_download
from optimum.quanto import quantize, qfloat8, freeze
import warnings
import os
import time
import psutil
import logging
from datetime import datetime
import sys
import GPUtil
import uuid
from functools import wraps
from safetensors.torch import load_file
from transformers import (
    CLIPTextModel,
    CLIPTokenizer,
    T5EncoderModel,
    T5TokenizerFast,
    pipeline,
    AutoTokenizer,
    AutoModelForSeq2SeqLM,ChineseCLIPProcessor, ChineseCLIPTextModel
)
from pathlib import Path

# 配置日志系统
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s',
    handlers=[
        logging.FileHandler("flux_quantization.log"),
        logging.StreamHandler()
    ]
)
logger = logging.getLogger("FluxQuantization")
current_working_directory = str(Path(__file__).resolve().parent)

batch=2
# 定义视觉元素
EMOJI = {
    "start": "🚀",
    "system": "🖥️",
    "download": "📥",
    "model": "🧠",
    "quant": "⚡",
    "freeze": "❄️",
    "offload": "💾",
    "generate": "🎨",
    "success": "✅",
    "warning": "⚠️",
    "error": "❌",
    "complete": "🏆",
    "time": "⏱️",
    "gpu": "🎮",
    "cpu": "💻",
    "memory": "🧮",
    "image": "🖼️",
    "decorator": "⏳",
    "chinese": "🇨🇳"

}



# ==================== 计时装饰器 ====================
def timeit(description=None, emoji=None):
    """带表情符号的计时装饰器"""
    def decorator(func):
        @wraps(func)
        def wrapper(*args, **kwargs):
            # 生成默认描述和表情
            desc = description or func.__name__.replace('_', ' ').title()
            emoji_char = emoji or EMOJI['decorator']
            
            # 开始计时
            start_time = time.time()
            logger.info(f"{emoji_char} 开始: {desc}...")
            
            # 执行函数
            result = func(*args, **kwargs)
            
            # 计算耗时
            elapsed = time.time() - start_time
            logger.info(f"{EMOJI['success']} 完成: {desc}!")
            logger.info(f"  {EMOJI['time']} 耗时: {elapsed:.2f}秒")
            
            return result
        return wrapper
    return decorator

# ==================== 系统信息 ====================
@timeit("系统信息收集", EMOJI['system'])
def print_system_info():
    """打印详细的系统信息"""
    logger.info(f"{EMOJI['system']} ===== 系统详情 =====")
    logger.info(f"  {EMOJI['time']} 当前时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    logger.info(f"  {EMOJI['system']} 操作系统: {os.name} {os.uname().version}")
    logger.info(f"  {EMOJI['system']} Python版本: {sys.version.split()[0]}")
    logger.info(f"  {EMOJI['system']} PyTorch版本: {torch.__version__}")
    
    if torch.cuda.is_available():
        logger.info(f"  {EMOJI['gpu']} CUDA版本: {torch.version.cuda}")
        logger.info(f"  {EMOJI['gpu']} GPU型号: {torch.cuda.get_device_name(0)}")
        logger.info(f"  {EMOJI['memory']} GPU内存: {torch.cuda.get_device_properties(0).total_memory/1024**3:.2f} GB")
    else:
        logger.info(f"  {EMOJI['warning']} CUDA不可用 - 使用CPU模式")
    
    logger.info(f"  {EMOJI['cpu']} CPU核心数: {psutil.cpu_count(logical=False)} (逻辑核心: {psutil.cpu_count()})")
    logger.info(f"  {EMOJI['memory']} 总内存: {psutil.virtual_memory().total/1024**3:.2f} GB")
    logger.info(f"{EMOJI['system']} ====================")

# ==================== 模型信息打印 ====================
def print_model_info(model, name):
    """打印模型信息"""
    logger.info(f"\n{EMOJI['model']} ===== {name} 模型架构 =====")
    logger.info(f"  {EMOJI['model']} 类型: {type(model).__name__}")
    logger.info(f"  {EMOJI['memory']} 参数量: {sum(p.numel() for p in model.parameters()):,}")
    logger.info(f"  {EMOJI['cpu']} 设备: {next(model.parameters()).device}")
    logger.info(f"  {EMOJI['model']} 数据类型: {next(model.parameters()).dtype}")
    logger.info(f"{EMOJI['model']} =========================")

# ==================== GPU内存监控 ====================
@timeit("GPU内存快照", EMOJI['gpu'])
def log_gpu_memory(context=""):
    """高级GPU内存监控"""
    if context:
        logger.info(f"{EMOJI['memory']} {context}资源状态:")
    
    if torch.cuda.is_available():
        mem_alloc = torch.cuda.memory_allocated() / 1024**3
        mem_reserved = torch.cuda.memory_reserved() / 1024**3
        
        # 获取更详细的GPU信息
        gpus = GPUtil.getGPUs()
        if gpus:
            gpu = gpus[0]
            logger.info(f"{EMOJI['gpu']} GPU内存: {gpu.memoryUsed:.2f}/{gpu.memoryTotal:.2f} GB (使用率: {gpu.memoryUtil*100:.1f}%)")
            logger.info(f"{EMOJI['gpu']} GPU负载: 计算 {gpu.load*100:.1f}% | 显存 {gpu.memoryUtil*100:.1f}% | 温度 {gpu.temperature}°C")
        else:
            logger.info(f"{EMOJI['gpu']} GPU内存: 已分配 {mem_alloc:.2f} GB | 已保留 {mem_reserved:.2f} GB")
    
    # 添加CPU内存信息
    cpu_mem = psutil.virtual_memory()
    logger.info(f"{EMOJI['cpu']} CPU内存: 使用 {cpu_mem.used/1024**3:.2f}/{cpu_mem.total/1024**3:.2f} GB ({cpu_mem.percent}%)")

# ==================== 主程序 ====================

"""主量子化创作流程"""
# 打印启动横幅
logger.info(f"\n{EMOJI['start']}{'='*80}")
logger.info(f"{'='*80}")
logger.info(f"{EMOJI['start']} 启动 FLUX-1 量子化推理引擎")
logger.info(f"{EMOJI['start']} 任务: 使用 QFloat8 量化生成高清图像")
logger.info(f"{'='*80}\n")

# 设置环境变量
os.environ['CUDA_LAUNCH_BLOCKING'] = "1"
os.environ["PYTORCH_CUDA_ALLOC_CONF"] = "expandable_segments:True"
os.environ['TORCH_CUDA_ARCH_LIST'] = "8.6"
torch.set_default_device("cpu")  # 所有模型默认加载到 CPU

# 忽略quanto的特定警告
warnings.filterwarnings("ignore", message="Some weights of .* were not initialized from the model checkpoint.*")
# 下载模型
@timeit("下载FLUX-1模型", EMOJI['download'])
def download_model():
    logger.info("📥 正在下载 FLUX 模型...")
    repo = snapshot_download("black-forest-labs/FLUX.1-dev", cache_dir="/mnt/f/models")
    logger.info(f"{EMOJI['model']} 存储位置: {repo}")
    logger.info("📥 正在下载 LoRA 模型...")
    lora_model_id = snapshot_download("yiwanji/FLUX_xiao_hong_shu_ji_zhi_zhen_shi_V2")
    logger.info(f"{EMOJI['model']} 存储位置: {lora_model_id}")
    clip_model_id = snapshot_download("AI-ModelScope/clip-vit-large-patch14")
    return {
        'bfl_repo': repo,
        'lora_model_path': lora_model_id,
        "clip_model_id":clip_model_id
    }


# 初始化管道
@timeit("初始化量子化管道", EMOJI['model'])
def initialize_pipeline():
    pipe = FluxPipeline.from_pretrained(bfl_repo, 
                                        torch_dtype=torch.bfloat16)
    return pipe

@timeit("设置toker", EMOJI['model'])
def reset_token(pipe):
    # text_encoder = CLIPTextModel.from_pretrained(clip_model_id, torch_dtype=torch.bfloat16,use_fast=False)
    # tokenizer = CLIPTokenizer.from_pretrained(clip_model_id, torch_dtype=torch.bfloat16,use_fast=False)
    # pipe.text_encoder= text_encoder
    # pipe.tokenizer=tokenizer
    print("+++++++++++++++++++==")
# 注入laro
@timeit("注入 LoRA 权重", EMOJI['quant'])
def inject_lora(pipe):
    # 注入 LoRA 权重
    logger.info("📥 加载 LoRA 权重...")
    lora_state_dict = load_file(f'{lora_model_path}/苏-FLUX抖音小红书极致真实_苏-FLUX小红书极致真实V2.safetensors')
    pipe.transformer.load_state_dict(lora_state_dict, strict=False)
    

# 量子化模型
@timeit("量子化模型组件", EMOJI['quant'])
def quantize_model_components(pipe):
    logger.info(f"{EMOJI['quant']} 应用 QFloat8 量化到文本编码器...")
    quantize(pipe.text_encoder_2, weights=qfloat8)
    logger.info(f"{EMOJI['quant']} 应用 QFloat8 量化到 Transformer 核心...")
    quantize(pipe.transformer, weights=qfloat8)
    logger.info(f"{EMOJI['quant']} 文本编码器量子化状态")
    logger.info(f"{EMOJI['quant']} Transformer量子化状态")

# 冻结模型
@timeit("冻结量子化参数", EMOJI['freeze'])
def freeze_model(pipe):
    freeze(pipe.text_encoder_2)
    freeze(pipe.transformer)



# 启用CPU offload
@timeit("激活智能内存管理", EMOJI['offload'])
def enable_memory_management(pipe):
    pipe.enable_model_cpu_offload()
    
# 打印系统信息
print_system_info()   
model_paths = download_model()
bfl_repo = model_paths['bfl_repo']
lora_model_path = model_paths['lora_model_path']
clip_model_id=model_paths["clip_model_id"]    
pipe = initialize_pipeline()
reset_token(pipe)
inject_lora(pipe)
quantize_model_components(pipe)
freeze_model(pipe)
# 打印总耗时
logger.info(f"\n{EMOJI['complete']}{'='*80}")
logger.info(f"{EMOJI['complete']} FLUX-1 量子化创作任务完成!")
logger.info(f"{EMOJI['complete']}{'='*80}")
enable_memory_management(pipe)    
# 生成图像
@timeit("量子艺术生成", EMOJI['generate'])
def generate(prompt,step=50,
                            width=1024,
                            height=1024,
                            guidance_scale=3.5,
                            max_sequence_length=512,
                            ):    
    # 生成参数显示
    logger.info(f"{EMOJI['generate']} 生成参数配置:")
    logger.info(f"  {EMOJI['image']} 分辨率: {width}X{height} (UHD)")
    logger.info(f"  {EMOJI['model']} 引导系数: {guidance_scale}")
    logger.info(f"  {EMOJI['time']} 推理步数: {step}")
    logger.info(f"  {EMOJI['model']} 最大序列长度: {max_sequence_length}")

    # 生成前资源快照
    log_gpu_memory("生成前")
    # 执行生成
    logger.info(f"{EMOJI['generate']} 开始生成量子艺术...")
    seed = -1
    image = pipe(
        prompt=prompt,
        height=height,
        width=width,
        guidance_scale=guidance_scale,
        num_inference_steps=step,
        max_sequence_length=max_sequence_length,
        generator=torch.Generator(device="cpu").manual_seed(int(seed) if seed != -1 else torch.seed()),
    ).images[0]
    
    # 生成后资源快照
    log_gpu_memory("生成后")
    return image







# 执行主程序
if __name__ == "__main__":
    prompt = """
    你好，可以生成一个猫吗，白色的猫
"""
    logger.info(f"{EMOJI['generate']} 提示词: '{prompt}'")
    for num in range(0,2):
        image =  generate(prompt,)
        # 保存图像
        logger.info(f"{EMOJI['image']} 保存量子艺术作品...")
        out_put_path = f"{current_working_directory}/temp/flux_{uuid.uuid1()}.png"
        image.save(out_put_path)
        logger.info(f"{EMOJI['success']} 作品已保存为: {out_put_path}")
    # 打印峰值内存和总耗时（装饰器无法捕获这些）
    peak_mem = psutil.Process().memory_info().rss/1024**3
    logger.info(f"  {EMOJI['memory']} 峰值内存使用: {peak_mem:.2f} GB")
    logger.info(f"{EMOJI['complete']}{'='*80}")
