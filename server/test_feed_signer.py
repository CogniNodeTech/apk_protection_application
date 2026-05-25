"""
Tests for the Ed25519-signed threat-feed envelope (Phase 3.1).

We never check in real signing material; every test generates a fresh key in-memory and
threads it through `main.py` via env vars so the production-mode signing path is exercised
end-to-end. The on-device verifier is implemented separately in Kotlin; verification here
uses the pure-Python `verify_envelope` round-trip to catch encoding / canonical bytes
regressions without spinning up an emulator.
"""
from __future__ import annotations

import base64
import importlib
import json

import pytest
from cryptography.exceptions import InvalidSignature
from cryptography.hazmat.primitives.asymmetric.ed25519 import Ed25519PrivateKey
from cryptography.hazmat.primitives.serialization import (
    Encoding,
    PrivateFormat,
    PublicFormat,
    NoEncryption,
)
from fastapi.testclient import TestClient
from unittest.mock import patch

from feed_signer import (
    FeedSigningKey,
    SIGNED_SCHEMA,
    load_signing_key_from_env,
    signed_envelope,
    verify_envelope,
)


def _gen_keypair() -> tuple[str, str]:
    priv = Ed25519PrivateKey.generate()
    raw_priv = priv.private_bytes(
        encoding=Encoding.Raw, format=PrivateFormat.Raw, encryption_algorithm=NoEncryption()
    )
    raw_pub = priv.public_key().public_bytes(encoding=Encoding.Raw, format=PublicFormat.Raw)
    return base64.b64encode(raw_priv).decode("ascii"), base64.b64encode(raw_pub).decode("ascii")


def _entry(sha256: str = "a" * 64, tlsh: str = "T1" + "A" * 70) -> dict:
    return {
        "sha256_hash": sha256,
        "sha512_hash": "b" * 128,
        "tlsh": tlsh,
        "signature": "Anatsa",
        "first_seen": "2024-06-01 12:34:56",
        "tags": ["banker"],
    }


def test_load_signing_key_from_env_disabled_when_unset(monkeypatch: pytest.MonkeyPatch) -> None:
    monkeypatch.delenv("THREAT_FEED_SIGNING_PRIVATE_KEY_B64", raising=False)
    monkeypatch.delenv("THREAT_FEED_SIGNING_KEY_ID", raising=False)
    assert load_signing_key_from_env() is None


def test_load_signing_key_from_env_rejects_partial_config(monkeypatch: pytest.MonkeyPatch) -> None:
    priv_b64, _ = _gen_keypair()
    monkeypatch.setenv("THREAT_FEED_SIGNING_PRIVATE_KEY_B64", priv_b64)
    monkeypatch.delenv("THREAT_FEED_SIGNING_KEY_ID", raising=False)
    with pytest.raises(RuntimeError, match="must both be set"):
        load_signing_key_from_env()


def test_load_signing_key_from_env_rejects_bad_length(monkeypatch: pytest.MonkeyPatch) -> None:
    too_short = base64.b64encode(b"\x00" * 8).decode("ascii")
    monkeypatch.setenv("THREAT_FEED_SIGNING_PRIVATE_KEY_B64", too_short)
    monkeypatch.setenv("THREAT_FEED_SIGNING_KEY_ID", "feed-test")
    with pytest.raises(RuntimeError, match="32-byte raw"):
        load_signing_key_from_env()


def test_load_signing_key_from_env_loads_valid_key(monkeypatch: pytest.MonkeyPatch) -> None:
    priv_b64, pub_b64 = _gen_keypair()
    monkeypatch.setenv("THREAT_FEED_SIGNING_PRIVATE_KEY_B64", priv_b64)
    monkeypatch.setenv("THREAT_FEED_SIGNING_KEY_ID", "feed-test-2026")
    key = load_signing_key_from_env()
    assert key is not None
    assert key.key_id == "feed-test-2026"
    assert key.public_key_b64 == pub_b64


def test_signed_envelope_round_trip_validates() -> None:
    priv_b64, pub_b64 = _gen_keypair()
    raw = base64.b64decode(priv_b64)
    key = FeedSigningKey(
        key_id="feed-test",
        private_key=Ed25519PrivateKey.from_private_bytes(raw),
        public_key_b64=pub_b64,
    )
    payload = {"items": [{"sha256": "a" * 64, "severity": 95}], "next_cursor_ms": 1234, "has_more": False}
    envelope = signed_envelope(payload, key)
    assert envelope["schema"] == SIGNED_SCHEMA
    assert envelope["key_id"] == "feed-test"
    assert envelope["signed_at_ms"] > 0

    decoded = verify_envelope(envelope, pub_b64)
    assert decoded == payload


def test_signed_envelope_rejects_tampered_payload() -> None:
    priv_b64, pub_b64 = _gen_keypair()
    raw = base64.b64decode(priv_b64)
    key = FeedSigningKey(
        key_id="feed-test",
        private_key=Ed25519PrivateKey.from_private_bytes(raw),
        public_key_b64=pub_b64,
    )
    envelope = signed_envelope({"items": [], "next_cursor_ms": 1, "has_more": False}, key)
    tampered_payload_bytes = json.dumps(
        {"items": [{"sha256": "0" * 64}], "next_cursor_ms": 1, "has_more": False},
        sort_keys=True,
        separators=(",", ":"),
    ).encode("utf-8")
    envelope_bad = {**envelope, "payload_b64": base64.b64encode(tampered_payload_bytes).decode("ascii")}
    with pytest.raises(InvalidSignature):
        verify_envelope(envelope_bad, pub_b64)


def test_signed_envelope_rejects_wrong_public_key() -> None:
    priv_b64_a, _ = _gen_keypair()
    _, pub_b64_b = _gen_keypair()  # unrelated keypair
    key = FeedSigningKey(
        key_id="feed-test",
        private_key=Ed25519PrivateKey.from_private_bytes(base64.b64decode(priv_b64_a)),
        public_key_b64=pub_b64_b,  # mismatched on purpose
    )
    envelope = signed_envelope({"items": [], "next_cursor_ms": 1, "has_more": False}, key)
    with pytest.raises(InvalidSignature):
        verify_envelope(envelope, pub_b64_b)


def test_signed_envelope_rejects_missing_signature_field() -> None:
    priv_b64, pub_b64 = _gen_keypair()
    key = FeedSigningKey(
        key_id="feed-test",
        private_key=Ed25519PrivateKey.from_private_bytes(base64.b64decode(priv_b64)),
        public_key_b64=pub_b64,
    )
    envelope = signed_envelope({"items": [], "next_cursor_ms": 1, "has_more": False}, key)
    envelope.pop("signature_b64")
    with pytest.raises(ValueError, match="missing payload_b64 or signature_b64"):
        verify_envelope(envelope, pub_b64)


def test_signed_envelope_rejects_unknown_schema() -> None:
    _, pub_b64 = _gen_keypair()
    bad = {"schema": "v0.unsigned", "payload_b64": "AAAA", "signature_b64": "AAAA"}
    with pytest.raises(ValueError, match="unsupported schema"):
        verify_envelope(bad, pub_b64)


def test_threat_feed_endpoint_returns_unsigned_when_signing_disabled(
    monkeypatch: pytest.MonkeyPatch,
) -> None:
    monkeypatch.setenv("TI_MODE", "mock")
    monkeypatch.delenv("MALWAREBAZAAR_AUTH_KEY", raising=False)
    monkeypatch.setenv("THREAT_FEED_RATE_LIMIT_MAX_REQUESTS", "1000")
    monkeypatch.delenv("THREAT_FEED_SIGNING_PRIVATE_KEY_B64", raising=False)
    monkeypatch.delenv("THREAT_FEED_SIGNING_KEY_ID", raising=False)
    monkeypatch.delenv("THREAT_FEED_SIGNING_REQUIRED", raising=False)

    import main

    importlib.reload(main)
    client = TestClient(main.app)

    r = client.get("/v1/threat-feed")
    assert r.status_code == 200
    body = r.json()
    # Legacy unsigned shape: top-level items / next_cursor_ms / has_more.
    assert "items" in body
    assert "next_cursor_ms" in body
    assert "schema" not in body
    assert "payload_b64" not in body


def test_threat_feed_endpoint_returns_signed_envelope_when_configured(
    monkeypatch: pytest.MonkeyPatch,
) -> None:
    priv_b64, pub_b64 = _gen_keypair()
    monkeypatch.setenv("TI_MODE", "malwarebazaar")
    monkeypatch.setenv("MALWAREBAZAAR_AUTH_KEY", "test-key")
    monkeypatch.setenv("THREAT_FEED_RATE_LIMIT_MAX_REQUESTS", "1000")
    monkeypatch.setenv("THREAT_FEED_SIGNING_PRIVATE_KEY_B64", priv_b64)
    monkeypatch.setenv("THREAT_FEED_SIGNING_KEY_ID", "feed-test-2026")

    import main

    importlib.reload(main)

    fake = {"query_status": "ok", "data": [_entry()]}
    with patch.object(main, "fetch_recent_apks_with_cache", return_value=fake):
        client = TestClient(main.app)
        r = client.get("/v1/threat-feed?limit=200")

    assert r.status_code == 200
    envelope = r.json()
    assert envelope["schema"] == SIGNED_SCHEMA
    assert envelope["key_id"] == "feed-test-2026"
    assert "signed_at_ms" in envelope
    inner = verify_envelope(envelope, pub_b64)
    assert "items" in inner
    assert "next_cursor_ms" in inner
    assert isinstance(inner["items"], list)
    assert len(inner["items"]) == 1


def test_threat_feed_public_key_endpoint_advertises_state(
    monkeypatch: pytest.MonkeyPatch,
) -> None:
    priv_b64, pub_b64 = _gen_keypair()
    monkeypatch.setenv("TI_MODE", "mock")
    monkeypatch.setenv("THREAT_FEED_RATE_LIMIT_MAX_REQUESTS", "1000")
    monkeypatch.setenv("THREAT_FEED_SIGNING_PRIVATE_KEY_B64", priv_b64)
    monkeypatch.setenv("THREAT_FEED_SIGNING_KEY_ID", "feed-pub-test")

    import main

    importlib.reload(main)
    client = TestClient(main.app)

    r = client.get("/v1/threat-feed/public-key")
    assert r.status_code == 200
    body = r.json()
    assert body["enabled"] is True
    assert body["algorithm"] == "ed25519"
    assert body["key_id"] == "feed-pub-test"
    assert body["public_key_b64"] == pub_b64


def test_threat_feed_signing_required_fails_without_key(monkeypatch: pytest.MonkeyPatch) -> None:
    monkeypatch.setenv("TI_MODE", "mock")
    monkeypatch.setenv("THREAT_FEED_SIGNING_REQUIRED", "true")
    monkeypatch.delenv("THREAT_FEED_SIGNING_PRIVATE_KEY_B64", raising=False)
    monkeypatch.delenv("THREAT_FEED_SIGNING_KEY_ID", raising=False)

    import main

    with pytest.raises(RuntimeError, match="THREAT_FEED_SIGNING_REQUIRED"):
        importlib.reload(main)
