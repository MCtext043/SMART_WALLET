#!/usr/bin/env python3
import json
import os
import sys
import tarfile
import tempfile
import time
from pathlib import Path

import paramiko

HOST = "85.198.96.227"
REMOTE = "/opt/smartwallet"
ROOT = Path(__file__).resolve().parent.parent
PASSWORD = os.environ.get("DEPLOY_SSH_PASS", "")


def main():
    if not PASSWORD:
        print("Set DEPLOY_SSH_PASS", file=sys.stderr)
        return 1

    client = paramiko.SSHClient()
    client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    client.connect(HOST, username="root", password=PASSWORD, timeout=30)

    def run(cmd, timeout=900):
        print(">>>", cmd[:100])
        _, stdout, stderr = client.exec_command(cmd, timeout=timeout)
        out = stdout.read().decode("utf-8", errors="replace")
        err = stderr.read().decode("utf-8", errors="replace")
        code = stdout.channel.recv_exit_status()
        if out.strip():
            print(out[-3000:])
        if code and err.strip():
            print("ERR:", err[-1000:])
        return code, out

    tmp = tempfile.NamedTemporaryFile(suffix=".tar.gz", delete=False)
    tmp.close()
    with tarfile.open(tmp.name, "w:gz") as tar:
        for name in ["docker-compose.yml", "Dockerfile", ".dockerignore", "pom.xml", "src"]:
            tar.add(ROOT / name, arcname=name)

    sftp = client.open_sftp()
    sftp.put(tmp.name, f"{REMOTE}/deploy.tar.gz")
    sftp.close()
    os.unlink(tmp.name)

    run(f"cd {REMOTE} && rm -rf src")
    run(f"cd {REMOTE} && tar -xzf deploy.tar.gz")
    run(f"ls {REMOTE}/src/main/java/com/smartwallet/gateway/")
    run(f"cd {REMOTE} && docker compose build --no-cache api")
    run(f"cd {REMOTE} && docker compose up -d api")

    for _ in range(40):
        time.sleep(5)
        _, out = run("curl -sf http://127.0.0.1:8000/health || true", timeout=30)
        if "healthy" in out[1]:
            print("API is healthy")
            break

    _, login_out = run(
        'curl -sf -m 30 -X POST http://127.0.0.1:8000/auth/login '
        '-H "Content-Type: application/json" '
        '-d \'{"phone":"+79001112233","password":"123456"}\'',
        timeout=60,
    )
    try:
        token = json.loads(login_out[1]).get("access_token", "")
    except json.JSONDecodeError:
        token = ""

    if token:
        _, chat_out = run(
            f'curl -sf -m 120 -X POST http://127.0.0.1:8000/assistant/chat '
            f'-H "Authorization: Bearer {token}" '
            f'-H "Content-Type: application/json" '
            f'-d \'{{"message":"привет"}}\'',
            timeout=150,
        )
        print("Assistant reply preview:", chat_out[1][:400])

    client.close()
    return 0


if __name__ == "__main__":
    sys.exit(main())
