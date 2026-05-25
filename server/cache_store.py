from __future__ import annotations

"""
Cache abstraction for deployment readiness.

Developer goals:
- MalwareBazaar lookup caching must be Redis-compatible without changing callers.
- Cache implementation is selected via env (CACHE_BACKEND).
"""

import json
import os
import threading
import time
from typing import Any, Optional


class CacheStore:
    def get_json(self, key: str) -> Optional[dict[str, Any]]:
        raise NotImplementedError

    def set_json(self, key: str, value: dict[str, Any], ttl_seconds: int) -> None:
        raise NotImplementedError


class MemoryCacheStore(CacheStore):
    """Simple in-process TTL cache.

    Note: not suitable for multi-instance deployments. Use Redis for production.
    """

    def __init__(self, default_ttl_seconds: int = 172800) -> None:
        self._default_ttl = default_ttl_seconds
        self._lock = threading.Lock()
        # key -> (expires_at_monotonic, json_value)
        self._store: dict[str, tuple[float, dict[str, Any]]] = {}

    def get_json(self, key: str) -> Optional[dict[str, Any]]:
        now = time.monotonic()
        with self._lock:
            hit = self._store.get(key)
            if not hit:
                return None
            exp, val = hit
            if exp <= now:
                del self._store[key]
                return None
            return val

    def set_json(self, key: str, value: dict[str, Any], ttl_seconds: int) -> None:
        ttl = ttl_seconds if ttl_seconds and ttl_seconds > 0 else self._default_ttl
        with self._lock:
            self._store[key] = (time.monotonic() + ttl, value)


def _env(name: str, default: str) -> str:
    return os.environ.get(name, default)


def create_cache_store() -> CacheStore:
    backend = _env("CACHE_BACKEND", "memory").strip().lower()
    if backend == "memory":
        return MemoryCacheStore(default_ttl_seconds=int(_env("MALWAREBAZAAR_CACHE_TTL_SEC", "172800")))

    # Redis mode
    if backend != "redis":
        raise RuntimeError(f"Unsupported CACHE_BACKEND: {backend}")

    redis_url = os.environ.get("REDIS_URL", "").strip()
    if not redis_url:
        raise RuntimeError("CACHE_BACKEND=redis requires REDIS_URL")

    # Import lazily so memory-only installs still work.
    from redis import Redis  # type: ignore

    prefix = os.environ.get("REDIS_CACHE_PREFIX", "safeguard:cache").strip()

    class RedisCacheStore(CacheStore):
        def __init__(self) -> None:
            self._client = Redis.from_url(redis_url)

        def _k(self, key: str) -> str:
            return f"{prefix}:{key}"

        def get_json(self, key: str) -> Optional[dict[str, Any]]:
            raw = self._client.get(self._k(key))
            if raw is None:
                return None
            return json.loads(raw)

        def set_json(self, key: str, value: dict[str, Any], ttl_seconds: int) -> None:
            ttl = int(ttl_seconds) if ttl_seconds and ttl_seconds > 0 else 172800
            payload = json.dumps(value, separators=(",", ":"), ensure_ascii=False)
            self._client.set(self._k(key), payload, ex=ttl)

    return RedisCacheStore()

