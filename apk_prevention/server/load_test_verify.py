from __future__ import annotations

"""
Simple load test runner for POST /v1/verify.

Security/QA can use it to measure:
- latency distribution
- 429 rate-limiting correctness
- error rates (4xx/5xx)

This script is dependency-light and should work anywhere Python 3 + httpx is available.
"""

import argparse
import asyncio
import json
import statistics
import time
from datetime import datetime, timezone
from pathlib import Path
from typing import Any

import httpx


def utc_now_iso() -> str:
    return datetime.now(timezone.utc).isoformat()


def build_sample_verify_body() -> dict[str, Any]:
    return {
        "apk_hash_sha256": "e" * 64,
        "apk_hash_sha512": "f" * 128,
        "package_name": "com.test.app",
        "version_code": 1,
        "permissions": ["INTERNET"],
        "file_size": 1000,
        "target_sdk": 34,
        "signature_fingerprint": None,
        "local_layer_scores": {
            "layer2_hash_result": "UNKNOWN",
            "layer3_permission_score": 10,
            "layer4_signature_score": 10,
            "layer5_ml_probability": 0.2,
        },
        "device_metadata": {"android_version": 34, "device_locale": "en-US"},
        "timestamp": 0,
    }


async def run_one(
    client: httpx.AsyncClient,
    base_url: str,
    headers: dict[str, str],
    body: dict[str, Any],
) -> tuple[int, float, str]:
    url = base_url.rstrip("/") + "/v1/verify"
    start = time.perf_counter()
    resp = await client.post(url, json=body, headers=headers, timeout=30)
    elapsed = (time.perf_counter() - start) * 1000.0
    text = ""
    try:
        text = resp.text[:200]
    except Exception:
        text = ""
    return resp.status_code, elapsed, text


async def main_async(args: argparse.Namespace) -> int:
    base_url = args.base_url
    if not base_url.startswith("http://") and not base_url.startswith("https://"):
        raise SystemExit("base-url must start with http:// or https://")

    body = build_sample_verify_body()
    headers = {"X-Request-Id": f"qa-load-{int(time.time())}"}
    if args.bearer_token:
        headers["Authorization"] = f"Bearer {args.bearer_token}"

    out_dir = Path(args.out_dir)
    out_dir.mkdir(parents=True, exist_ok=True)

    latencies: list[float] = []
    status_counts: dict[int, int] = {}
    first_errors: list[str] = []

    sem = asyncio.Semaphore(args.concurrency)

    async with httpx.AsyncClient() as client:

        async def worker() -> None:
            async with sem:
                status, elapsed_ms, text = await run_one(client, base_url, headers, body)
                latencies.append(elapsed_ms)
                status_counts[status] = status_counts.get(status, 0) + 1
                if status >= 400 and len(first_errors) < 5:
                    first_errors.append(f"{status}: {_short(text)}")

        tasks = [asyncio.create_task(worker()) for _ in range(args.requests)]
        await asyncio.gather(*tasks)

    if not latencies:
        raise SystemExit("No requests completed.")

    latencies_sorted = sorted(latencies)
    p50 = latencies_sorted[int(len(latencies_sorted) * 0.50) - 1]
    p95 = latencies_sorted[int(len(latencies_sorted) * 0.95) - 1]
    p99 = latencies_sorted[int(len(latencies_sorted) * 0.99) - 1]
    avg = sum(latencies) / len(latencies)

    summary = {
        "run_started_at": utc_now_iso(),
        "base_url": base_url,
        "requests": args.requests,
        "concurrency": args.concurrency,
        "latency_ms": {
            "avg": avg,
            "p50": p50,
            "p95": p95,
            "p99": p99,
            "min": min(latencies),
            "max": max(latencies),
        },
        "status_counts": status_counts,
        "first_errors": first_errors,
    }

    if args.out_file:
        out_path = out_dir / args.out_file
    else:
        out_path = out_dir / f"load_{int(time.time())}.json"
    out_path.write_text(json.dumps(summary, ensure_ascii=False, indent=2), encoding="utf-8")

    print(f"[OK] load-test summary saved: {out_path}")
    print(json.dumps(summary, ensure_ascii=False))
    return 0


def _short(s: str, n: int = 200) -> str:
    return s[:n] + ("…" if len(s) > n else "")


def parse_args() -> argparse.Namespace:
    p = argparse.ArgumentParser()
    p.add_argument("--base-url", required=True, help="Example: https://staging-api.yourdomain.com/")
    p.add_argument("--requests", type=int, default=200, help="Total requests to send")
    p.add_argument("--concurrency", type=int, default=20, help="Max concurrent in-flight requests")
    p.add_argument("--bearer-token", default="", help="Optional TI_API_BEARER_SECRET (server auth)")
    p.add_argument("--out-dir", default="./qa-load-artifacts")
    p.add_argument("--out-file", default="")
    return p.parse_args()


def main() -> None:
    args = parse_args()
    asyncio.run(main_async(args))


if __name__ == "__main__":
    main()

