import torch
from pprint import pprint

def check_fp8_support():
    """全面检测系统对 FP8 的支持情况"""
    results = {
        "fp8_supported": False,
        "reasons": [],
        "details": {}
    }
    
    # 1. 检查 GPU 是否存在
    if not torch.cuda.is_available():
        results["reasons"].append("❌ 未检测到 CUDA 设备")
        return results
    
    # 2. 获取 GPU 信息
    device_count = torch.cuda.device_count()
    results["details"]["device_count"] = device_count
    results["details"]["devices"] = []
    
    for i in range(device_count):
        device_info = {}
        props = torch.cuda.get_device_properties(i)
        
        device_info["id"] = i
        device_info["name"] = props.name
        device_info["compute_capability"] = f"{props.major}.{props.minor}"
        device_info["total_memory"] = f"{props.total_memory / 1024**3:.1f} GB"
        
        # 3. 检查 GPU 架构
        # FP8 需要 Ada Lovelace (8.9) 或 Hopper (9.0) 架构
        is_ada_lovelace = props.major == 8 and props.minor >= 9
        is_hopper = props.major == 9
        device_info["fp8_arch_supported"] = is_ada_lovelace or is_hopper
        
        if not device_info["fp8_arch_supported"]:
            reason = f"❌ 架构不支持: {props.name} (计算能力 {props.major}.{props.minor})"
            results["reasons"].append(reason)
        else:
            results["fp8_supported"] = True
            
        results["details"]["devices"].append(device_info)
    
    # 4. 检查 CUDA 版本
    cuda_version = torch.version.cuda
    results["details"]["cuda_version"] = cuda_version
    
    # FP8 需要 CUDA 11.8+
    if cuda_version:
        major, minor = map(int, cuda_version.split('.'))
        cuda_supported = (major >= 12) or (major == 11 and minor >= 8)
        results["details"]["cuda_supported"] = cuda_supported
        
        if not cuda_supported:
            results["reasons"].append(f"❌ CUDA 版本过低: {cuda_version} (需要 11.8+)")
    else:
        results["reasons"].append("❌ 无法确定 CUDA 版本")
    
    # 5. 检查 PyTorch 版本
    torch_version = torch.__version__
    results["details"]["torch_version"] = torch_version
    
    # FP8 需要 PyTorch 2.1+
    torch_major = int(torch_version.split('.')[0])
    torch_minor = int(torch_version.split('.')[1])
    torch_supported = torch_major > 2 or (torch_major == 2 and torch_minor >= 1)
    results["details"]["torch_supported"] = torch_supported
    
    if not torch_supported:
        results["reasons"].append(f"❌ PyTorch 版本过低: {torch_version} (需要 2.1+)")
    
    # 6. 检查 transformer_engine 库
    try:
        import transformer_engine
        te_version = transformer_engine.__version__
        results["details"]["transformer_engine"] = te_version
        results["details"]["te_installed"] = True
    except ImportError:
        results["details"]["te_installed"] = False
        results["reasons"].append("❌ 未安装 transformer_engine 库")
    
    # 7. 实际 FP8 张量创建测试
    if all([results["fp8_supported"], cuda_supported, torch_supported, results["details"].get("te_installed", False)]):
        try:
            # 尝试创建 FP8 张量
            fp8_tensor = torch.tensor([1.0, 2.0], dtype=torch.float8_e4m3fn, device="cuda")
            results["details"]["fp8_test"] = "✅ 成功创建 FP8 张量"
        except Exception as e:
            results["reasons"].append(f"❌ FP8 测试失败: {str(e)}")
            results["details"]["fp8_test"] = f"❌ 错误: {str(e)}"
    else:
        results["details"]["fp8_test"] = "未执行（前置条件不满足）"
    
    # 最终结论
    if results["fp8_supported"] and not results["reasons"]:
        results["summary"] = "✅ 您的系统完全支持 FP8!"
    else:
        results["summary"] = "❌ 您的系统不支持 FP8，原因见下方"
    
    return results

# 运行检测
fp8_status = check_fp8_support()
print("\n" + "="*50)
print("FP8 支持性检测报告")
print("="*50)
print(f"最终结论: {fp8_status['summary']}")

if fp8_status["reasons"]:
    print("\n不支持的原因:")
    for reason in fp8_status["reasons"]:
        print(f"  - {reason}")

print("\n详细系统信息:")
pprint(fp8_status["details"])