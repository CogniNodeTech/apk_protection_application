"""
Ed25519 signing for the bulk threat-feed (Phase 3.1).

The bulk feed is the on-device Layer 2 sync's "source of truth" for malware signatures.
Without authentication, anyone who can MITM the device's HTTPS connection (compromised
proxy, rogue CA, captive portal injection) could ship malicious or empty rows directly
into the device's malware DB. TLS alone is not enough — the threat model includes a
trusted CA being compromised or the user being on a managed network with a legitimately
issued interception cert.

So we wrap the response in an Ed25519 signed envelope whose inner bytes ARE the legacy
unsigned JSON that the older clients still parse. New clients verify the signature with a
public key bundled at build time; older clients fall back to the unsigned shape.

Ed25519 is preferred over RSA-PSS / ECDSA because:
  - 64-byte signatures (cheap to ship in the cursor-sized batches we send),
  - Deterministic (no nonce reuse foot-guns on a server that might be horizontally scaled),
  - Constant-time by construction in `cryptography`'s implementation.

Key material is provided via env vars so the signing key never sits on disk in checked-in
config:
  - `THREAT_FEED_SIGNING_PRIVATE_KEY_B64`: base64-encoded raw 32-byte Ed25519 private key.
  - `THREAT_FEED_SIGNING_KEY_ID`: short stable identifier (e.g. "feed-2026-04") so we can
    rotate keys without breaking older client builds — the client picks the key it knows.

Generate a fresh keypair with `tools/generate_signing_key.py`.
"""
from __future__ import annotations

import base64
import json
import logging
import os
import time
from dataclasses import dataclass
from typing import Any, Mapping

try:
    from cryptography.hazmat.primitives.asymmetric.ed25519 import (
        Ed25519PrivateKey,
        Ed25519PublicKey,
    )
    from cryptography.hazmat.primitives.serialization import (
        Encoding,
        PrivateFormat,
        PublicFormat,
        NoEncryption,
    )
except Exception as e:  # pragma: no cover - cryptography is a hard requirement when signing
    Ed25519PrivateKey = None  # type: ignore[assignment]
    Ed25519PublicKey = None  # type: ignore[assignment]
    _IMPORT_ERROR: Exception | None = e
else:
    _IMPORT_ERROR = None

logger = logging.getLogger(__name__)

SIGNED_SCHEMA = "v1.signed"
ED25519_RAW_PRIV_LEN = 32
ED25519_RAW_PUB_LEN = 32
ED25519_SIG_LEN = 64


@dataclass(frozen=True)
class FeedSigningKey:
    """Loaded Ed25519 private key + public-key projection for client distribution."""

    key_id: str
    private_key: Any  # Ed25519PrivateKey when cryptography is installed.
    public_key_b64: str

    def sign(self, payload_bytes: bytes) -> str:
        sig = self.private_key.sign(payload_bytes)
        return base64.b64encode(sig).decode("ascii")


def load_signing_key_from_env() -> FeedSigningKey | None:
    """Return the configured signing key, or `None` if signing is not configured.

    Signing is OFF by default. Mock/dev environments stay functionally identical to the
    pre-3.1 behaviour. Production deployments should set both env vars; if only one is
    set we fail loud — partial config is almost always a misconfigured rotation.
    """
    raw_key_b64 = (os.environ.get("THREAT_FEED_SIGNING_PRIVATE_KEY_B64") or "").strip()
    key_id = (os.environ.get("THREAT_FEED_SIGNING_KEY_ID") or "").strip()
    if not raw_key_b64 and not key_id:
        return None
    if not raw_key_b64 or not key_id:
        raise RuntimeError(
            "THREAT_FEED_SIGNING_PRIVATE_KEY_B64 and THREAT_FEED_SIGNING_KEY_ID must "
            "both be set (or both unset). Refusing to start with partial signing config."
        )
    if Ed25519PrivateKey is None:
        raise RuntimeError(
            "Threat-feed signing requested but the `cryptography` package is not "
            f"importable: {_IMPORT_ERROR!r}. Install requirements.txt to enable signing."
        )

    try:
        raw = base64.b64decode(raw_key_b64, validate=True)
    except Exception as e:
        raise RuntimeError(f"Invalid base64 in THREAT_FEED_SIGNING_PRIVATE_KEY_B64: {e}") from e
    if len(raw) != ED25519_RAW_PRIV_LEN:
        raise RuntimeError(
            "THREAT_FEED_SIGNING_PRIVATE_KEY_B64 must decode to a 32-byte raw Ed25519 "
            f"seed; got {len(raw)} bytes."
        )

    private_key = Ed25519PrivateKey.from_private_bytes(raw)
    public_bytes = private_key.public_key().public_bytes(
        encoding=Encoding.Raw, format=PublicFormat.Raw
    )
    public_key_b64 = base64.b64encode(public_bytes).decode("ascii")
    logger.info("Threat-feed signing enabled (key_id=%s)", key_id)
    return FeedSigningKey(key_id=key_id, private_key=private_key, public_key_b64=public_key_b64)


def signed_envelope(payload: Mapping[str, Any], key: FeedSigningKey) -> dict[str, Any]:
    """Wrap [payload] in the Phase 3.1 signed envelope.

    The inner [payload] is serialised once with `sort_keys=True` and `separators=(",", ":")`
    so the bytes are reproducible and free of float/whitespace ambiguity. The client
    receives the *exact* bytes (base64-encoded) it must verify, then parses them — no
    canonical-JSON contract has to live on the device, only base64-decode + Ed25519-verify.
    """
    inner_bytes = json.dumps(payload, sort_keys=True, separators=(",", ":")).encode("utf-8")
    payload_b64 = base64.b64encode(inner_bytes).decode("ascii")
    signature_b64 = key.sign(inner_bytes)
    return {
        "schema": SIGNED_SCHEMA,
        "key_id": key.key_id,
        "signed_at_ms": int(time.time() * 1000),
        "payload_b64": payload_b64,
        "signature_b64": signature_b64,
    }


def verify_envelope(envelope: Mapping[str, Any], public_key_b64: str) -> Mapping[str, Any]:
    """Verify a signed envelope and return the decoded inner payload.

    Used in tests and by the (Python) staging smoke harness so we don't need to spin up
    the Android client just to confirm a deployment is signing correctly. Mirrors the
    on-device Kotlin verifier; keep them in sync.
    """
    if Ed25519PublicKey is None:  # pragma: no cover - same gate as signing
        raise RuntimeError("cryptography not importable; cannot verify Ed25519 signatures.")
    if envelope.get("schema") != SIGNED_SCHEMA:
        raise ValueError(f"unsupported schema: {envelope.get('schema')!r}")
    payload_b64 = envelope.get("payload_b64") or ""
    signature_b64 = envelope.get("signature_b64") or ""
    if not payload_b64 or not signature_b64:
        raise ValueError("envelope is missing payload_b64 or signature_b64")

    try:
        payload_bytes = base64.b64decode(payload_b64, validate=True)
        signature_bytes = base64.b64decode(signature_b64, validate=True)
        public_bytes = base64.b64decode(public_key_b64, validate=True)
    except Exception as e:
        raise ValueError(f"invalid base64 in envelope: {e}") from e

    if len(public_bytes) != ED25519_RAW_PUB_LEN:
        raise ValueError("public key must be 32 raw bytes")
    if len(signature_bytes) != ED25519_SIG_LEN:
        raise ValueError("signature must be 64 raw bytes")

    public_key = Ed25519PublicKey.from_public_bytes(public_bytes)
    public_key.verify(signature_bytes, payload_bytes)  # raises InvalidSignature on mismatch
    return json.loads(payload_bytes.decode("utf-8"))


__all__ = [
    "ED25519_RAW_PRIV_LEN",
    "ED25519_RAW_PUB_LEN",
    "ED25519_SIG_LEN",
    "FeedSigningKey",
    "SIGNED_SCHEMA",
    "load_signing_key_from_env",
    "signed_envelope",
    "verify_envelope",
]
