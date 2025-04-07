# import torch
# # 强制清理显存
# torch.cuda.empty_cache()
# torch.cuda.reset_peak_memory_stats()
# print(f"当前 GPU 内存占用: {torch.cuda.memory_allocated() / 1e9:.2f} GB")

def readFile(path):
    with open(path, "r", encoding='utf-8') as f:  # 打开文件
        data = f.read()  # 读取文件
        return data
print(readFile('./asset/海绵宝宝/prompt.txt'))