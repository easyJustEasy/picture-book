import torch
from diffusers import FluxPipeline


from transformers import AutoTokenizer, pipeline, AutoModelForSeq2SeqLM

# 下载模型
from modelscope import snapshot_download
import os
from fastapi import FastAPI, Request, BackgroundTasks
from fastapi.responses import StreamingResponse
from pathlib import Path
import gradio as gr

os.environ["PYTORCH_CUDA_ALLOC_CONF"] = "max_split_size_mb:64"
current_working_directory = str(Path(__file__).resolve().parent)

# 强制清理显存
torch.cuda.empty_cache()
torch.cuda.reset_peak_memory_stats()
bfl_repo = snapshot_download("zhusiyuanhao/FLUX1-schnell-fp8")
print(f'downloaded at {bfl_repo}')
dtype = torch.float16
device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
# 这里要指定local_files_only 因为上面已经下载过了
# 同时这里也解释一下为啥用snapshot_download，
# 因为下面翻译没有加入FluxPipeline，翻译模型在flux模型文件夹里面，所以需要获取模型目录
# 启动大概需要8分钟
pipe = FluxPipeline.from_pretrained(
    bfl_repo, 
    torch_dtype=dtype, 
    use_safetensors=True, 
    local_files_only=True)
pipe.enable_model_cpu_offload()
pipe.enable_vae_slicing()

trans_model_name="cubeai/trans-opus-mt-zh-en"
trans_tokenizer = AutoTokenizer.from_pretrained(
    bfl_repo,
    subfolder=trans_model_name,
    torch_dtype=dtype,
)
trans_model = AutoModelForSeq2SeqLM.from_pretrained(
    bfl_repo,
    subfolder=trans_model_name,
    torch_dtype=dtype,
)
trans_pipeline = pipeline(
    "translation_en_to_zh", 
    model=trans_model, 
    tokenizer=trans_tokenizer,
    device=device
)


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
    return image
demo = gr.Interface(
    fn=generate,
    inputs=[
        "textbox",
        gr.Number(value=4),
        gr.Number(value=3.5),
        gr.Slider(0, 1920, value=1024, step=2),
        gr.Slider(0, 1920, value=1024, step=2),
        gr.Number(value=-1),
    ],
    outputs="image",
)

demo.launch(server_name="0.0.0.0", server_port=9000)
