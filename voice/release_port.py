import sys
import subprocess


def get_process_id_by_port(port):
    """获取占用指定端口的进程ID"""
    if sys.platform.startswith("win"):
        # Windows系统
        result = subprocess.run(["netstat", "-ano"], stdout=subprocess.PIPE)
        for line in result.stdout.decode().splitlines():
            if f":{port}" in line and "LISTENING" in line:
                pid = line.split()[-1]
                return pid
    else:
        # Unix-like系统（Linux, macOS等）
        try:
            result = subprocess.run(["lsof", "-ti", f":{port}"], stdout=subprocess.PIPE)
            pid = result.stdout.decode().strip()
            if pid:
                return pid
        except FileNotFoundError:
            print("lsof command not found. Please install it.")
    return None


def kill_process(pid):
    """根据进程ID终止进程"""
    if sys.platform.startswith("win"):
        # Windows系统
        subprocess.run(["taskkill", "/F", "/PID", str(pid)])
    else:
        # Unix-like系统
        subprocess.run(["kill", "-9", str(pid)])


def release_port(port):
    """释放被占用的端口"""
    pid = get_process_id_by_port(port)
    if pid:
        print(f"Port {port} is being used by process with PID {pid}.")
        kill_process(pid)
        print(f"Process with PID {pid} has been terminated.")
    else:
        print(f"No process found using port {port}.")


if __name__ == "__main__":
    if len(sys.argv) != 2 or not sys.argv[1].isdigit():
        print("Usage: python script.py <port>")
        sys.exit(1)

    port = int(sys.argv[1])
    release_port(port)
