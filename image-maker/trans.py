from transformers import MarianTokenizer, MarianMTModel
from modelscope import snapshot_download
import torch
import logging

# 设置日志格式
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)


class OpusTranslation:
    def __init__(self):
        model_name =snapshot_download("moxying/opus-mt-zh-en")
        self.tokenizer = MarianTokenizer.from_pretrained(model_name)
        self.model = MarianMTModel.from_pretrained(model_name)

        # 如果有 GPU 支持，可启用 GPU 加速
        if torch.cuda.is_available():
            self.device = "cuda"
            self.model.to("cuda")
        else:
            self.device = "cpu"

        print(f"Model loaded on device: {self.device}")

    def translate(self, text):
        inputs = self.tokenizer(text, return_tensors="pt").to(self.device)
        with torch.no_grad():
            outputs = self.model.generate(
            **inputs,
            max_length=30000,              
            num_beams=5,
            early_stopping=True,
            no_repeat_ngram_size=2,
            pad_token_id=self.tokenizer.pad_token_id
        )

        translated_text = self.tokenizer.decode(outputs[0], skip_special_tokens=True)
        return translated_text

    def test(self):
        test_cases = [
            "你好，世界！",
            "我喜欢学习人工智能。",
            "今天天气不错。",
            "这是一个测试句子。"
        ]

        for src in test_cases:
            tgt = self.translate(src)
            print(f"原文: {src}")
            print(f"翻译: {tgt}")
            print("-" * 40)


if __name__ == "__main__":
    tester = OpusTranslation()
    tester.test()