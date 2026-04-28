# AEGISNODE – Hybrid AI-Driven APK Protection

Production-oriented Android app that protects users from malicious APK files using a **zero-trust**, multi-layered security model. Built for elderly and non-technical users (WCAG AAA–oriented UI).

## Features

- **7 independent protection layers** (file monitor, hash DB + SHA-512 collision check, permissions, signature, ML, cloud, YARA-style content rules)
- **Zero-trust decision engine**: no single point of trust; consensus-based verdicts
- **Offline-first**: local scanning and hash DB; cloud used when online
- **Elderly-friendly UI**: 18sp+ fonts, high contrast, large touch targets
- **Quarantine**: automatic isolation of detected threats with 30-day auto-delete
- **No accessibility service** (Play Store compliant)
- **Recursive real-time monitoring** of shared storage, including chat-app folders (WhatsApp, Telegram, etc.) when **All files access** is granted
- **Magic-byte APK detection** — catches Android packages disguised under non-`.apk` filenames (`update.zip`, `photo.dat`, no extension at all) by reading the ZIP header and verifying the `AndroidManifest.xml` entry
- **Install + update interception** — scans both fresh installs (`PACKAGE_ADDED`) and weaponized updates (`PACKAGE_REPLACED`)
- **Periodic deep sweep** — every ~6 hours a backstop worker re-walks shared storage to catch APKs the realtime observer missed (service was killed, watch cap reached, files predated install)
- **TLSH fuzzy hashing** for variant detection — Layer 2 now uses the published [Trend Locality Sensitive Hash](https://github.com/trendmicro/tlsh) to flag repacked / resigned / minor-recompile variants of known malware, instead of the previous block-of-SHA-256 pseudo-fuzzy that produced no real similarity signal
- **Live threat-intel feed sync** — every ~12 hours a `WorkManager` job pulls fresh malware rows (SHA-256 + SHA-512 + TLSH + severity) from the SafeGuard server's `GET /v1/threat-feed` endpoint and upserts them into Room, so Layers 2/6 keep matching against current campaigns instead of a stale install-time bundle. Cursor-based, idempotent, network/battery-constrained, gated by the user's "cloud verification" preference
- **YARA-style content rules (Layer 7)** — a pure-Kotlin engine matches a curated, bundled `*.yar` ruleset (Anatsa, Hydra, GodFather, accessibility-overlay banker, generic dropper, BadPack ZIP-tamper, Frida, AVS evasion) against `classes*.dex`, `AndroidManifest.xml`, and small native libs. Catches recompiled / resigned variants whose hashes, certs, permissions, and ML signal all look clean. Supports `ascii` / `wide` / `nocase` literals, hex with `??` wildcards, and `any/all/N of them` + `and/or/not` conditions — strict subset, parser refuses anything outside it (no silent shadowing of unsupported features)
- **Threat-feed observability** — the dashboard now surfaces a "Threat database" tile derived from `ThreatFeedRepository.observeStatus()`. Three colour states: *green* (synced inside 48 h), *amber* (stale or last attempt failed but a recent success exists), *red* (never synced or every attempt has failed). The repository persists `lastSuccessMs`, `lastAttemptMs`, last outcome (`SUCCESS` / `SKIPPED` / `FAILED`), failure reason, and last inserted row count atomically per worker run, so a transient network blip can no longer wipe the success indicator the user relies on
- **SHA-512 collision detection (Layer 2)** — the hash validator now computes SHA-256 *and* SHA-512 in a single I/O pass. When a SHA-256 hits a known-malware row, the local SHA-512 is cross-checked against the row's stored SHA-512: a *match* surfaces explicit "SHA-512 confirmed (collision-resistant match)" evidence (important for legal/IR), a *mismatch* downgrades the verdict from MALICIOUS to SUSPICIOUS with `isCollision=true` and clears the threat name (the matched row could be tampered with, or this could be a real SHA-256 collision — either way, refusing to label is safer than confidently guessing)
- **Signed threat-feed updates (Phase 3.1)** — the FastAPI server signs `/v1/threat-feed` responses with **Ed25519** (PyCA `cryptography` on the server, `net.i2p.crypto:eddsa` on the device). The client pins the public key at *build time* via `local.properties` ➜ BuildConfig (never fetched at runtime — that would let a compromised server push its own key). Wire format is a "signed envelope" `{schema, key_id, signed_at_ms, payload_b64, signature_b64}`; the device decodes base64, runs `Ed25519.verify` against the pinned key, and only then parses the inner payload and upserts rows. Rejection paths (signature fail, wrong key id, malformed envelope, tampered payload) are distinct from generic network failures so the worker can decide whether to back off or hard-stop. Legacy unsigned responses are accepted only when the build has no key configured — a build with a key configured *cannot* be downgraded to unsigned silently
- **Privacy-preserving feedback channel (Phase 3.2)** — opt-in (default OFF) per-scan metadata uploads via `POST /v1/feedback` to the SafeGuard server. Strictly limited to `{sha256, verdict, confidence, package_name, version_code, layer_scores, triggered_rules}` — **no APK bytes, no file paths, no PII**. Three privacy switches gate every event (`scan_feedback_enabled` ∧ `scan_telemetry_enabled` ∧ ¬`privacy_sharing_opt_out`); turning any one off immediately stops both enqueueing and uploads. Events live in the same SQLCipher Room database as everything else, drained in batches by a network/battery-constrained `FeedbackUploadWorker` (every ~6 h, exponential backoff). The server enforces the privacy contract a second time via Pydantic `extra="forbid"` — an accidental client-side leak is a 422 not a stored-then-deleted record. Settings screen exposes the toggle and a "Clear queued feedback" purge action that deletes everything regardless of the gate state
- **Detection-rate benchmarking (Phase 3.3)** — a JVM-pure harness in `core:` reads a CSV manifest of `(sha256, label)` rows, runs them through a pluggable `ScanOracle`, and prints precision / recall / F1 / FPR with a stable line format that downstream dashboards can grep. Abstain verdicts (`SUSPICIOUS` / `UNKNOWN`) count as recall misses on malicious samples — refusing to confidently flag a banker is functionally identical to a false negative from the user's perspective. The smoke test is **skipped by default**; CI enables it by setting `SAFEGUARD_BENCHMARK_MANIFEST` (and optionally `SAFEGUARD_BENCHMARK_ORACLE`). See **[BENCHMARKING.md](BENCHMARKING.md)** for the manifest format, oracle JSON shape, and the published-score table contract
- **Play Integrity API cross-check (Phase 3.4)** — Layer 6 now consults a `PlayIntegrityChecker` and appends a `PlayIntegrity: device=… app=… account=… source=…` evidence line to every scan, regardless of the cloud's verdict. The default `NoOpPlayIntegrityChecker` returns `source=DISABLED` so dev / CI / non-Play builds still emit useful forensic data. A real `GooglePlayIntegrityChecker` is wired in via Hilt only when `safeguard.play.integrity.cloud.project.number` is set to a numeric value in `local.properties` (typo'd values fall back to NoOp with a single warning log — "fail open"). Today the integrity verdict is purely informational; gating the zero-trust decision engine on it is a follow-up, intentionally landed *after* the plumbing is stable and observable

## Requirements

- **Android**: minSdk 26, targetSdk 34
- **IDE**: Android Studio Hedgehog or later
- **JDK**: 17
- **Kotlin**: 1.9+

## Project structure

```
SafeGuardApp/
├── app/           # UI, Hilt, MainActivity, navigation, use case impls, WorkManager workers
│                  #   (ThreatFeedSyncWorker, FeedbackUploadWorker, GooglePlayIntegrityChecker scaffold)
├── core/          # Domain models, repository interfaces, zero-trust engine, orchestration,
│                  #   ScanFeedbackEvent + FeedbackPrivacyGate (3.2),
│                  #   benchmark harness (3.3),
│                  #   PlayIntegrityChecker / NoOp / Config (3.4)
├── data/          # Room (SQLCipher), Retrofit, repository impls (ThreatFeedRepositoryImpl,
│                  #   ScanFeedbackRepositoryImpl, Ed25519ThreatFeedVerifier), encrypted prefs
├── security/      # All 7 protection layers (Layer 7 YARA-subset engine + bundled rules),
│                  #   CloudVerifier (now consumes PlayIntegrityChecker), quarantine manager
├── mlmodel/       # TFLite loader, feature extraction for Layer 5
├── notification/  # Notification channels and scan result notifications
├── server/        # FastAPI: /v1/verify, /v1/threat-feed (Ed25519-signed envelope),
│                  #   /v1/feedback (Phase 3.2), feed_signer.py, feedback_routes.py
└── docs/          # This README, ARCHITECTURE.md, BENCHMARKING.md, PRODUCTION_READINESS_IMPLEMENTATION_PLAN.md
```

## Build and run

1. Clone and open the project in Android Studio.
2. **Gradle wrapper**: If `gradlew` / `gradlew.bat` are missing, run `gradle wrapper` from the project root (or let Android Studio create the wrapper on first sync).
3. Sync Gradle.
4. Set **Build Variant** to `debug`.
5. Run on device or emulator (API 26+): **Run > Run 'app'**.

From command line (after wrapper exists):

```bash
# Windows
gradlew.bat :app:assembleDebug

# Linux/macOS
./gradlew :app:assembleDebug

# Install: adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Configuration

- **Cloud API**: Base URL is in `data/di/NetworkModule.kt` (`provideBaseUrl()`). Replace with your threat-intel backend. If the backend is unreachable, Layer 6 returns `UNKNOWN`.
- **API base URL, privacy / terms URLs & certificate pin**: Set optional keys in `local.properties` (see `local.properties.example`). `safeguard.api.base.url` drives `BuildConfig.API_BASE_URL` for Layer 6 (`NetworkModule`). The FastAPI server under `server/` can call **MalwareBazaar** when `MALWAREBAZAAR_AUTH_KEY` is set (never in the app). See `server/README.md`; debug builds allow cleartext to `localhost` / `10.0.2.2` only.
- **Malware DB**: Seed `malware_signatures` and `trusted_apps` via Room/DAOs. Layer 2 uses this for exact (SHA-256) and fuzzy (TLSH) hash lookup. Set `fuzzyHash` to a 70-character TLSH hex string when seeding new rows; legacy `1:` / `3:` pseudo-fuzzy entries are silently ignored by the new lookup path.
- **Threat-feed sync**: `ThreatFeedSyncWorker` runs every 12h (`NetworkType.CONNECTED`, `requiresBatteryNotLow=true`, exponential backoff). It calls `ThreatIntelligenceApi.getThreatFeed(since, limit)` against the SafeGuard server, paginates via the server's `next_cursor_ms` until `has_more=false` (capped at 20 batches × 1000 rows), defensively re-validates hex lengths client-side, and `INSERT … ON CONFLICT REPLACE`s into `malware_signatures`. The cursor (`SecurePreferencesManager.lastThreatFeedSyncMs`) is persisted **only on a fully successful pass** so partial network failures retry the same window instead of skipping rows. Sync is skipped entirely when the user has disabled cloud verification.
- **Threat-feed status (dashboard tile)**: status is persisted in five additional `EncryptedSharedPreferences` fields (`lastThreatFeedSuccessMs`, `lastThreatFeedAttemptMs`, `lastThreatFeedOutcome`, `lastThreatFeedFailureReason`, `lastThreatFeedInsertedCount`) and emitted via `ThreatFeedStatusStore.observe()`. Atomicity comes from a single `prefs.edit().…apply()` plus a monotonic `epoch` counter the flow listens on. Failure rows preserve the previous `lastSuccessMs` / inserted count, so the UI keeps showing "Updated 2 days ago — last attempt failed" instead of regressing to "never synced" on the first transient error. Staleness threshold for the warning state is **48 h** (≥4 missed periodic cycles, well clear of WorkManager + Doze drift).
- **YARA ruleset (Layer 7)**: bundled under `security/src/main/assets/yara/*.yar`, parsed once at process startup by `YaraRuleSet.fromAssets(context)`. Each rule has `meta: severity = N`; severity ≥90 → MALICIOUS @ 0.92 confidence (single hit BLOCKs via the zero-trust engine), 60–89 → SUSPICIOUS proportional to severity. Per-scan ceilings: 4 MiB / artifact, 32 MiB / scan total, 6 native libs max. Add new rules by dropping a `*.yar` file in the asset directory — duplicate rule names, regex strings, half-byte wildcards, and all-wildcard hex patterns are rejected at *load* time so a bad rule fails QA loudly instead of silently never firing.
- **TFLite model**: Optional. Place `malware_detector_v3_quantized.tflite` in `mlmodel/src/main/assets/`. If missing, Layer 5 uses a heuristic.
- **Threat-feed signing keys (Phase 3.1)**: Generate with `python server/tools/generate_signing_key.py`. Set `THREAT_FEED_SIGNING_KEY_ID` + `THREAT_FEED_SIGNING_PRIVATE_KEY_B64` on the server (env / k8s secret — never commit), and `safeguard.threatfeed.signing.key.id` + `safeguard.threatfeed.signing.public.key.b64` in the device's `local.properties`. Empty values disable signing on both sides; a key configured on the device cannot be silently bypassed by an unsigned server response. The server exposes the public key + status at `GET /v1/threat-feed/public-key` for operational visibility, but the device never trusts it — pinning is build-time only.
- **Feedback channel (Phase 3.2)**: Server reads `FEEDBACK_MAX_BATCH` (default 200), `FEEDBACK_MAX_SKEW_MS` (default 48 h), and `FEEDBACK_SINK_CAPACITY` (default 1000) from the environment. The default sink is in-memory and capped — replace with a pluggable `FeedbackSink` (e.g. message queue, BigQuery streaming insert) for production volumes. The client never logs or transmits APK bytes / file paths regardless of server config; that boundary is enforced both in the DTO shape (`FeedbackEventJson` is allow-listed) and on the server with Pydantic `extra="forbid"`.
- **Detection-rate benchmark (Phase 3.3)**: see **[BENCHMARKING.md](BENCHMARKING.md)** for the corpus manifest, oracle JSON, and `SAFEGUARD_BENCHMARK_*` env vars.
- **Play Integrity API (Phase 3.4)**: Set `safeguard.play.integrity.cloud.project.number` in `local.properties` to a numeric GCP project ID to enable the real checker (currently a scaffold — emits `source=PLAY_INTEGRITY_API_ERROR` until the SDK call is wired). Empty / non-numeric values fall back to `NoOpPlayIntegrityChecker` (`source=DISABLED`). The verdict is appended to Layer 6 evidence on every scan but does not currently gate the zero-trust decision; see `core/src/main/kotlin/com/safeguard/core/domain/integrity/` for the contract.
- **Password reset SMTP (server)**: `POST /auth/reset-password` now supports real email delivery through SMTP. Set these environment variables before starting `uvicorn`: `AUTH_SMTP_HOST`, `AUTH_SMTP_PORT` (587 TLS / 465 SSL), `AUTH_SMTP_USERNAME`, `AUTH_SMTP_PASSWORD`, `AUTH_SMTP_FROM_EMAIL`, optional `AUTH_SMTP_FROM_NAME` (default `AEGISNODE Security`), `AUTH_SMTP_USE_TLS`, `AUTH_SMTP_USE_SSL`, optional `AUTH_RESET_LINK_BASE_URL`, and optional `AUTH_PASSWORD_RESET_TOKEN_TTL_SECONDS` (default 900). Keep `AUTH_DEBUG_RETURN_RESET_TOKEN=0` in production so tokens are never returned in API responses.

## Permissions

- `ok`
- `READ_EXTERNAL_STORAGE` / `READ_MEDIA_`* – Scan APK files
- `POST_NOTIFICATIONS` – Scan result and monitoring notifications
- `FOREGROUND_SERVICE` – File monitoring service
- `MANAGE_EXTERNAL_STORAGE` (Android 11+, **user-granted in Settings**) – Required to monitor and deep-scan messenger / chat-app folders such as `Android/media/<pkg>/` where modern Android places downloaded files. Without it, real-time monitoring still works but coverage of WhatsApp / Telegram drops dramatically. SafeGuard prompts for it once via a first-run permission onboarding screen and respects "Continue without it" — users can re-enable any time from Settings.

No accessibility or overlay abuse.

## Architecture

See **[ARCHITECTURE.md](ARCHITECTURE.md)** for layers, data flow, and zero-trust rules.

## Production readiness roadmap

See **[PRODUCTION_READINESS_IMPLEMENTATION_PLAN.md](PRODUCTION_READINESS_IMPLEMENTATION_PLAN.md)** for phased backend, testing, detection, and operations work beyond the Android client.

## Detection-rate benchmarks

See **[BENCHMARKING.md](BENCHMARKING.md)** for the corpus manifest format, env vars, the JVM-pure harness under `core:` that prints precision / recall / F1 / FPR, and the published-score table. The harness is opt-in — CI runs without a corpus stay green.

## License

Use per your project’s license terms.