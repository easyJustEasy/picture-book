# -*- coding: utf-8 -*-
import os
import torch
from cosyvoice.cli.cosyvoice import CosyVoice2
from cosyvoice.utils.file_utils import load_wav
import torchaudio
import sys
import argparse
current_working_directory =os.path.dirname(os.path.abspath(__file__))
# 设置环境变量
sys.path.append(f'{current_working_directory}/third_party/Matcha-TTS')
sys.path.append(f'{current_working_directory}/cosyvoice')
# torch.backends.cudnn.enabled = False
model_dir = f'{current_working_directory}/pretrained_models/CosyVoice2-0.5B'
# 初始化 CosyVoice2 模型
cosyvoice = CosyVoice2(
       model_dir
,
    load_jit=False,  # 如果显存不足，可以尝试设置为 True
    load_trt=False,  # 如果显存不足，可以尝试设置为 True
    fp16=True       # 使用半精度浮点数以减少显存占用
)

# 加载示例音频
prompt_speech_16k = load_wav(f'{current_working_directory}/asset/longyue.wav', 16000)


# 指令语音合成
def run_instruct(text,path):
  for i, j in enumerate(cosyvoice.inference_instruct2(
        text,
        '用普通话亲切甜蜜的口吻说这句话',
        prompt_speech_16k,
        stream=True , # 使用流式推理以减少显存占用
        speed=0.8
    )):
        torchaudio.save(os.path.join(path,f'instruct_{i}.wav'), j['tts_speech'], cosyvoice.sample_rate)
        torch.cuda.empty_cache()  # 释放显存

# 运行推理
if __name__ == "__main__":
    print("current:===================="+current_working_directory)
    parser = argparse.ArgumentParser()
    parser.add_argument('--text',
                        type=str,
                        default="请输入需要转换的文字")
    parser.add_argument('--path',
                        type=str,
                        default=f'{current_working_directory}/temp')
    args = parser.parse_args()
    text = args.text
    run_instruct(text,args.path)