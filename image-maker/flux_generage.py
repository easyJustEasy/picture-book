import torch


from optimum.quanto import freeze, qfloat8, quantize

from diffusers import FlowMatchEulerDiscreteScheduler, AutoencoderKL
from diffusers.models.transformers.transformer_flux import FluxTransformer2DModel
from diffusers.pipelines.flux.pipeline_flux import FluxPipeline
from transformers import CLIPTextModel, CLIPTokenizer,T5EncoderModel, T5TokenizerFast,AutoTokenizer, pipeline,AutoModelForSeq2SeqLM
import os


# 下载模型
from modelscope import snapshot_download
from fastapi import FastAPI, Request,Form
from fastapi.responses import StreamingResponse
from pathlib import Path
import uvicorn
import uuid
from starlette.background import BackgroundTask

os.environ["PYTORCH_CUDA_ALLOC_CONF"] = "expandable_segments:True"
current_working_directory = str(Path(__file__).resolve().parent)

device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
# 强制清理显存
torch.cuda.empty_cache()
torch.cuda.reset_peak_memory_stats()
bfl_repo = snapshot_download("zhusiyuanhao/FLUX1-schnell-fp8")
# bfl_repo = '/mnt/e/modescope_model/models/zhusiyuanhao/FLUX1-schnell-fp8'
dtype = torch.bfloat16
bfl_repo =  f'{bfl_repo}'
print(f'downloaded at {bfl_repo}')

def print_gpu_memory(step):
    allocated = torch.cuda.memory_allocated(device=0) / 1024**3  # 已分配显存（GB）
    cached = torch.cuda.memory_reserved(device=0) / 1024**3      # 保留缓存（GB）
    print(f"{step}====> 已分配显存: {allocated:.2f} GB | 保留缓存: {cached:.2f} GB")
scheduler = FlowMatchEulerDiscreteScheduler.from_pretrained(bfl_repo, subfolder="scheduler")
print_gpu_memory('scheduler model inited')
text_encoder = CLIPTextModel.from_pretrained(bfl_repo, subfolder="text_encoder", torch_dtype=dtype)
print_gpu_memory('text_encoder model inited')
tokenizer = CLIPTokenizer.from_pretrained(bfl_repo, subfolder="tokenizer", torch_dtype=dtype)
print_gpu_memory('tokenizer model inited')
text_encoder_2 = T5EncoderModel.from_pretrained(bfl_repo, subfolder="text_encoder_2", torch_dtype=dtype)
quantize(text_encoder_2, weights=qfloat8)
print_gpu_memory('quantize text_encoder_2')
freeze(text_encoder_2)
print_gpu_memory('freeze text_encoder_2')
print_gpu_memory('text_encoder_2 model inited')
tokenizer_2 = T5TokenizerFast.from_pretrained(bfl_repo, subfolder="tokenizer_2", torch_dtype=dtype)
print_gpu_memory('tokenizer_2 model inited')
vae = AutoencoderKL.from_pretrained(bfl_repo, subfolder="vae", torch_dtype=dtype)
print_gpu_memory('vae model inited')
transformer = FluxTransformer2DModel.from_pretrained(bfl_repo, subfolder="transformer", torch_dtype=dtype)
quantize(transformer, weights=qfloat8)
print_gpu_memory('quantize transformer')
freeze(transformer)
print_gpu_memory('freeze transformer')
print_gpu_memory('transformer model inited')
print_gpu_memory('all model inited')



pipe = FluxPipeline(
    scheduler=scheduler,    
    text_encoder=text_encoder,
    tokenizer=tokenizer,
    text_encoder_2=None,
    tokenizer_2=tokenizer_2,
    vae=vae,
    transformer=None,
)
pipe.text_encoder_2= text_encoder_2
pipe.transformer= transformer

print_gpu_memory('init FluxPipeline')
pipe.enable_model_cpu_offload()
pipe.enable_vae_slicing()
pipe.enable_attention_slicing()
trans_tokenizer = AutoTokenizer.from_pretrained(bfl_repo, subfolder="cubeai/trans-opus-mt-zh-en", torch_dtype=dtype)
trans_model = AutoModelForSeq2SeqLM.from_pretrained(bfl_repo, subfolder="cubeai/trans-opus-mt-zh-en", torch_dtype=dtype)
trans_pipeline = pipeline("translation_en_to_zh", model=trans_model, tokenizer=trans_tokenizer, device=device)
def is_chinese(string):
    """
    检查整个字符串是否包含中文
    :param string: 需要检查的字符串
    :return: bool
    """
    for ch in string:
        if "\u4e00" <= ch <= "\u9fff":
            return True
    return False


def translate_text(input_text):
    if is_chinese(input_text):
        result = trans_pipeline(input_text, max_length=30000)
        return result[0]["translation_text"]
    return input_text

def generate(prompt, steps, guidance, width, height, seed):
    # 强制清理显存
    torch.cuda.empty_cache()
    torch.cuda.reset_peak_memory_stats()
    print_gpu_memory('generate img before')
    if seed == -1:
        seed = torch.seed()
    generator = torch.Generator().manual_seed(int(seed))
    translated_text = translate_text(prompt)
    print(f"prompt is {prompt} ,translated_text is {translated_text}")
    image = pipe(
        prompt=translated_text,
        width=width,
        height=height,
        num_inference_steps=steps,
        generator=generator,
        guidance_scale=guidance,
    ).images[0]
    print_gpu_memory('generate img after')
    return image
if __name__ == "__main__":
    prompt = "一只可爱的小猫"
    img = generate(prompt, 20, 3.5, 1280, 720, -1)
    img.save('./test.png')


