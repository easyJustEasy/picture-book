# File: translator.py

from modelscope import AutoModelForCausalLM, AutoTokenizer
import torch
from modelscope import snapshot_download

class QwenTranslator:
    def __init__(self, model_name="Qwen/Qwen3-0.6B", device="auto", torch_dtype="auto"):
        """
        初始化模型和分词器
        :param model_name: 模型名称或路径
        :param device: 设备 ('cpu' 或 'cuda')
        :param torch_dtype: 推理精度 (如 'auto', torch.float16 等)
        """
        self.device = device
        self.tokenizer = AutoTokenizer.from_pretrained(model_name)
        self.model = AutoModelForCausalLM.from_pretrained(
            model_name,
            torch_dtype=torch_dtype,
            device_map=device
        )
    def is_chinese(self,string):
        """判断字符串是否包含中文"""
        return any("\u4e00" <= ch <= "\u9fff" for ch in string)
    def translate(self, prompt, max_new_tokens=32768):
        """
        将输入的中文 Prompt 翻译成英文
        :param prompt: 中文 Prompt 字符串
        :param max_new_tokens: 最大生成 token 数量
        :return: 英文翻译结果字符串
        """
        if self.is_chinese(prompt)==False:
            return prompt;
        messages = [
            {"role": "user", "content": prompt},{"role":"system","content":"根据用户输入中文，输出英文翻译结果，只需要结果"}
        ]
        text = self.tokenizer.apply_chat_template(
            messages,
            tokenize=False,
            add_generation_prompt=True,
            enable_thinking=True  # 可控制是否开启思考模式
        )
        model_inputs = self.tokenizer([text], return_tensors="pt").to(self.model.device)

        generated_ids = self.model.generate(
            **model_inputs,
            max_new_tokens=max_new_tokens
        )
        output_ids = generated_ids[0][len(model_inputs.input_ids[0]):].tolist()

        # 尝试提取纯 response 部分（跳过思考链）
        try:
            index = len(output_ids) - output_ids[::-1].index(151668)  # 找到最后一个 151668 (专属 token)
        except ValueError:
            index = 0

        content = self.tokenizer.decode(output_ids[index:], skip_special_tokens=True).strip("\n")
        return content

    def test(self):
        test_cases = [
            "你好，世界！",
            "我喜欢学习人工智能。",
            "今天天气不错。",
            "这是一个测试句子。",
            "创建一张高度详细且逼真的年轻女性肖像照片。她拥有一头乌黑亮丽的长发，自然地披散在肩上，发丝光泽顺滑，显得非常健康和有质感。她的皮肤白皙细腻，散发出自然的光泽，显示出良好的肤质和健康状态。她的面部特征精致而甜美，包括大而明亮的眼睛、浓密的睫毛、小巧挺拔的鼻子以及涂有柔和粉色口红的丰满嘴唇，整体给人一种温柔和清新的感觉。她穿着一件白色的背心，背心上有黑色的品牌标志，简洁大方。外面搭配了一件红色格子衬衫，衬衫宽松舒适，袖子微微卷起，增加了休闲感和随性感。她的下身穿着一条浅色牛仔短裤，露出修长的双腿。脚上穿着一双经典的黑白相间的帆布鞋，鞋子设计简约时尚，与整体造型相得益彰。她的姿态自然放松，坐在屋顶的木板上，双腿交叉，双手放在膝盖上，微微侧身看向一侧，嘴角带着淡淡的微笑，给人一种亲切和自信的感觉。背景设定在一个城市的屋顶环境中，远处可以看到一些建筑物和山脉的轮廓，天空晴朗，阳光明媚，营造出一种轻松愉快的氛围。光线柔和均匀，照亮了她的脸庞和衣物，使整个画面显得非常明亮和清新。构图主要集中在她的上半身和脸部，采用浅景深效果，使她成为画面的焦点，同时将背景元素柔和地虚化处理，进一步突显了她的美丽与优雅。细节方面，注意她的头发丝滑自然，要显得随意而不失整洁；眼睛要有神采，眼神清澈明亮；唇色要柔和自然，不要过于鲜艳；衣物的褶皱和光影效果要真实自然，体现出材质的柔软和质感。背景中的建筑物和山脉要清晰但不喧宾夺主，增加画面的深度和层次感"
        ]

        for src in test_cases:
            tgt = self.translate(src)
            print(f"原文: {src}")
            print(f"翻译: {tgt}")
            print("-" * 40)


if __name__ == "__main__":
    tester = QwenTranslator()
    tester.test()