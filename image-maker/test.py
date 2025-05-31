import torch
print(torch.cuda.get_arch_list())
print(torch.cuda.is_available())
print(torch.cuda.get_device_capability())
print(torch.version.cuda)
print("CuDNN Version:", torch.backends.cudnn.version())