
import os

from fastapi import FastAPI, Request,Form
from fastapi.responses import StreamingResponse
from pathlib import Path
import uvicorn
import uuid
from starlette.background import BackgroundTask
from flux_generage import generate
current_working_directory = str(Path(__file__).resolve().parent)

app = FastAPI()
print(f'app inited')
@app.post("/get_image_remote")
async def get_image_remote(prompt:str = Form(...)):

    img = generate(prompt, 4, 3.5, 1280, 720, -1)
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
        media_type="image/png",
        background=BackgroundTask(lambda: os.remove(path)),
    )


# if __name__ == "__main__":
#     print("start sercver")
#     uvicorn.run(
#         app, host="0.0.0.0", port=10001, log_level="debug", workers=1
#     )
