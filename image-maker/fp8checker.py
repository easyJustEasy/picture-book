import torch
from pynvml import nvmlInit, nvmlDeviceGetHandleByIndex, nvmlDeviceGetName, nvmlDeviceGetCudaComputeCapability

def check_fp8_support():
    """检查系统是否支持float8_e4m3fn格式"""
    results = {
        # `fp8_supported` is a boolean variable that indicates whether the system supports the
        # `float8_e4m3fn` format for floating-point numbers. It is determined based on several
        # conditions checked in the `check_fp8_support()` function, including the CUDA compute
        # capability of the GPU, CUDA version, PyTorch version, and the availability of the
        # `float8_e4m3fn` data type in PyTorch. If all these conditions are met, `fp8_supported` is
        # set to `True`, indicating that the system fully supports the `float8_e4m3fn` format.
        
        "fp8_supported": False,
        "cuda_available": torch.cuda.is_available(),
        "gpu_name": "N/A",
        "compute_capability": "N/A",
        "cuda_version": "N/A",
        "torch_version": torch.__version__,
        "fp8_dtype_available": hasattr(torch, "float8_e4m3fn"),
        "recommended_actions": []
    }

    # 检查CUDA可用性
    if not results["cuda_available"]:
        results["recommended_actions"].append("未检测到CUDA设备，请安装NVIDIA显卡和驱动")
        return results

    # 获取CUDA版本
    try:
        results["cuda_version"] = torch.version.cuda
    except AttributeError:
        pass

    # 获取GPU信息
    try:
        nvmlInit()
        handle = nvmlDeviceGetHandleByIndex(0)
        gpu_name = nvmlDeviceGetName(handle).decode('utf-8')
        cc_major, cc_minor = nvmlDeviceGetCudaComputeCapability(handle)
        compute_capability = f"{cc_major}.{cc_minor}"
        
        results["gpu_name"] = gpu_name
        results["compute_capability"] = compute_capability
    except:
        # 回退方法
        if torch.cuda.is_available():
            device = torch.cuda.current_device()
            results["gpu_name"] = torch.cuda.get_device_name(device)
            capability = torch.cuda.get_device_capability(device)
            results["compute_capability"] = f"{capability[0]}.{capability[1]}"

    # 检查FP8支持条件
    fp8_supported = True
    
    # 1. 检查计算能力是否支持
    try:
        cc = float(results["compute_capability"])
        if cc < 8.9:  # Ada/Hopper架构开始支持
            fp8_supported = False
            results["recommended_actions"].append(
                f"GPU计算能力({results['compute_capability']})不足，需要8.9+（RTX 40系列或H100）"
            )
    except:
        fp8_supported = False

    # 2. 检查CUDA版本是否支持
    try:
        cuda_major, cuda_minor = map(int, results["cuda_version"].split('.'))
        if cuda_major < 12:  # CUDA 12+开始支持
            fp8_supported = False
            results["recommended_actions"].append(
                f"CUDA版本({results['cuda_version']})过低，需要12.0+"
            )
    except:
        fp8_supported = False

    # 3. 检查PyTorch版本是否支持
    torch_major, torch_minor, *_ = map(int, torch.__version__.split('+')[0].split('.'))
    if torch_major < 2 or (torch_major == 2 and torch_minor < 1):  # PyTorch 2.1+开始支持
        fp8_supported = False
        results["recommended_actions"].append(
            f"PyTorch版本({torch.__version__})过低，需要2.1+"
        )

    # 4. 检查数据类型是否存在
    if not results["fp8_dtype_available"]:
        fp8_supported = False
        results["recommended_actions"].append(
            "当前PyTorch版本不支持float8_e4m3fn数据类型"
        )

    results["fp8_supported"] = fp8_supported and results["fp8_dtype_available"]
    
    if fp8_supported:
        results["recommended_actions"].append("系统完全支持float8_e4m3fn!")

    return results

def print_support_report(results):
    """打印详细的兼容性报告"""
    print("="*50)
    print("float8_e4m3fn (FP8) 兼容性检查报告")
    print("="*50)
    
    print(f"1. 支持状态: {'✅ 支持' if results['fp8_supported'] else '❌ 不支持'}")
    print(f"2. CUDA可用: {'✅ 是' if results['cuda_available'] else '❌ 否'}")
    print(f"3. GPU型号: {results['gpu_name']}")
    print(f"4. 计算能力: {results['compute_capability']}")
    print(f"5. CUDA版本: {results['cuda_version']}")
    print(f"6. PyTorch版本: {results['torch_version']}")
    print(f"7. float8_e4m3fn数据类型: {'✅ 可用' if results['fp8_dtype_available'] else '❌ 不可用'}")
    
    print("\n诊断建议:")
    if results["recommended_actions"]:
        for i, action in enumerate(results["recommended_actions"], 1):
            print(f"  {i}. {action}")
    else:
        print("  ✅ 无需额外操作")
    
    print("\n完整支持FP8的硬件要求:")
    print("  - NVIDIA GPU架构: Ada (RTX 40系列) 或 Hopper (H100)")
    print("  - 计算能力: 8.9+")
    print("  - CUDA版本: 12.0+")
    print("  - PyTorch版本: 2.1+")
    print("="*50)

if __name__ == "__main__":
    # 检查支持情况
    fp8_support = check_fp8_support()
    
    # 打印详细报告
    print_support_report(fp8_support)
    
    # 实际测试FP8操作（如果支持）
    if fp8_support["fp8_supported"]:
        try:
            print("\n运行FP8测试操作...")
            a = torch.randn(10, 10, device='cuda', dtype=torch.float8_e4m3fn)
            b = torch.randn(10, 10, device='cuda', dtype=torch.float8_e4m3fn)
            c = torch.mm(a, b)
            print("✅ FP8矩阵乘法测试成功!")
            print(f"结果形状: {c.shape}, 数据类型: {c.dtype}")
        except Exception as e:
            print(f"❌ FP8测试失败: {str(e)}")
            fp8_support["fp8_supported"] = False
    else:
        print("\n跳过FP8测试操作（系统不支持）")