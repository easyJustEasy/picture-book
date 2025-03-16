
import os
from pathlib import Path
import gradio as gr
from flux_generage import generate
current_working_directory = str(Path(__file__).resolve().parent)
current_working_directory = str(Path(__file__).resolve().parent)

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
