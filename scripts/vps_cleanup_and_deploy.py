#!/usr/bin/env python3
import os, sys, tarfile, tempfile, time
from pathlib import Path
import paramiko

HOST = os.environ.get("DEPLOY_HOST", "85.198.96.227")
USER = os.environ.get("DEPLOY_USER", "root")
PASSWORD = os.environ.get("DEPLOY_SSH_PASS", "")
REMOTE_DIR = "/opt/smartwallet"
MODEL = os.environ.get("ASSISTANT_OLLAMA_MODEL", "qwen2.5:1.5b")
ROOT = Path(__file__).resolve().parent.parent
INCLUDE = ["docker-compose.yml", "Dockerfile", ".dockerignore", "pom.xml", "src"]


def run(client, cmd, timeout=900):
    print(f">>> {cmd[:100]}")
    _, stdout, stderr = client.exec_command(cmd, timeout=timeout)
    out = stdout.read().decode("utf-8", errors="replace")
    err = stderr.read().decode("utf-8", errors="replace")
    code = stdout.channel.recv_exit_status()
    if out.strip():
        print(out[-2500:])
    if code and err.strip():
        print("ERR:", err[-1000:])
    return code, out


def main():
    if not PASSWORD:
        return 1
    client = paramiko.SSHClient()
    client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    client.connect(HOST, username=USER, password=PASSWORD, timeout=30, allow_agent=False, look_for_keys=False)

    run(client, "df -h / && docker system df")
    run(client, "docker builder prune -af || true", timeout=600)
    run(client, "docker image prune -af || true", timeout=600)
    run(client, "df -h /")

    tmp = tempfile.NamedTemporaryFile(suffix=".tar.gz", delete=False)
    tmp.close()
    with tarfile.open(tmp.name, "w:gz") as tar:
        for name in INCLUDE:
            p = ROOT / name
            if p.exists():
                tar.add(p, arcname=name)
    sftp = client.open_sftp()
    sftp.put(tmp.name, f"{REMOTE_DIR}/deploy.tar.gz")
    sftp.close()
    os.unlink(tmp.name)
    run(client, f"cd {REMOTE_DIR} && rm -rf src && tar -xzf deploy.tar.gz")

    run(client, f"cd {REMOTE_DIR} && docker compose up -d ollama db", timeout=120)
    run(client, f"cd {REMOTE_DIR} && docker compose exec -T ollama ollama list", timeout=60)
    code, _ = run(client, f"cd {REMOTE_DIR} && docker compose exec -T ollama ollama pull {MODEL}", timeout=1800)
    if code != 0:
        print("pull failed")

    code, _ = run(client, f"cd {REMOTE_DIR} && docker compose up --build -d api", timeout=900)
    if code != 0:
        print("build failed", file=sys.stderr)
        return code

    for _ in range(30):
        time.sleep(5)
        _, o = run(client, "curl -sf http://127.0.0.1:8000/health")
        if "healthy" in o:
            break

    _, o = run(
        client,
        'curl -sf -m 90 -X POST http://127.0.0.1:8000/auth/login -H "Content-Type: application/json" '
        '-d \'{"phone":"+79001112233","password":"123456"}\'',
    )
    import json
    try:
        token = json.loads(o).get("access_token", "")
    except Exception:
        token = ""
    if token:
        run(
            client,
            f'curl -sf -m 120 -X POST http://127.0.0.1:8000/assistant/chat '
            f'-H "Authorization: Bearer {token}" -H "Content-Type: application/json" '
            f'-d \'{{"message":"какой кэшбэк на еду?"}}\'',
            timeout=150,
        )
    client.close()
    return 0


if __name__ == "__main__":
    sys.exit(main())
