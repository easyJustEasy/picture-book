import torch
# 强制清理显存
torch.cuda.empty_cache()
torch.cuda.reset_peak_memory_stats()
print(f"当前 GPU 内存占用: {torch.cuda.memory_allocated() / 1e9:.2f} GB")
