"""
SafeGuard threat-intelligence API (FastAPI).

Developer-side production readiness features:
- MalwareBazaar integration controlled by TI_MODE
- Redis-compatible caching via CacheStore abstraction (CACHE_BACKEND)
- Rate limiting for POST /v1/verify (429 + Retry-After)
- Structured request logging hooks (request id + timings)

Run: uvicorn main:app --host 127.0.0.1 --port 3000
"""
from __future__ import annotations

import hashlib
import logging
import json
import os
import traceback
import time
import threading

from fastapi import Depends, FastAPI, Header, HTTPException, Request
from fastapi.exceptions import RequestValidationError
from fastapi.responses import JSONResponse, PlainTextResponse
from pydantic import BaseModel, ConfigDict, Field
try:
    from prometheus_fastapi_instrumentator import Instrumentator
except Exception:  # pragma: no cover - optional runtime dependency
    Instrumentator = None
try:
    import sentry_sdk
    from sentry_sdk.integrations.fastapi import FastApiIntegration
except Exception:  # pragma: no cover - optional runtime dependency
    sentry_sdk = None
    FastApiIntegration = None

from auth_routes import router as auth_router

from feed_signer import (
    FeedSigningKey,
    SIGNED_SCHEMA,
    load_signing_key_from_env,
    signed_envelope,
)
from feedback_routes import register_feedback_routes
from malwarebazaar import (
    THREAT_FEED_DEFAULT_LIMIT,
    THREAT_FEED_MAX_LIMIT,
    build_threat_feed_response,
    fetch_recent_apks_with_cache,
    lookup_with_cache,
    map_malwarebazaar_to_verification,
    map_recent_to_feed_items,
)

logger = logging.getLogger(__name__)

TI_MODE = os.environ.get("TI_MODE", "mock").strip().lower()
MB_KEY = os.environ.get("MALWAREBAZAAR_AUTH_KEY", "").strip()
TI_API_BEARER_SECRET = os.environ.get("TI_API_BEARER_SECRET", "").strip()
MOCK_API_KEY = os.environ.get("MOCK_API_KEY", "").strip()
MB_ENABLED = TI_MODE in {"malwarebazaar", "production", "prod"}
PROD_MODE = TI_MODE in {"production", "prod"}
if MB_ENABLED and not MB_KEY:
    raise RuntimeError("TI_MODE=malwarebazaar/production requires MALWAREBAZAAR_AUTH_KEY")

_bearer_auth_required_raw = os.environ.get("TI_API_BEARER_AUTH_REQUIRED", "").strip().lower()
TI_API_BEARER_AUTH_REQUIRED = (
    True
    if _bearer_auth_required_raw in {"1", "true", "yes", "y"}
    else False
    if _bearer_auth_required_raw in {"0", "false", "no", "n", ""}
    else PROD_MODE
)

_expected_auth_secret = TI_API_BEARER_SECRET or MOCK_API_KEY
if TI_API_BEARER_AUTH_REQUIRED and not _expected_auth_secret:
    raise RuntimeError("Bearer auth required but TI_API_BEARER_SECRET/MOCK_API_KEY is missing.")

from cache_store import create_cache_store
from rate_limit import create_rate_limiter

cache_store = create_cache_store()
rate_limiter = create_rate_limiter()

# Threat-feed signing (Phase 3.1). When configured, every threat-feed response is wrapped
# in an Ed25519 signed envelope so on-device clients can detect MITM / cache-poisoning
# even if their TLS chain is compromised. None = signing disabled (legacy/mock mode).
FEED_SIGNING_KEY: FeedSigningKey | None = load_signing_key_from_env()
THREAT_FEED_SIGNING_REQUIRED = (
    os.environ.get("THREAT_FEED_SIGNING_REQUIRED", "").strip().lower()
    in {"1", "true", "yes", "y"}
)
if THREAT_FEED_SIGNING_REQUIRED and FEED_SIGNING_KEY is None:
    raise RuntimeError(
        "THREAT_FEED_SIGNING_REQUIRED=true but THREAT_FEED_SIGNING_PRIVATE_KEY_B64 / "
        "THREAT_FEED_SIGNING_KEY_ID are not configured."
    )

RATE_LIMIT_WINDOW_SECONDS = int(os.environ.get("RATE_LIMIT_WINDOW_SECONDS", "60"))
RATE_LIMIT_MAX_REQUESTS = int(os.environ.get("RATE_LIMIT_MAX_REQUESTS", "30"))
# Threat-feed is a bulk endpoint (one device pulls hundreds of rows in one call) so its
# rate limit is intentionally tighter than /v1/verify. Devices only sync every ~12 hours.
THREAT_FEED_RATE_LIMIT_WINDOW_SECONDS = int(
    os.environ.get("THREAT_FEED_RATE_LIMIT_WINDOW_SECONDS", "3600")
)
THREAT_FEED_RATE_LIMIT_MAX_REQUESTS = int(
    os.environ.get("THREAT_FEED_RATE_LIMIT_MAX_REQUESTS", "12")
)
REQUEST_MAX_SKEW_MS = int(os.environ.get("REQUEST_MAX_SKEW_MS", "300000"))
REPLAY_WINDOW_MS = int(os.environ.get("REPLAY_WINDOW_MS", "600000"))
_freshness_required_raw = os.environ.get("REQUEST_FRESHNESS_REQUIRED", "").strip().lower()
REQUEST_FRESHNESS_REQUIRED = (
    True
    if _freshness_required_raw in {"1", "true", "yes", "y"}
    else False
    if _freshness_required_raw in {"0", "false", "no", "n", ""}
    else PROD_MODE
)

API_VERSION = "1.2.0"

REQUIRE_HTTPS = os.environ.get("REQUIRE_HTTPS", "true").strip().lower() in {"1", "true", "yes", "y"}


def enforce_https(request: Request) -> None:
    """Reject non-HTTPS traffic in provider/production mode.

    Supports reverse proxies by honoring X-Forwarded-Proto.
    """
    if not PROD_MODE or not REQUIRE_HTTPS:
        return

    xf_proto = request.headers.get("X-Forwarded-Proto")
    if xf_proto:
        proto = xf_proto.split(",")[0].strip().lower()
    else:
        proto = request.url.scheme.lower()

    if proto != "https":
        raise HTTPException(status_code=400, detail="HTTPS required")


def _malwarebazaar_auth_key() -> str:
    return MB_KEY


def _request_id(request: Request) -> str:
    rid = request.headers.get("X-Request-Id")
    return rid.strip() if rid else ""


def _hash_key_material(val: str) -> str:
    return hashlib.sha256(val.encode("utf-8")).hexdigest()[:16]


def _rate_limit_key(request: Request) -> str:
    auth = request.headers.get("authorization", "")
    if auth.startswith("Bearer "):
        token = auth[len("Bearer ") :].strip()
        if token:
            return f"tok:{_hash_key_material(token)}"
    ip = request.client.host if request.client else "unknown"
    return f"ip:{_hash_key_material(ip)}"


def rate_limit_verify(request: Request) -> None:
    key = _rate_limit_key(request)
    decision = rate_limiter.limit(
        key,
        window_seconds=RATE_LIMIT_WINDOW_SECONDS,
        max_requests=RATE_LIMIT_MAX_REQUESTS,
    )
    if not decision.allowed:
        raise HTTPException(
            status_code=429,
            detail="Rate limit exceeded",
            headers={"Retry-After": str(decision.retry_after_seconds)},
        )


def rate_limit_threat_feed(request: Request) -> None:
    """Separate bucket from /v1/verify so a hot device sync can't starve interactive lookups."""
    key = "feed:" + _rate_limit_key(request)
    decision = rate_limiter.limit(
        key,
        window_seconds=THREAT_FEED_RATE_LIMIT_WINDOW_SECONDS,
        max_requests=THREAT_FEED_RATE_LIMIT_MAX_REQUESTS,
    )
    if not decision.allowed:
        raise HTTPException(
            status_code=429,
            detail="Rate limit exceeded",
            headers={"Retry-After": str(decision.retry_after_seconds)},
        )


app = FastAPI(
    title="SafeGuard Threat Intel",
    version=API_VERSION,
    description="Layer 6 API for SafeGuard Android (MalwareBazaar via TI_MODE).",
)
app.include_router(auth_router)

# Initialize monitoring (optional in test/dev environments).
if Instrumentator is not None:
    Instrumentator().instrument(app).expose(app, endpoint="/metrics")

SENTRY_DSN = os.environ.get("SENTRY_DSN", "")
if SENTRY_DSN and sentry_sdk is not None and FastApiIntegration is not None:
    sentry_sdk.init(
        dsn=SENTRY_DSN,
        integrations=[FastApiIntegration()],
        environment=os.environ.get("ENVIRONMENT", "production"),
        traces_sample_rate=0.1,
    )


def optional_bearer_auth(authorization: str | None = Header(None)) -> None:
    """Optionally require Authorization: Bearer <secret> (server-side only).

    Controlled by env vars:
    - `TI_API_BEARER_SECRET` (recommended for production)
    - `MOCK_API_KEY` (legacy/development)
    """
    expected = TI_API_BEARER_SECRET or MOCK_API_KEY
    if not expected:
        return
    if not authorization or not authorization.startswith("Bearer "):
        raise HTTPException(status_code=401, detail="Authorization Bearer token required")
    token = authorization[len("Bearer ") :].strip()
    if token != expected:
        raise HTTPException(status_code=403, detail="Invalid token")


# Phase 3.2 — privacy-preserving feedback channel. Registered through a helper so the
# `/v1/feedback` route inherits the same Bearer + HTTPS enforcement as `/v1/verify`. The
# dependency module is kept separate (feedback_routes.py) so its validators can be unit-
# tested without spinning up the verify pipeline / MalwareBazaar deps.
register_feedback_routes(
    app,
    optional_bearer_auth=optional_bearer_auth,
    enforce_https=enforce_https,
)


class LocalLayerScores(BaseModel):
    model_config = ConfigDict(extra="ignore")

    layer2_hash_result: str
    layer3_permission_score: int
    layer4_signature_score: int
    layer5_ml_probability: float


class DeviceMetadata(BaseModel):
    model_config = ConfigDict(extra="ignore")

    android_version: int
    device_locale: str


class VerificationRequest(BaseModel):
    model_config = ConfigDict(populate_by_name=True, extra="ignore")

    apk_hash_sha256: str
    apk_hash_sha512: str
    package_name: str
    version_code: int
    permissions: list[str]
    file_size: int
    target_sdk: int
    signature_fingerprint: str | None = None
    local_layer_scores: LocalLayerScores
    device_metadata: DeviceMetadata
    timestamp: int


class VerificationResponse(BaseModel):
    model_config = ConfigDict(populate_by_name=True)

    verdict: str
    confidence: float
    threat_name: str | None = None
    threat_family: str | None = None
    av_detections: int | None = None
    total_av_scanned: int | None = None
    community_reports: int | None = None
    virustotal_link: str | None = None
    evidence: list[str] | None = None
    recommendation: str | None = None


_replay_lock = threading.Lock()
_seen_requests: dict[str, int] = {}


def _request_fingerprint(body: VerificationRequest) -> str:
    material = "|".join(
        [
            body.apk_hash_sha256.strip().lower(),
            body.apk_hash_sha512.strip().lower(),
            str(body.timestamp),
            body.package_name.strip().lower(),
            str(body.version_code),
        ]
    )
    return hashlib.sha256(material.encode("utf-8")).hexdigest()


def validate_request_freshness_and_replay(body: VerificationRequest) -> None:
    if not REQUEST_FRESHNESS_REQUIRED:
        return
    now_ms = int(time.time() * 1000)
    skew = abs(now_ms - int(body.timestamp))
    if skew > REQUEST_MAX_SKEW_MS:
        raise HTTPException(status_code=400, detail="Stale request timestamp")

    key = _request_fingerprint(body)
    cutoff = now_ms - REPLAY_WINDOW_MS
    with _replay_lock:
        stale = [k for k, ts in _seen_requests.items() if ts < cutoff]
        for k in stale:
            _seen_requests.pop(k, None)
        if key in _seen_requests:
            raise HTTPException(status_code=409, detail="Replay request detected")
        _seen_requests[key] = now_ms


class HealthResponse(BaseModel):
    status: str
    service: str


class VersionResponse(BaseModel):
    api_version: str
    build: str
    notes: str


class ThreatFeedItem(BaseModel):
    """One malware sample exposed to the on-device Layer 2 sync worker."""

    model_config = ConfigDict(populate_by_name=True)

    sha256: str
    sha512: str | None = None
    fuzzy_hash: str
    threat_name: str
    threat_family: str | None = None
    severity: int
    first_seen_ms: int | None = None
    source: str = "malwarebazaar"


class ThreatFeedResponse(BaseModel):
    """
    Cursor-based pagination envelope, returned in two shapes:

    * **Unsigned** (legacy / mock): `items` + `next_cursor_ms` + `has_more` at the top
      level. This is what older client builds without a bundled public key still parse.
    * **Signed** (Phase 3.1): the same payload is serialised with sorted keys, base64ed,
      and wrapped in `{schema, key_id, signed_at_ms, payload_b64, signature_b64}` so
      clients can verify the bytes against a build-time-pinned Ed25519 public key.

    Pydantic sees them as one model with optional fields; the response builder picks the
    shape based on whether [FEED_SIGNING_KEY] is configured.
    """

    model_config = ConfigDict(populate_by_name=True, extra="ignore")

    # Unsigned-shape fields (always present in mock mode; absent in signed mode).
    items: list[ThreatFeedItem] | None = None
    next_cursor_ms: int | None = None
    has_more: bool | None = None

    # Signed-envelope fields (present only when signing is enabled). `schema` collides
    # with Pydantic's BaseModel.schema(); we alias the Python attribute to `schema_` and
    # tell the (de)serialiser to use the un-suffixed name on the wire.
    schema_: str | None = Field(default=None, alias="schema", serialization_alias="schema")
    key_id: str | None = None
    signed_at_ms: int | None = None
    payload_b64: str | None = None
    signature_b64: str | None = None


class ThreatFeedPublicKeyResponse(BaseModel):
    """Public-key advertisement for the on-device verifier. Format is `ed25519` + raw 32 bytes
    base64 (matches the wire format the client expects). [enabled] = false means the server
    is intentionally not signing — useful for mock/dev environments so the client can detect
    "deployment hasn't enabled signing yet" instead of treating it as a config bug."""

    model_config = ConfigDict(populate_by_name=True)

    enabled: bool
    algorithm: str = "ed25519"
    key_id: str | None = None
    public_key_b64: str | None = None


def _mock_response(body: VerificationRequest) -> VerificationResponse:
    return VerificationResponse(
        verdict="UNKNOWN",
        confidence=0.0,
        threat_name=None,
        threat_family=None,
        av_detections=None,
        total_av_scanned=None,
        community_reports=None,
        virustotal_link=None,
        evidence=[
            "FastAPI mock: set MALWAREBAZAAR_AUTH_KEY to enable MalwareBazaar lookups.",
            f"Received sha256 prefix {body.apk_hash_sha256[:16]}…",
        ],
        recommendation="WARN_USER",
    )


@app.exception_handler(HTTPException)
async def http_exception_handler(request: Request, exc: HTTPException):
    # Structured, safe-by-default logs (no secrets, no full payload).
    logger.warning(
        json.dumps(
            {
                "request_id": _request_id(request),
                "method": request.method,
                "path": request.url.path,
                "status_code": exc.status_code,
                "detail": str(exc.detail)[:120],
            }
        )
    )
    # Preserve headers (e.g., Retry-After for 429).
    return JSONResponse(
        status_code=exc.status_code,
        content={"detail": exc.detail},
        headers=exc.headers or None,
    )


@app.exception_handler(RequestValidationError)
async def request_validation_exception_handler(request: Request, exc: RequestValidationError):
    logger.warning(
        json.dumps(
            {
                "request_id": _request_id(request),
                "method": request.method,
                "path": request.url.path,
                "status_code": 400,
                "detail": "Invalid request payload",
            }
        )
    )
    return JSONResponse(status_code=400, content={"detail": "Invalid request payload"})


@app.get("/")
def read_root() -> dict[str, str]:
    """Default welcome route for the SafeGuard Threat Intelligence backend."""
    return {
        "message": "Welcome to SafeGuard Threat Intelligence API",
        "service": "safeguard-ti-api",
        "api_version": API_VERSION,
        "status": "operational",
        "documentation": "/docs"
    }


@app.get("/health", response_model=HealthResponse)
def health_json() -> HealthResponse:
    """JSON health for load balancers / k8s probes (prefer this in production)."""
    name = "safeguard-ti-malwarebazaar" if MB_ENABLED else "safeguard-ti-mock"
    return HealthResponse(status="ok", service=name)


@app.get("/health/legacy", response_class=PlainTextResponse)
def health_plain() -> str:
    """Plain 'ok' for simple curl checks (matches older docs)."""
    return "ok"


@app.get("/v1/version", response_model=VersionResponse)
def version() -> VersionResponse:
    """Build/version metadata — extend with git SHA in real deployments."""
    build = "fastapi-malwarebazaar" if MB_ENABLED else "fastapi-dev-mock"
    notes = "MalwareBazaar get_info enabled (abuse.ch)." if MB_ENABLED else "Mock mode (TI_MODE=mock)."
    return VersionResponse(api_version=API_VERSION, build=build, notes=notes)


@app.post(
    "/v1/verify",
    response_model=VerificationResponse,
    dependencies=[Depends(optional_bearer_auth)],
)
def verify_apk(body: VerificationRequest, request: Request) -> VerificationResponse:
    # Enforce HTTPS in provider/production mode (before rate limiting).
    enforce_https(request)
    # Replay protection and freshness checks.
    validate_request_freshness_and_replay(body)
    # Rate limit: always enforced for verify calls.
    rate_limit_verify(request)

    start = time.perf_counter()
    req_id = _request_id(request)

    sha = body.apk_hash_sha256.strip().lower()
    if len(sha) != 64 or any(c not in "0123456789abcdef" for c in sha):
        resp = VerificationResponse(
            verdict="UNKNOWN",
            confidence=0.25,
            threat_name=None,
            threat_family=None,
            av_detections=None,
            total_av_scanned=None,
            community_reports=None,
            virustotal_link=None,
            evidence=["Invalid SHA256 from client; MalwareBazaar lookup skipped."],
            recommendation="WARN_USER",
        )
        logger.info(
            json.dumps(
                {
                    "request_id": req_id,
                    "method": request.method,
                    "path": request.url.path,
                    "elapsed_ms": int((time.perf_counter() - start) * 1000),
                    "verdict": resp.verdict,
                    "confidence": resp.confidence,
                    "sha256_prefix": sha[:12],
                }
            )
        )
        return resp

    if not MB_ENABLED:
        resp = _mock_response(body)
        logger.info(
            json.dumps(
                {
                    "request_id": req_id,
                    "method": request.method,
                    "path": request.url.path,
                    "elapsed_ms": int((time.perf_counter() - start) * 1000),
                    "verdict": resp.verdict,
                    "confidence": resp.confidence,
                    "mode": "mock",
                }
            )
        )
        return resp

    try:
        mb_json = lookup_with_cache(sha, _malwarebazaar_auth_key(), cache_store)
    except Exception as e:
        logger.warning("MalwareBazaar lookup failed: %s", e)
        logger.debug(traceback.format_exc())
        resp = VerificationResponse(
            verdict="UNKNOWN",
            confidence=0.3,
            threat_name=None,
            threat_family=None,
            av_detections=None,
            total_av_scanned=None,
            community_reports=None,
            virustotal_link=None,
            evidence=[
                f"MalwareBazaar request failed ({type(e).__name__}). Use on-device results.",
                "Provider lookup error suppressed for privacy/safety.",
            ],
            recommendation="WARN_USER",
        )
        logger.info(
            json.dumps(
                {
                    "request_id": req_id,
                    "method": request.method,
                    "path": request.url.path,
                    "elapsed_ms": int((time.perf_counter() - start) * 1000),
                    "verdict": resp.verdict,
                    "confidence": resp.confidence,
                    "mode": "provider_error",
                }
            )
        )
        return resp

    ls = body.local_layer_scores
    mapped = map_malwarebazaar_to_verification(
        sha256=sha,
        package_name=body.package_name,
        mb=mb_json,
        layer3_permission_score=ls.layer3_permission_score,
        layer4_signature_score=ls.layer4_signature_score,
        layer5_ml_probability=ls.layer5_ml_probability,
    )
    resp = VerificationResponse(**mapped)
    logger.info(
        json.dumps(
            {
                "request_id": req_id,
                "method": request.method,
                "path": request.url.path,
                "elapsed_ms": int((time.perf_counter() - start) * 1000),
                "verdict": resp.verdict,
                "confidence": resp.confidence,
                "mode": "malwarebazaar",
            }
        )
    )
    return resp


@app.get("/v1/threat-feed/public-key", response_model=ThreatFeedPublicKeyResponse)
def threat_feed_public_key() -> ThreatFeedPublicKeyResponse:
    """Lets ops verify which signing key the server is using without restarting clients.

    The on-device verifier does NOT trust this endpoint at runtime — keys are pinned at
    *build* time via `local.properties` so a compromised server can't roll out a malicious
    new key. This endpoint is operational metadata only.
    """
    if FEED_SIGNING_KEY is None:
        return ThreatFeedPublicKeyResponse(enabled=False)
    return ThreatFeedPublicKeyResponse(
        enabled=True,
        algorithm="ed25519",
        key_id=FEED_SIGNING_KEY.key_id,
        public_key_b64=FEED_SIGNING_KEY.public_key_b64,
    )


def _maybe_sign_feed(payload: dict) -> dict:
    """Return either the legacy unsigned payload or the Phase 3.1 signed envelope."""
    if FEED_SIGNING_KEY is None:
        return payload
    return signed_envelope(payload, FEED_SIGNING_KEY)


@app.get(
    "/v1/threat-feed",
    response_model=ThreatFeedResponse,
    response_model_by_alias=True,
    response_model_exclude_none=True,
    dependencies=[Depends(optional_bearer_auth)],
)
def threat_feed(
    request: Request,
    since: int | None = None,
    limit: int = THREAT_FEED_DEFAULT_LIMIT,
) -> ThreatFeedResponse:
    """
    Bulk MalwareBazaar APK feed for on-device Layer 2 sync.

    Query parameters:
      - `since`: optional Unix-millisecond cursor returned as `next_cursor_ms` in a previous
        response. Only samples whose `first_seen` is strictly newer than this are returned.
        Pass `null` / omit on first sync.
      - `limit`: max items to return, capped at [THREAT_FEED_MAX_LIMIT].

    In mock mode (no `MALWAREBAZAAR_AUTH_KEY`) we return an empty feed with an advancing
    cursor so device-side smoke tests don't hammer the abuse.ch API and don't get stuck
    re-walking the same window forever.
    """
    enforce_https(request)
    rate_limit_threat_feed(request)

    start = time.perf_counter()
    req_id = _request_id(request)
    requested_limit = max(1, min(int(limit), THREAT_FEED_MAX_LIMIT))

    if since is not None and since < 0:
        raise HTTPException(status_code=400, detail="`since` must be a non-negative epoch in ms")

    if not MB_ENABLED:
        # Empty feed + fresh cursor = device sync becomes a no-op against mock servers.
        empty = build_threat_feed_response([], requested_limit=requested_limit)
        logger.info(
            json.dumps(
                {
                    "request_id": req_id,
                    "method": request.method,
                    "path": request.url.path,
                    "elapsed_ms": int((time.perf_counter() - start) * 1000),
                    "items": 0,
                    "mode": "mock",
                    "signed": FEED_SIGNING_KEY is not None,
                }
            )
        )
        return ThreatFeedResponse(**_maybe_sign_feed(empty))

    try:
        mb_json = fetch_recent_apks_with_cache(
            _malwarebazaar_auth_key(),
            cache_store,
            limit=requested_limit,
        )
    except Exception as e:  # pragma: no cover - exercised via integration tests
        logger.warning("MalwareBazaar feed fetch failed: %s", e)
        logger.debug(traceback.format_exc())
        raise HTTPException(status_code=502, detail="Threat-feed provider unavailable")

    items = map_recent_to_feed_items(mb_json, since_ms=since, limit=requested_limit)
    payload = build_threat_feed_response(items, requested_limit=requested_limit)
    logger.info(
        json.dumps(
            {
                "request_id": req_id,
                "method": request.method,
                "path": request.url.path,
                "elapsed_ms": int((time.perf_counter() - start) * 1000),
                "items": len(items),
                "since_ms": since,
                "next_cursor_ms": payload["next_cursor_ms"],
                "mode": "malwarebazaar",
                "signed": FEED_SIGNING_KEY is not None,
            }
        )
    )
    return ThreatFeedResponse(**_maybe_sign_feed(payload))
