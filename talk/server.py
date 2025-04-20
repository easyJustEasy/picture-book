from modelscope.pipelines import pipeline
from modelscope.utils.constant import Tasks

from fastapi import FastAPI, Request,Form,File, UploadFile
from pathlib import Path
from modelscope import snapshot_download

current_working_directory = str(Path(__file__).resolve().parent)
model_id = snapshot_download('iic/SenseVoiceSmall')
inference_pipeline = pipeline(
    task=Tasks.auto_speech_recognition,
    model=model_id,
    model_revision="master",
    device="cuda:0",)
app = FastAPI()

@app.post("/get_voice_txt")
async def get_voice_txt( file: bytes = File(), fileb: UploadFile = File()):
    rec_result = inference_pipeline(file)
    return rec_result




