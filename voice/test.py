# -*- coding: utf-8 -*-
import os


from fastapi import FastAPI, UploadFile, Form, File
from fastapi.responses import StreamingResponse
from fastapi.middleware.cors import CORSMiddleware
import uvicorn
import subprocess
from flask import Flask
app = Flask(__name__)
current_working_directory =os.path.dirname(os.path.abspath(__file__))

@app.route("/get_voice")
async def get_voice(tts_text: str = Form(), path: str = Form()):
    
    return {"code":200,"data":{"path":path}}
if __name__ == '__main__':
    app.run(host="0.0.0.0", port=8000, threaded=True)
