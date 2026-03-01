import os
import platform
import subprocess
PUERTOS = [8080, 9090, 8081, 9091,8082, 9092, 8083, 9093, 8000, 8097]
def liberar_puerto(port):
    sistema = platform.system()
    try:
        if sistema == "Windows":
            output = subprocess.check_output(
                f"netstat -ano | findstr :{port}", shell=True
            ).decode()
            lines = output.strip().split("\n")
            if not lines:
                print(f"Puerto {port} está libre")
                return
            for line in lines:
                pid = line.split()[-1]
                os.system(f"taskkill /PID {pid} /F")
                print(f"Puerto {port} liberado (PID {pid})")
        else:
            pid = subprocess.check_output(
                f"lsof -t -i :{port}", shell=True
            ).decode().strip()
            if pid:
                os.system(f"kill -9 {pid}")
                print(f"Puerto {port} liberado (PID {pid})")
            else:
                print(f"Puerto {port} está libre")
    except subprocess.CalledProcessError:
        print(f"Puerto {port} está libre")
if __name__ == "__main__":
    for port in PUERTOS:
        liberar_puerto(port)