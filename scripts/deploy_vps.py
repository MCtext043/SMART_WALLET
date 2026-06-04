#!/usr/bin/env python3
"""Deploy Smart Wallet + Ollama to VPS. Requires DEPLOY_SSH_PASS env var."""
import os
import sys
import tarfile
import tempfile
import time
from pathlib import Path

import paramiko

HOST = os.environ.get("DEPLOY_HOST", "85.198.96.227")
USER = os.environ.get("DEPLOY_USER", "root")
PASSWORD = os.environ.get("DEPLOY_SSH_PASS", "")
REMOTE_DIR = "/opt/smartwallet"
OLLAMA_MODEL = os.environ.get("ASSISTANT_OLLAMA_MODEL", "qwen2.5:1.5b")
ROOT = Path(__file__).resolve().parent.parent
INCLUDE = ["docker-compose.yml", "Dockerfile", ".dockerignore", "pom.xml", "src"]


def run(client, cmd, timeout=900):
    print(f">>> {cmd[:120]}...")
    _, stdout, stderr = client.exec_command(cmd, timeout=timeout)
    out = stdout.read().decode("utf-8", errors="replace")
    err = stderr.read().decode("utf-8", errors="replace")
    code = stdout.channel.recv_exit_status()
    if out.strip():
        print(out[-4000:])
    if code != 0 and err.strip():
        print("ERR:", err[-2000:])
    return code, out


def main():
    if not PASSWORD:
        print("Set DEPLOY_SSH_PASS", file=sys.stderr)
        return 1

    tmp = tempfile.NamedTemporaryFile(suffix=".tar.gz", delete=False)
    tmp.close()
    with tarfile.open(tmp.name, "w:gz") as tar:
        for name in INCLUDE:
            p = ROOT / name
            if p.exists():
                tar.add(p, arcname=name)

    client = paramiko.SSHClient()
    client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    print(f"Connecting to {HOST}...")
    client.connect(HOST, username=USER, password=PASSWORD, timeout=30, allow_agent=False, look_for_keys=False)

    run(client, f"mkdir -p {REMOTE_DIR}")
    sftp = client.open_sftp()
    sftp.put(tmp.name, f"{REMOTE_DIR}/deploy.tar.gz")
    sftp.close()
    os.unlink(tmp.name)

    run(client, f"cd {REMOTE_DIR} && rm -rf src && tar -xzf deploy.tar.gz && rm -f deploy.tar.gz")

    env_content = f"ASSISTANT_OLLAMA_MODEL={OLLAMA_MODEL}\n"
    run(client, f"cat > {REMOTE_DIR}/.env << 'EOF'\n{env_content}EOF")

    run(client, f"cd {REMOTE_DIR} && docker compose pull ollama 2>/dev/null || true")
    run(client, f"cd {REMOTE_DIR} && docker compose up -d ollama db", timeout=300)

    print(f"Pulling Ollama model {OLLAMA_MODEL} (may take several minutes)...")
    code, _ = run(
        client,
        f"cd {REMOTE_DIR} && docker compose exec -T ollama ollama pull {OLLAMA_MODEL}",
        timeout=1800,
    )
    if code != 0:
        print("WARN: model pull failed, trying api anyway", file=sys.stderr)

    run(client, f"cd {REMOTE_DIR} && docker compose up --build -d api", timeout=900)

    print("Waiting for API...")
    for _ in range(40):
        time.sleep(5)
        _, o = run(client, "curl -sf http://127.0.0.1:8000/health || true", timeout=30)
        if '"healthy"' in o:
            print("API healthy")
            break

    print("Testing assistant (warm-up Ollama)...")
    run(
        client,
        r"""bash -lc 'TOKEN=$(curl -sf -X POST http://127.0.0.1:8000/auth/login -H "Content-Type: application/json" -d "{\"phone\":\"+79001112233\",\"password\":\"123456\"}" | python3 -c "import sys,json; print(json.load(sys.stdin).get(\"access_token\",\"\"))" 2>/dev/null); \
if [ -n "$TOKEN" ]; then curl -sf -m 120 -X POST http://127.0.0.1:8000/assistant/chat -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d "{\"message\":\"привет\"}"; else echo skip-login; fi'""",
        timeout=180,
    )

    client.close()
    print(f"\nDone: http://{HOST}:8000/  Ollama model: {OLLAMA_MODEL}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
