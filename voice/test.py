# -*- coding: utf-8 -*-
import os


from fastapi import FastAPI, UploadFile, Form, File
from fastapi.responses import StreamingResponse
from fastapi.middleware.cors import CORSMiddleware
import uvicorn
app = FastAPI()
current_working_directory =os.path.dirname(os.path.abspath(__file__))

@app.route("/get_voice")
async def get_voice(tts_text: str = Form(), path: str = Form()):
    
    return {"code":200,"data":{"path":path}}
if __name__ == '__main__':
    print('start server')
    uvicorn.run(app, host="0.0.0.0", port=7777, log_level="info", workers=1)
