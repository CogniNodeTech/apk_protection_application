from __future__ import annotations

"""
Backend rate limiting abstraction for deployment readiness.

Developer goals:
- /v1/verify must be rate-limit-ready and return 429 + Retry-After.
- Implementation must support Redis for multi-instance deployments.
"""

import os
import threading
import time
from typing import Optional


class RateLimitDecision:
    def __init__(self, allowed: bool, retry_after_seconds: int) -> None:
        self.allowed = allowed
        self.retry_after_seconds = retry_after_seconds


class RateLimiter:
    def limit(self, key: str, *, window_seconds: int, max_requests: int) -> RateLimitDecision:
        raise NotImplementedError


class MemoryRateLimiter(RateLimiter):
    def __init__(self) -> None:
        self._lock = threading.Lock()
        # key -> (reset_time_monotonic, count)
        self._store: dict[str, tuple[float, int]] = {}

    def limit(self, key: str, *, window_seconds: int, max_requests: int) -> RateLimitDecision:
        now = time.monotonic()
        with self._lock:
            hit = self._store.get(key)
            if not hit:
                self._store[key] = (now + window_seconds, 1)
                return RateLimitDecision(allowed=True, retry_after_seconds=0)

            reset_at, count = hit
            if now >= reset_at:
                self._store[key] = (now + window_seconds, 1)
                return RateLimitDecision(allowed=True, retry_after_seconds=0)

            if count >= max_requests:
                retry_after = int(max(0, reset_at - now))
                return RateLimitDecision(allowed=False, retry_after_seconds=retry_after)

            self._store[key] = (reset_at, count + 1)
            return RateLimitDecision(allowed=True, retry_after_seconds=0)


def _env(name: str, default: str) -> str:
    return os.environ.get(name, default)


def create_rate_limiter() -> RateLimiter:
    backend = _env("RATE_LIMIT_BACKEND", "memory").strip().lower()
    if backend == "memory":
        return MemoryRateLimiter()

    if backend != "redis":
        raise RuntimeError(f"Unsupported RATE_LIMIT_BACKEND: {backend}")

    redis_url = os.environ.get("REDIS_URL", "").strip()
    if not redis_url:
        raise RuntimeError("RATE_LIMIT_BACKEND=redis requires REDIS_URL")

    from redis import Redis  # type: ignore

    prefix = os.environ.get("REDIS_RATE_LIMIT_PREFIX", "safeguard:rate").strip()

    class RedisRateLimiter(RateLimiter):
        def __init__(self) -> None:
            self._client = Redis.from_url(redis_url)

        def _k(self, key: str) -> str:
            return f"{prefix}:{key}"

        def limit(self, key: str, *, window_seconds: int, max_requests: int) -> RateLimitDecision:
            redis_key = self._k(key)
            # INCR + set expiry if first hit in window
            count = self._client.incr(redis_key)
            if count == 1:
                self._client.expire(redis_key, window_seconds)
            if count > max_requests:
                ttl = self._client.ttl(redis_key)
                retry_after = int(ttl) if ttl and ttl > 0 else window_seconds
                return RateLimitDecision(allowed=False, retry_after_seconds=retry_after)
            return RateLimitDecision(allowed=True, retry_after_seconds=0)

    return RedisRateLimiter()

