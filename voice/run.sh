#!/bin/bash

# 定义你的Conda基础路径和虚拟环境名称
CONDA_BASE=$(conda info --base)  # 获取conda的基础路径
ENV_NAME="cosyvoice"         # 将这里的your_env_name替换为你要激活的conda环境名称
PYTHON_SCRIPT_PATH="server.py"  # 替换为你的Python脚本路径

# 检查是否已安装conda
if ! command -v conda &> /dev/null; then
    echo "Conda could not be found. Please install Anaconda or Miniconda."
    exit 1
fi

# 初始化Conda环境变量
__conda_setup="$('$CONDA_BASE/bin/conda' 'shell.bash' 'hook' 2> /dev/null)"
if [ $? -eq 0 ]; then
    eval "$__conda_setup"
else
    if [ -f "$CONDA_BASE/etc/profile.d/conda.sh" ]; then
        . "$CONDA_BASE/etc/profile.d/conda.sh"
    else
        export PATH="$CONDA_BASE/bin:$PATH"
    fi
fi
unset __conda_setup

# 激活指定的conda环境
source activate $ENV_NAME

# 检查环境是否激活成功
if [ $? -ne 0 ]; then
    echo "Failed to activate Conda environment '$ENV_NAME'."
    exit 1
fi

# 运行Python脚本
nohup python $PYTHON_SCRIPT_PATH> voice.log 2>&1 &






