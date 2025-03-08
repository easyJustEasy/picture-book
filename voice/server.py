from fastapi import FastAPI, Request, Form, HTTPException
from fastapi.responses import StreamingResponse, JSONResponse
from starlette.background import BackgroundTask

import os
import torch
from cosyvoice.cli.cosyvoice import CosyVoice2
from cosyvoice.utils.file_utils import load_wav
import torchaudio
import uuid
import numpy as np
from modelscope import snapshot_download
from pathlib import Path
import uvicorn
import sys
import random

from cosyvoice.utils.common import set_all_random_seed
# 强制清理显存
torch.cuda.empty_cache()
torch.cuda.reset_peak_memory_stats()
# 下载预训练模型
model_dir = snapshot_download('iic/CosyVoice2-0.5B',local_dir='pretrained_models/CosyVoice2-0.5B')
# model_dir = snapshot_download('iic/CosyVoice-300M',local_dir='pretrained_models/CosyVoice-300M')
# 初始化CosyVoice2模型
current_working_directory = str(Path(__file__).resolve().parent)
temp_path = f'{current_working_directory}/temp'
if not os.path.exists(temp_path):
    os.mkdir()
# 设置环境变量
sys.path.append(f'{current_working_directory}/third_party/Matcha-TTS')
sys.path.append(f'{current_working_directory}/cosyvoice')

# 初始化 CosyVoice2 模型
cosyvoice = CosyVoice2(
       model_dir,
       fp16=True
)


def generate_data(model_output):
    for i in model_output:
        tts_audio = (i['tts_speech'].numpy() * (2 ** 15)).astype(np.int16).tobytes()
        yield tts_audio


app = FastAPI()

def readFile(path):
    with open(path, "r") as f:  # 打开文件
        data = f.read()  # 读取文件
        return data


@app.post("/get_voice_remote")
async def get_voice_remote(request: Request):
    seed = random.randint(1, 100000000)
    set_all_random_seed(seed)
    # 强制清理显存
    torch.cuda.empty_cache()
    torch.cuda.reset_peak_memory_stats()
    form = await request.form()
    tts_text = form.get("tts_text")
    audio = form.get("audio")
    speed=form.get("speed")
    if audio is None:
        audio = 'longyue'
    if speed is None:
        speed = 1.0
    print(f'audio is {audio},speed is {speed}')
    prompt_speech_16k_r = load_wav(f'{current_working_directory}/asset/{audio}/prompt.wav', 16000)

    emo = readFile(f'{current_working_directory}/asset/{audio}/prompt.txt')
    model_output = cosyvoice.inference_zero_shot(tts_text, emo, prompt_speech_16k_r,speed)
    tts_audio = b''
    for i, j in enumerate(model_output):
        tau = (j['tts_speech'].numpy() * (2 ** 15)).astype(np.int16).tobytes()
        tts_audio += tau
    tts_speech = torch.from_numpy(np.array(np.frombuffer(tts_audio, dtype=np.int16))).unsqueeze(dim=0)
    path = os.path.join(f'{current_working_directory}/temp', f'instruct_{uuid.uuid1()}.wav')
    torchaudio.save(path, tts_speech, cosyvoice.sample_rate)
    # 强制清理显存
    torch.cuda.empty_cache()
    torch.cuda.reset_peak_memory_stats()
    async def iterfile():
        with open(path, mode="rb") as file_like:
            while True:
                chunk = file_like.read(512*1024)
                if not chunk:
                    break
                yield chunk

    return StreamingResponse(iterfile(), media_type="application/octet-stream",
                             background=BackgroundTask(lambda: os.remove(path)
                                                        ))

if __name__ == "__main__":

    uvicorn.run(app="server:app", host="0.0.0.0", port=10000, log_level="info")
