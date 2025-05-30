import os
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
    pipeline,
    AutoTokenizer,
    AutoModelForSeq2SeqLM,
)
from modelscope import snapshot_download
from translator import QwenTranslator
#from trans import OpusTranslation
# 设置环境变量
os.environ["PYTORCH_CUDA_ALLOC_CONF"] = "expandable_segments:True"
os.environ['CUDA_LAUNCH_BLOCKING'] = "1"
os.environ["PYTORCH_CUDA_ALLOC_CONF"] = "expandable_segments:True"
os.environ['TORCH_CUDA_ARCH_LIST'] = "8.6"
torch.set_default_device("cpu")  # 所有模型默认加载到 CPU

# 当前工作目录
current_working_directory = os.path.dirname(os.path.abspath(__file__))
device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
dtype = torch.bfloat16


def print_gpu_memory(step):
    """打印当前 GPU 显存使用情况"""
    if torch.cuda.is_available():
        allocated = torch.cuda.memory_allocated(device=0) / 1024 ** 3
        cached = torch.cuda.memory_reserved(device=0) / 1024 ** 3
        formatted_now = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
        print(f"{formatted_now} {step} ===> 已分配: {allocated:.2f} GB | 保留缓存: {cached:.2f} GB")

# 加载翻译模型（限制在 CPU 上）
print("🧠 加载翻译模型...")
translator = QwenTranslator()
print("🧠 翻译模型加载完毕...")
# 下载主模型和 LoRA 模型
print("📥 正在下载基础模型...")
bfl_repo = snapshot_download("zhusiyuanhao/FLUX1-schnell-fp8")
print("📥 正在下载 LoRA 模型...")
lora_model_path = snapshot_download('yiwanji/FLUX_xiao_hong_shu_ji_zhi_zhen_shi_V2')


# 加载 scheduler 和 tokenizer
print("🧠 初始化 scheduler 和 tokenizer...")
scheduler = FlowMatchEulerDiscreteScheduler.from_pretrained(bfl_repo, subfolder="scheduler")
tokenizer = CLIPTokenizer.from_pretrained(bfl_repo, subfolder="tokenizer")
tokenizer_2 = T5TokenizerFast.from_pretrained(bfl_repo, subfolder="tokenizer_2")

# 加载 text_encoder（CLIP）
print("🧠 初始化 text_encoder (CLIP)...")
text_encoder = CLIPTextModel.from_pretrained(bfl_repo, subfolder="text_encoder", torch_dtype=dtype)

# 加载 text_encoder_2（T5）并进行 FP8 量化 + 冻结
print("🧠 初始化 text_encoder_2 (T5) 并量化冻结...")
text_encoder_2 = T5EncoderModel.from_pretrained(bfl_repo, subfolder="text_encoder_2", torch_dtype=dtype)
quantize(text_encoder_2, weights=qfloat8)
freeze(text_encoder_2)

# 加载 vae
print("🧠 初始化 vae...")
vae = AutoencoderKL.from_pretrained(bfl_repo, subfolder="vae", torch_dtype=dtype)

# 加载 transformer 并注入 LoRA 权重 + 量化 + 冻结
print("🧠 初始化 transformer 并注入 LoRA 权重...")
transformer = FluxTransformer2DModel.from_pretrained(bfl_repo, subfolder="transformer", torch_dtype=dtype)
state_dict = load_file(f'{lora_model_path}/苏-FLUX抖音小红书极致真实_苏-FLUX小红书极致真实V2.safetensors')
transformer.load_state_dict(state_dict, strict=False)
quantize(transformer, weights=qfloat8)
freeze(transformer)

# 构建 pipeline
print("🧠 构建 FluxPipeline 并启用自动卸载...")
pipe = FluxPipeline(
    scheduler=scheduler,
    text_encoder=text_encoder,
    tokenizer=tokenizer,
    text_encoder_2=text_encoder_2,
    tokenizer_2=tokenizer_2,
    vae=vae,
    transformer=transformer,
)
pipe.enable_model_cpu_offload()
pipe.enable_vae_slicing()

# 删除不再需要的组件以释放内存
del text_encoder, text_encoder_2, vae, transformer
torch.cuda.empty_cache()




def translate_text(input_text):
    """翻译文本"""
    try:
    # 可以选择在这里添加重试逻辑或者跳过当前任务
        result =  translator.translate(input_text)
        return result
    except IndexError as e:
        print(f"Caught an IndexError: {e}")
    return input_text


def generate(prompt, steps, guidance, width, height, seed):
    """生成图片"""
    torch.cuda.empty_cache()
    print_gpu_memory("生成前显存状态")
    
    translated_prompt = translate_text(prompt)
    print(f"Prompt: {prompt} → Translated: {translated_prompt}")

    image = pipe(
        prompt=translated_prompt,
        width=width,
        height=height,
        num_inference_steps=steps,
        guidance_scale=guidance,
        generator=torch.Generator(device="cpu").manual_seed(int(seed) if seed != -1 else torch.seed()),
    ).images[0]

    print_gpu_memory("生成后显存状态")
    return image


if __name__ == "__main__":
    print("📸 开始测试图像生成...")
    prompt = "一只可爱的小猫"
    img = generate(prompt=prompt, steps=20, guidance=3.5, width=1280, height=720, seed=-1)
    output_path = os.path.join(current_working_directory, "test_output.png")
    img.save(output_path)
    print(f"✅ 图像已保存至：{output_path}")