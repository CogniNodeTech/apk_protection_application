# Threat-intelligence API (FastAPI)

Python **FastAPI** implements the Android client’s `POST /v1/verify` JSON contract (see `data/.../VerificationResponse.kt`).

## MalwareBazaar (optional)

When **`TI_MODE=malwarebazaar`** (or `production/prod`) and **`MALWAREBAZAAR_AUTH_KEY`** is set (from [auth.abuse.ch](https://auth.abuse.ch/)), `POST /v1/verify` calls MalwareBazaar **`query=get_info`** for the APK SHA256. The key stays **server-side only**.

| Env | Purpose |
|-----|---------|
| `TI_MODE` | `mock` (default) or `malwarebazaar`/`production` |
| `TI_API_BEARER_AUTH_REQUIRED` | If unset, defaults to `true` when `TI_MODE=production`; if enabled, server refuses to start without a bearer secret. |
| `TI_API_BEARER_SECRET` | If set, requires `Authorization: Bearer <secret>` on `/v1/verify` |
| `MALWAREBAZAAR_AUTH_KEY` | abuse.ch Auth-Key (required for real lookups) |
| `MALWAREBAZAAR_API_URL` | Override API base (default `https://mb-api.abuse.ch/api/v1/`) |
| `MALWAREBAZAAR_CACHE_TTL_SEC` | In-memory cache TTL seconds for `/v1/verify` lookups (default `172800`) |
| `MALWAREBAZAAR_FEED_CACHE_TTL_SEC` | In-memory cache TTL for the bulk APK feed (default `1800` — abuse.ch refreshes hourly) |
| `THREAT_FEED_RATE_LIMIT_WINDOW_SECONDS` | Bulk-feed rate-limit window (default `3600`) |
| `THREAT_FEED_RATE_LIMIT_MAX_REQUESTS` | Bulk-feed max requests per window per client (default `12` — devices sync every ~12 h) |
| `MOCK_API_KEY` | If set, requires `Authorization: Bearer <same>` on `/v1/verify` (matches Android) |

If **`MALWAREBAZAAR_AUTH_KEY`** is unset, `/v1/verify` returns a **mock** `UNKNOWN` response (for local dev without abuse.ch).

**Fair use / commercial:** See [abuse.ch terms](https://abuse.ch/terms-of-use/#principles). For-profit use may require the [enhanced commercial API](https://www.spamhaus.com/data-access/abusech-api/).

**Semantics:** `hash_not_found` means the file is **not** in MalwareBazaar — **not** “benign.” The API returns `UNKNOWN` and blends on-device layer scores into `confidence`.

## Run

```bash
cd server
python -m venv .venv
.venv\Scripts\activate   # Windows
pip install -r requirements.txt

# Optional: Windows PowerShell
$env:MALWAREBAZAAR_AUTH_KEY="your-key-from-auth.abuse.ch"
uvicorn main:app --host 127.0.0.1 --port 3000
```

## Tests

```bash
pip install -r requirements.txt
python -m pytest test_malwarebazaar.py test_threat_feed.py -v
```

## Endpoints

| Method | Path | Purpose |
|--------|------|---------|
| GET | `/health` | JSON `{"status":"ok","service":"..."}` for probes |
| GET | `/health/legacy` | Plain text `ok` (simple curl) |
| GET | `/v1/version` | API version + build label |
| POST | `/v1/verify` | Layer 6 contract (mock or MalwareBazaar-backed) |
| GET | `/v1/threat-feed` | Bulk APK feed for on-device Layer 2 sync (mock-empty or MalwareBazaar `get_file_type`) |

OpenAPI docs: `http://127.0.0.1:3000/docs` (FastAPI default).

### `GET /v1/threat-feed`

Returns recent APK samples from MalwareBazaar projected into the on-device `MalwareSignature` schema. The Android client persists `next_cursor_ms` and passes it back as `since` on the next sync — only samples whose `first_seen` is **strictly newer** than the cursor are shipped, so steady-state syncs are O(new) rather than O(all).

Query parameters:

| Name | Type | Default | Notes |
|------|------|---------|-------|
| `since` | `int?` | `null` | Unix-ms cursor from a previous response. Omit / pass `null` on first sync. |
| `limit` | `int` | `200` | Capped at `1000` (MalwareBazaar `get_file_type` page size). |

Response (`ThreatFeedResponse`):

```json
{
  "items": [
    {
      "sha256": "…",
      "sha512": "…",
      "fuzzy_hash": "<70 hex chars, TLSH-128-1>",
      "threat_name": "Anatsa",
      "threat_family": "Anatsa",
      "severity": 95,
      "first_seen_ms": 1717245296000,
      "source": "malwarebazaar"
    }
  ],
  "next_cursor_ms": 1717245296000,
  "has_more": false
}
```

Filters applied server-side:
- entries without a parseable SHA-256 are dropped (unindexable on the device);
- entries without a TLSH hash are dropped (`fuzzy_hash` is the indexable column on the device side, and SHA-256 lookups are already covered by `/v1/verify`);
- entries older than `since` are dropped.

In **mock mode** (`TI_MODE=mock` / no `MALWAREBAZAAR_AUTH_KEY`) the endpoint returns `items: []` with an advancing `next_cursor_ms` so on-device smoke tests stay no-ops.

## Point the Android app at it

In **`local.properties`** (Gradle project root):

```properties
safeguard.api.base.url=http://10.0.2.2:3000/
```

Use **`10.0.2.2`** from the Android emulator to reach the host machine’s localhost. On a physical device, use your PC’s LAN IP.

**Cleartext:** Debug builds allow HTTP to `localhost` / `10.0.2.2` via `app/src/debug/res/xml/network_security_config.xml`. Release builds require HTTPS.

## Production

Deploy this service behind TLS with secrets management; use **Redis** (or equivalent) instead of in-memory cache at scale. See [`docs/PRODUCTION_READINESS_IMPLEMENTATION_PLAN.md`](../docs/PRODUCTION_READINESS_IMPLEMENTATION_PLAN.md).

## Security/QA helper scripts

The Security/QA team can run:
- `qa_staging_smoke_test.py` — contract smoke checks against staging
- `load_test_verify.py` — concurrency + latency test for `/v1/verify`

See `docs/SECURITY_QA_STAGING_VALIDATION_RUNBOOK.md` for scenarios and evidence capture.

### Redis + rate limiting envs

| Env | Purpose |
|-----|---------|
| `CACHE_BACKEND` | `memory` (dev) or `redis` (prod) |
| `REDIS_URL` | Redis connection string (required if `CACHE_BACKEND=redis` or `RATE_LIMIT_BACKEND=redis`) |
| `REDIS_CACHE_PREFIX` | Key prefix for cache (optional) |
| `RATE_LIMIT_BACKEND` | `memory` (dev) or `redis` (prod) |
| `RATE_LIMIT_WINDOW_SECONDS` | Rate limit window size (default `60`) |
| `RATE_LIMIT_MAX_REQUESTS` | Max requests per window (default `30`) |
| `REDIS_RATE_LIMIT_PREFIX` | Key prefix for rate limiting (optional) |

### TLS / HTTPS enforcement

When running with `TI_MODE=production` or `TI_MODE=prod`, the backend can enforce HTTPS:

| Env | Purpose |
|-----|---------|
| `REQUIRE_HTTPS` | `true` (default) rejects requests unless `X-Forwarded-Proto` is `https` or the request URL scheme is `https` |
