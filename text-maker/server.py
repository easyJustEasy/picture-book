from text_generate import do_generate
import os
from fastapi import FastAPI, Request,Form
from fastapi.responses import StreamingResponse
from pathlib import Path
import uvicorn
import uuid
from starlette.background import BackgroundTask
os.environ["PYTORCH_CUDA_ALLOC_CONF"] = "expandable_segments:True"

current_working_directory = str(Path(__file__).resolve().parent)

app = FastAPI()
print(f'app inited')
@app.post("/get_text_remote")
async def get_text_remote(prompt:str = Form(...),system:str=Form(default='你是一个非常棒的AI助手')):
        return do_generate(prompt,system)