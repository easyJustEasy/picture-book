#!/bin/bash
SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do # resolve $SOURCE until the file is no longer a symlink
  DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE" # if $SOURCE was a relative symlink, we need to resolve it relative to the path where the symlink file was located
done
DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"
#####
#这里是配置信息
ENV_NAME="talk"         # 将这里的your_env_name替换为你要激活的conda环境名称
PYTHON_SCRIPT_PATH=${DIR}"/server.py"  # 替换为你的Python脚本路径
port=10003 # 根据端口号去查询对应的PID
LOG_NAME="image-maker.log"
######

pid=$(sudo netstat -nlp | grep ":$port" | awk '{print $7}' | cut -d'/' -f1)
# 杀掉对应的进程如果PID存在
if [ -n "$pid" ]; then
    sudo kill -9 $pid
    echo "killed port:$port====> pid:$pid"
fi


# 定义你的Conda基础路径和虚拟环境名称
CONDA_BASE=$(conda info --base)  # 获取conda的基础路径
ENV_BIN_PATH=${CONDA_BASE}/envs/${ENV_NAME}/bin
ENV_PYTHON_PATH=${ENV_BIN_PATH}/python
# 检查是否已安装conda
if ! command -v conda &> /dev/null; then
    echo "Conda could not be found. Please install Anaconda or Miniconda."
    exit 1
fi

${ENV_BIN_PATH}/pip install gunicorn uvicorn

# 运行Python脚本
#nohup $ENV_PATH $PYTHON_SCRIPT_PATH> $LOG_NAME 2>&1 &
nohup  ${ENV_BIN_PATH}/gunicorn -w 1 -k uvicorn.workers.UvicornWorker -b 0.0.0.0:$port -t 1800 --log-level 'debug' server:app > $LOG_NAME 2>&1 &
#14分钟启动
echo "$ENV_PYTHON_PATH  $PYTHON_SCRIPT_PATH is running"





