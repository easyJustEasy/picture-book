from fastapi import FastAPI, Request, Form, HTTPException, BackgroundTasks
from fastapi.responses import StreamingResponse, JSONResponse
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
# 下载预训练模型
model_dir = snapshot_download('iic/CosyVoice2-0.5B')

# 初始化CosyVoice2模型
current_working_directory = str(Path(__file__).resolve().parent)
# 设置环境变量
sys.path.append(f'{current_working_directory}/third_party/Matcha-TTS')
sys.path.append(f'{current_working_directory}/cosyvoice')

# 初始化 CosyVoice2 模型
cosyvoice = CosyVoice2(
       model_dir      # 使用半精度浮点数以减少显存占用
)
def generate_data(model_output):
    for i in model_output:
        tts_audio = (i['tts_speech'].numpy() * (2 ** 15)).astype(np.int16).tobytes()
        yield tts_audio



app = FastAPI()


@app.post("/get_voice_remote")
async def get_voice_remote(request: Request):
    form = await request.form()
    tts_text = form.get("tts_text")
    audio = form.get("audio")
    prompt_speech_16k_r = load_wav(f'{current_working_directory}/asset/{audio}.wav', 16000)
    model_output = cosyvoice.inference_instruct2(tts_text, '请用中文普通话朗读下面的儿童故事...', prompt_speech_16k_r)
    tts_audio = b''
    for i, j in enumerate(model_output):
        tau = (j['tts_speech'].numpy() * (2 ** 15)).astype(np.int16).tobytes()
        tts_audio += tau
    tts_speech = torch.from_numpy(np.array(np.frombuffer(tts_audio, dtype=np.int16))).unsqueeze(dim=0)
    path = os.path.join(f'{current_working_directory}/temp', f'instruct_{uuid.uuid1()}.wav')
    torchaudio.save(path, tts_speech, cosyvoice.sample_rate)

    async def iterfile():
        with open(path, mode="rb") as file_like:
            while True:
                chunk = file_like.read(512*1024)
                if not chunk:
                    break
                yield chunk

    return StreamingResponse(iterfile(), media_type="application/octet-stream", background=BackgroundTasks(lambda: os.remove(path)))
if __name__ == "__main__":
    uvicorn.run(app="server:app", host="0.0.0.0", port=8000, log_level="info", workers=2)