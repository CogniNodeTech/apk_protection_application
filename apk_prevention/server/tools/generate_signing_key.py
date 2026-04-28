"""
Generate an Ed25519 keypair for the threat-feed signing system (Phase 3.1).

Usage:
    python tools/generate_signing_key.py [--key-id KEY_ID]

Prints a `.env`-shaped block on stdout: paste the private-key line into the *server*'s
secret store (env var or k8s secret), and the public-key line into the *Android* build's
`local.properties` so the on-device verifier pins it at compile time.

Why we generate fresh material instead of reusing existing org keys: the threat-feed key
is a low-impact, easily-rotatable signing key with one verifier (the bundled Android app).
Co-mingling it with multipurpose org keys (TLS chain, JWT signers, ...) makes rotation a
cross-team event when it should be a one-PR build-time change.
"""
from __future__ import annotations

import argparse
import base64
import datetime as dt
import sys

from cryptography.hazmat.primitives.asymmetric.ed25519 import Ed25519PrivateKey
from cryptography.hazmat.primitives.serialization import (
    Encoding,
    PrivateFormat,
    PublicFormat,
    NoEncryption,
)


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--key-id",
        default=None,
        help="Stable key identifier. Defaults to feed-YYYY-MM (UTC).",
    )
    args = parser.parse_args(argv)

    key_id = args.key_id or f"feed-{dt.datetime.now(tz=dt.timezone.utc):%Y-%m}"

    priv = Ed25519PrivateKey.generate()
    raw_priv = priv.private_bytes(
        encoding=Encoding.Raw, format=PrivateFormat.Raw, encryption_algorithm=NoEncryption()
    )
    raw_pub = priv.public_key().public_bytes(encoding=Encoding.Raw, format=PublicFormat.Raw)

    priv_b64 = base64.b64encode(raw_priv).decode("ascii")
    pub_b64 = base64.b64encode(raw_pub).decode("ascii")

    print("# === SafeGuard threat-feed signing key (Phase 3.1) ===")
    print(f"# Generated: {dt.datetime.now(tz=dt.timezone.utc).isoformat()}")
    print(f"# Algorithm: Ed25519 (RFC 8032 PureEdDSA)")
    print(f"# Key id:    {key_id}")
    print()
    print("# --- Server (env / k8s secret; never commit) ---")
    print(f"THREAT_FEED_SIGNING_KEY_ID={key_id}")
    print(f"THREAT_FEED_SIGNING_PRIVATE_KEY_B64={priv_b64}")
    print()
    print("# --- Android (local.properties; safe to commit per-environment build configs) ---")
    print(f"safeguard.threatfeed.signing.key.id={key_id}")
    print(f"safeguard.threatfeed.signing.public.key.b64={pub_b64}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
