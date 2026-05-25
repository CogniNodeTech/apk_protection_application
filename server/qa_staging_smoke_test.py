from __future__ import annotations

"""
Automated staging smoke / contract checks for SafeGuard Layer 6 backend.

This script is meant for Security/QA execution.
It saves evidence JSON to --out-dir and prints a short summary.
"""

import argparse
import json
import os
from datetime import datetime, timezone
from pathlib import Path
from typing import Any

import httpx


def _now_iso() -> str:
    return datetime.now(timezone.utc).isoformat()


def _safe_truncate(s: str, n: int = 200) -> str:
    return s[:n] + ("…" if len(s) > n else "")


def build_sample_verify_body() -> dict[str, Any]:
    # Minimal, schema-valid sample matching server/main.py models.
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


def assert_response_json_keys(data: Any, keys: list[str]) -> None:
    if not isinstance(data, dict):
        raise AssertionError(f"Expected JSON object but got: {type(data).__name__}")
    missing = [k for k in keys if k not in data]
    if missing:
        raise AssertionError(f"Missing keys: {missing}")


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--base-url", required=True, help="Example: https://staging-api.yourdomain.com/")
    parser.add_argument("--bearer-token", default="", help="Optional TI_API_BEARER_SECRET")
    parser.add_argument("--out-dir", default="./qa-artifacts")
    args = parser.parse_args()

    base_url = args.base_url.rstrip("/") + "/"
    out_dir = Path(args.out_dir)
    out_dir.mkdir(parents=True, exist_ok=True)
    run_id = _now_iso().replace(":", "-")

    headers = {"X-Request-Id": f"qa-{run_id}"}
    if args.bearer_token:
        headers["Authorization"] = f"Bearer {args.bearer_token}"

    evidence: dict[str, Any] = {"run_id": run_id, "base_url": base_url, "started_at": _now_iso()}

    def request_json(method: str, path: str, *, json_body: Any | None = None) -> httpx.Response:
        url = base_url + path.lstrip("/")
        resp = httpx.request(method, url, headers=headers, json=json_body, timeout=25)
        return resp

    # /health
    resp = request_json("GET", "/health")
    evidence["health"] = {"status_code": resp.status_code, "body": resp.text}
    resp.raise_for_status()
    health = resp.json()
    assert_response_json_keys(health, ["status", "service"])

    # /v1/version
    resp = request_json("GET", "/v1/version")
    evidence["version"] = {"status_code": resp.status_code, "body": resp.text}
    resp.raise_for_status()
    ver = resp.json()
    assert_response_json_keys(ver, ["api_version", "build", "notes"])

    # invalid payload => 400
    resp = request_json("POST", "/v1/verify", json_body={})
    evidence["invalid_payload"] = {"status_code": resp.status_code, "body": resp.text}
    if resp.status_code != 400:
        raise AssertionError(f"Expected 400 for invalid payload, got {resp.status_code}")

    # valid payload => 200 or 429 (rate limit) or 503 (provider unavailable)
    body = build_sample_verify_body()
    resp = request_json("POST", "/v1/verify", json_body=body)
    evidence["valid_payload"] = {"status_code": resp.status_code, "body": resp.text}

    if resp.status_code == 429:
        retry_after = resp.headers.get("Retry-After")
        if not retry_after:
            raise AssertionError("Expected Retry-After header on 429")
        # JSON should still exist
        data = resp.json()
        assert_response_json_keys(data, ["detail"])
    elif resp.status_code == 200:
        data = resp.json()
        assert_response_json_keys(
            data,
            ["verdict", "confidence", "threat_name", "threat_family", "av_detections", "total_av_scanned", "community_reports", "virustotal_link", "evidence", "recommendation"],
        )
    elif resp.status_code == 503:
        # Provider unavailable should be safe and not crash the contract.
        data = resp.json()
        assert_response_json_keys(data, ["detail"])
    else:
        raise AssertionError(f"Unexpected status code for valid payload: {resp.status_code}")

    evidence["finished_at"] = _now_iso()
    out_file = out_dir / f"qa_smoke_{run_id}.json"
    out_file.write_text(json.dumps(evidence, ensure_ascii=False, indent=2), encoding="utf-8")

    print(f"[OK] Saved evidence: {out_file}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

