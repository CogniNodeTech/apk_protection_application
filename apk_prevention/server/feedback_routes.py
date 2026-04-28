"""
Privacy-preserving scan feedback endpoint for SafeGuard Android (Phase 3.2).

The mobile client uploads opt-in scan metadata so we can:
  * tune layer thresholds against real-world scans (not just synthesised lab traffic),
  * track false-positive trends across builds (`client_app_version_code`),
  * and feed an evolving evaluation corpus for the Phase 3.3 benchmark harness.

**Privacy contract (server-side, must remain true forever):**
  * Pydantic `extra="forbid"` on every model — a future client that tries to ship a path
    or filename gets a 422 instead of silently leaking PII into our DB.
  * No request body is logged at INFO level. Dev-mode log sink (FEEDBACK_DEV_LOG=1) emits
    a structured DEBUG line with hashes only, never the original event objects.
  * Verdict / package strings are length-capped (defence-in-depth against a misbehaving
    client trying to use this as a generic exfil channel).
  * The persistence layer is intentionally pluggable (FEEDBACK_SINK env var) and defaults
    to an in-memory ring buffer in dev so accidental misconfiguration doesn't ship raw
    events to a production DB.
"""
from __future__ import annotations

import hashlib
import json
import logging
import os
import re
import threading
import time
from collections import deque
from typing import Any, Deque

from fastapi import APIRouter, Depends, HTTPException, Request
from pydantic import BaseModel, ConfigDict, Field, field_validator

logger = logging.getLogger(__name__)

# Server-side caps. Generous on layers (we may add layers in future) but tight on
# free-form strings to keep this from being abusable as a side-channel.
MAX_EVENTS_PER_BATCH = int(os.environ.get("FEEDBACK_MAX_BATCH", "200"))
MAX_VERDICT_LEN = 32
MAX_PACKAGE_LEN = 256
MAX_RULE_NAME_LEN = 128
MAX_RULES_PER_EVENT = 32
MAX_LAYER_KEY_LEN = 32
MAX_LAYERS_PER_EVENT = 32
MAX_TIMESTAMP_SKEW_MS = int(os.environ.get("FEEDBACK_MAX_SKEW_MS", "172800000"))  # 48h

# Allowed verdicts mirror the on-device `Verdict` enum exactly. Anything else suggests a
# misconfigured client and should round-trip as a 422 rather than be silently coerced.
ALLOWED_VERDICTS = frozenset({"SAFE", "SUSPICIOUS", "MALICIOUS", "UNKNOWN"})

_SHA256_RE = re.compile(r"^[0-9a-f]{64}$")
# Android packages: java identifiers separated by '.'. We keep the regex permissive (no
# leading-digit / reserved-word checks) — those are the SDK's job, we just want to keep
# weird control characters and excess length out of our DB.
_PACKAGE_RE = re.compile(r"^[A-Za-z0-9_.]+$")
_LAYER_KEY_RE = re.compile(r"^[A-Za-z0-9_]+$")
# Rule names come from YARA / our own bundled rules. ASCII is fine; we strip the rest.
_RULE_NAME_RE = re.compile(r"^[A-Za-z0-9_.\-]+$")


class FeedbackEvent(BaseModel):
    """One scan feedback event. Mirrors the Kotlin DTO field-for-field with `extra="forbid"`
    so the server *cannot* accept a payload containing fields the client shouldn't send."""

    model_config = ConfigDict(extra="forbid")

    id: str = Field(..., min_length=1, max_length=128)
    created_at_ms: int = Field(..., ge=0)
    sha256: str
    verdict: str
    confidence: float = Field(..., ge=0.0, le=1.0)
    package_name: str | None = None
    version_code: int | None = Field(default=None, ge=0)
    layer_scores: dict[str, float]
    triggered_rules: list[str]

    @field_validator("sha256")
    @classmethod
    def _sha256_lowercase_hex(cls, v: str) -> str:
        v = v.strip().lower()
        if not _SHA256_RE.fullmatch(v):
            raise ValueError("sha256 must be 64 lowercase hex chars")
        return v

    @field_validator("verdict")
    @classmethod
    def _known_verdict(cls, v: str) -> str:
        if len(v) > MAX_VERDICT_LEN or v not in ALLOWED_VERDICTS:
            raise ValueError(f"verdict must be one of {sorted(ALLOWED_VERDICTS)}")
        return v

    @field_validator("package_name")
    @classmethod
    def _safe_package(cls, v: str | None) -> str | None:
        if v is None:
            return None
        v = v.strip()
        if not v:
            return None
        if len(v) > MAX_PACKAGE_LEN or not _PACKAGE_RE.fullmatch(v):
            raise ValueError("package_name must be a valid Android package identifier")
        return v

    @field_validator("layer_scores")
    @classmethod
    def _layer_scores_bounded(cls, v: dict[str, float]) -> dict[str, float]:
        if len(v) > MAX_LAYERS_PER_EVENT:
            raise ValueError(f"too many layer_scores; max {MAX_LAYERS_PER_EVENT}")
        for key, score in v.items():
            if len(key) > MAX_LAYER_KEY_LEN or not _LAYER_KEY_RE.fullmatch(key):
                raise ValueError(f"layer_scores key '{key[:32]}…' is invalid")
            if not (0.0 <= score <= 1.0):
                raise ValueError(f"layer_scores['{key}'] must be in [0, 1]; got {score}")
        return v

    @field_validator("triggered_rules")
    @classmethod
    def _rules_bounded(cls, v: list[str]) -> list[str]:
        if len(v) > MAX_RULES_PER_EVENT:
            raise ValueError(f"too many triggered_rules; max {MAX_RULES_PER_EVENT}")
        for rule in v:
            if len(rule) > MAX_RULE_NAME_LEN or not _RULE_NAME_RE.fullmatch(rule):
                raise ValueError(f"triggered_rules contains an invalid name '{rule[:32]}…'")
        return v


class FeedbackUploadRequest(BaseModel):
    model_config = ConfigDict(extra="forbid")

    events: list[FeedbackEvent] = Field(..., min_length=1)
    client_app_version_code: int = Field(..., ge=0)
    client_android_api_level: int = Field(..., ge=1)
    uploaded_at_ms: int = Field(..., ge=0)

    @field_validator("events")
    @classmethod
    def _bounded_batch(cls, v: list[FeedbackEvent]) -> list[FeedbackEvent]:
        if len(v) > MAX_EVENTS_PER_BATCH:
            raise ValueError(f"events batch exceeds server limit {MAX_EVENTS_PER_BATCH}")
        return v

    @field_validator("uploaded_at_ms")
    @classmethod
    def _within_skew(cls, v: int) -> int:
        # Reject obviously-skewed clients to avoid corpus-poisoning via wall-clock spoof.
        # We don't fail the whole batch over individual `created_at_ms` since users can
        # accumulate events before the device gets back online.
        now_ms = int(time.time() * 1000)
        if abs(now_ms - v) > MAX_TIMESTAMP_SKEW_MS:
            raise ValueError("uploaded_at_ms outside acceptable skew window")
        return v


class FeedbackUploadResponse(BaseModel):
    """Payload echoed back to the client. `accepted_ids` lets the device delete only the
    rows the server actually persisted (older builds without partial-success need to
    fall back to "if 200, the whole batch is accepted")."""

    model_config = ConfigDict(populate_by_name=True)

    accepted_ids: list[str]
    rejected_count: int = 0


class FeedbackSink:
    """Pluggable sink for accepted feedback events. Real deployments back this with a
    queue (SQS / Pub/Sub) or a write-only analytics DB; dev defaults to a bounded ring
    buffer that keeps the last N events for inspection without ever hitting disk."""

    def __init__(self, capacity: int = 1000) -> None:
        self._lock = threading.Lock()
        self._buf: Deque[dict[str, Any]] = deque(maxlen=capacity)

    def write(self, event_dict: dict[str, Any]) -> None:
        with self._lock:
            self._buf.append(event_dict)

    def snapshot(self) -> list[dict[str, Any]]:
        with self._lock:
            return list(self._buf)

    def clear(self) -> None:
        with self._lock:
            self._buf.clear()


_sink_singleton: FeedbackSink | None = None
_sink_lock = threading.Lock()


def get_feedback_sink() -> FeedbackSink:
    """Lazy-initialised singleton so tests can monkeypatch via `app.dependency_overrides`."""
    global _sink_singleton
    with _sink_lock:
        if _sink_singleton is None:
            cap = int(os.environ.get("FEEDBACK_SINK_CAPACITY", "1000"))
            _sink_singleton = FeedbackSink(capacity=cap)
        return _sink_singleton


def _dev_log_event(event: FeedbackEvent, request_id: str) -> None:
    """Hash-only DEBUG log: useful for spotting throughput regressions without ever
    putting raw events on disk. Emits at most one line per event so the sink is the
    durable record."""
    if not logger.isEnabledFor(logging.DEBUG):
        return
    digest = hashlib.sha256(event.id.encode("utf-8")).hexdigest()[:16]
    logger.debug(
        json.dumps(
            {
                "request_id": request_id,
                "event_id_hash": digest,
                "verdict": event.verdict,
                "confidence": round(event.confidence, 3),
                "layers": len(event.layer_scores),
                "rules": len(event.triggered_rules),
                "sha256_prefix": event.sha256[:12],
            }
        )
    )


router = APIRouter(prefix="/v1", tags=["feedback"])


# Optional auth dep is plumbed in by main.py via `dependencies=[Depends(optional_bearer_auth)]`
# at registration time so we don't double-import the function here.
def _request_id(request: Request) -> str:
    rid = request.headers.get("X-Request-Id")
    return rid.strip() if rid else ""


def register_feedback_routes(app, *, optional_bearer_auth, enforce_https) -> None:
    """Mounts the feedback router under the FastAPI app, wiring in the host's auth +
    HTTPS dependencies. Exposed as a function (not a top-level `app.include_router`) so
    `main.py` controls the dependency stack and we don't accidentally bypass auth in
    isolated tests of this module."""

    @router.post(
        "/feedback",
        response_model=FeedbackUploadResponse,
        dependencies=[Depends(optional_bearer_auth)],
    )
    def upload_feedback(
        body: FeedbackUploadRequest,
        request: Request,
        sink: FeedbackSink = Depends(get_feedback_sink),
    ) -> FeedbackUploadResponse:
        enforce_https(request)
        start = time.perf_counter()
        req_id = _request_id(request)

        accepted: list[str] = []
        rejected = 0
        for event in body.events:
            # Per-event `created_at_ms` must not be in the future relative to the upload.
            # We tolerate stale events (offline device) but reject "from-the-future" events
            # that suggest a clock-spoof attack on the corpus.
            if event.created_at_ms > body.uploaded_at_ms + 5000:
                rejected += 1
                continue
            try:
                sink.write(
                    {
                        # Server adds metadata the client cannot influence.
                        "received_at_ms": int(time.time() * 1000),
                        "client_app_version_code": body.client_app_version_code,
                        "client_android_api_level": body.client_android_api_level,
                        # Then the validated event payload.
                        "event": event.model_dump(),
                    }
                )
            except Exception as e:  # pragma: no cover - sink failure path
                logger.warning("Feedback sink write failed: %s", e)
                rejected += 1
                continue
            accepted.append(event.id)
            _dev_log_event(event, req_id)

        if not accepted and rejected > 0:
            # Whole batch rejected (clock-skew attack / sink down). Treat as a 4xx so the
            # client doesn't retry-loop forever — its events will be re-enqueued on the
            # next opt-in scan if the issue clears up.
            raise HTTPException(status_code=400, detail="all events rejected")

        logger.info(
            json.dumps(
                {
                    "request_id": req_id,
                    "method": request.method,
                    "path": request.url.path,
                    "accepted": len(accepted),
                    "rejected": rejected,
                    "elapsed_ms": int((time.perf_counter() - start) * 1000),
                    "client_app_version_code": body.client_app_version_code,
                }
            )
        )
        return FeedbackUploadResponse(accepted_ids=accepted, rejected_count=rejected)

    app.include_router(router)


__all__ = [
    "ALLOWED_VERDICTS",
    "FeedbackEvent",
    "FeedbackSink",
    "FeedbackUploadRequest",
    "FeedbackUploadResponse",
    "MAX_EVENTS_PER_BATCH",
    "MAX_RULES_PER_EVENT",
    "MAX_LAYERS_PER_EVENT",
    "get_feedback_sink",
    "register_feedback_routes",
    "router",
]
