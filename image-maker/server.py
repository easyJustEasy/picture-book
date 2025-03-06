import torch
from diffusers import FluxPipeline


from transformers import AutoTokenizer, pipeline, AutoModelForSeq2SeqLM

# 下载模型
from modelscope import snapshot_download
import os
from fastapi import FastAPI, Request, BackgroundTasks
from fastapi.responses import StreamingResponse
from pathlib import Path
import uvicorn
import uuid

os.environ["PYTORCH_CUDA_ALLOC_CONF"] = "expandable_segments:True"
current_working_directory = str(Path(__file__).resolve().parent)
app = FastAPI()
# 强制清理显存
torch.cuda.empty_cache()
torch.cuda.reset_peak_memory_stats()
bfl_repo = snapshot_download("zhusiyuanhao/FLUX1-schnell-fp8")
dtype = torch.float16
revision = "main"
device = torch.device("cuda:0" if torch.cuda.is_available() else "cpu")
pipe = FluxPipeline.from_pretrained(bfl_repo, torch_dtype=torch.float16)
pipe.enable_model_cpu_offload()
pipe.enable_vae_slicing()

trans_tokenizer = AutoTokenizer.from_pretrained(
    bfl_repo,
    subfolder="cubeai/trans-opus-mt-zh-en",
    torch_dtype=dtype,
    revision=revision,
)
trans_model = AutoModelForSeq2SeqLM.from_pretrained(
    bfl_repo,
    subfolder="cubeai/trans-opus-mt-zh-en",
    torch_dtype=dtype,
    revision=revision,
)
trans_pipeline = pipeline(
    "translation_en_to_zh", model=trans_model, tokenizer=trans_tokenizer,
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
        result = trans_pipeline(input_text, max_length=3000)
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


@app.post("/get_image_remote")
async def get_image_remote(request: Request):

    # 强制清理显存
    torch.cuda.empty_cache()
    torch.cuda.reset_peak_memory_stats()
    form = await request.form()
    prompt = form.get("prompt")
    img = generate(prompt, 4, 0.0, 1280, 720, -1)
    path = os.path.join(
        f"{current_working_directory}/temp", f"img_{uuid.uuid1()}.png"
    )
    img.save(path)

    async def iterfile():
        with open(path, mode="rb") as file_like:
            while True:
                chunk = file_like.read(512 * 1024)
                if not chunk:
                    break
                yield chunk

    return StreamingResponse(
        iterfile(),
        media_type="application/octet-stream",
        background=BackgroundTasks(lambda: os.remove(path)),
    )


if __name__ == "__main__":
    uvicorn.run(
        app="server:app", host="0.0.0.0", port=8001, log_level="info",
        workers=2
    )
