from modelscope import AutoModelForCausalLM, AutoTokenizer,snapshot_download
import torch
import bitsandbytes as bnb
print(bnb.__version__)  # 应输出 0.41.1
model_name = snapshot_download("Qwen/Qwen2.5-1.5B-Instruct")
print(torch.cuda.is_available())
  # 明确使用bfloat16数据类型（3090支持）
torch_dtype = torch.bfloat16 if torch.cuda.is_available() else torch.float32
        
        # 加载模型，添加内存优化选项
model = AutoModelForCausalLM.from_pretrained(
            model_name,
            torch_dtype="auto",
            device_map="auto",
            low_cpu_mem_usage=True,
            trust_remote_code=True,
            use_flash_attention_2=False,  # 启用 Flash Attention（加速 Attention 计算）
        )
print('init model')
tokenizer = AutoTokenizer.from_pretrained(model_name, trust_remote_code=True)
print('init tokenizer')
def do_generate(prompt,system="你是一个非常棒的AI助手"):
    messages = [
        {"role": "system", "content": system},
        {"role": "user", "content": prompt}
    ]
    try:
            text = tokenizer.apply_chat_template(
                messages,
                tokenize=False,
                add_generation_prompt=True
            )
            print('init text')
            model_inputs = tokenizer([text], return_tensors="pt").to(model.device)
            print('init model_inputs')
            generated_ids = model.generate(
                **model_inputs,
                        max_new_tokens=512,
                        do_sample=True,
                        temperature=0.7,
                        top_p=0.9,
                        pad_token_id=tokenizer.eos_token_id,  # 避免警告

            )
            print('init generated_ids')
            generated_ids = [
                output_ids[len(input_ids):] for input_ids, output_ids in zip(model_inputs.input_ids, generated_ids)
            ]
            print('init generated_ids2')
            response = tokenizer.batch_decode(generated_ids, skip_special_tokens=True)[0]
            print('init response')
            print(response)
            return response
    except Exception as e:
            print(f"发生错误: {str(e)}")
            return "error"
    finally:
            # 清理GPU内存
            if torch.cuda.is_available():
                torch.cuda.empty_cache()