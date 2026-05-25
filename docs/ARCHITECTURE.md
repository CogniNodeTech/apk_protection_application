# SafeGuard – Architecture

## High-level flow

1. **APK input**: FileObserver (Layer 1) or user manual scan (file picker).
2. **Orchestrator** runs Layers 2 → 7 → 6 in sequence (each layer can early-exit on a high-confidence MALICIOUS verdict via `ScanOrchestrator.internalScan`'s definitive-malware short-circuit).
3. **Zero-trust decision engine** combines all layer results and outputs final verdict and action.
4. **Actions**: Quarantine (block), Warn, or Allow; result is stored and optionally notified.

## Modules and dependencies

```
app → core, data, security, mlmodel, notification
security → core, data, mlmodel
data → core
mlmodel → core
notification → core (optional)
```

- **core**: Domain models (`Verdict`, `ScanResult`, `LayerResult`, `Action`, etc.), repository interfaces, `ZeroTrustDecisionEngine`, `RiskAssessmentEngine`, `ScanOrchestrator` (orchestrator depends on `ProtectionLayer` from core).
- **data**: Room DB (SQLCipher), DAOs, Retrofit, implementations of `ScanRepository`, `ThreatDatabaseRepository`, `CloudVerificationRepository`, `QuarantineRepository`.
- **security**: Implements `ProtectionLayer` for all 7 layers, `QuarantineManager`, `FileSystemMonitor`. Layer 7's bundled YARA-subset rules ship under `security/src/main/assets/yara/`.
- **mlmodel**: `FeatureExtractor`, `TFLiteRunner` for Layer 5.

## Protection layers

| Layer | Name                 | Role |
|-------|----------------------|------|
| 1     | File System Monitor  | Source path and file metadata risk (e.g. WhatsApp/Telegram, filename, size). |
| 2     | Hash Database        | SHA-256 + SHA-512 (single I/O pass) and TLSH fuzzy hash vs. malware/trusted DB. SHA-256 hit + SHA-512 confirm → MALICIOUS @ 1.0; SHA-256 hit + SHA-512 mismatch → **SUSPICIOUS @ 0.85** with `isCollision=true` and threat name suppressed (collision/tampering safety net); SHA-256 hit on a legacy row with no SHA-512 → MALICIOUS; trusted (non-expired) → SAFE. |
| 3     | Permission Analyzer   | Risk from requested permissions and dangerous combinations (e.g. SMS+INTERNET+CONTACTS). |
| 4     | Signature Validator  | X.509 from META-INF; unsigned/expired/weak/blacklisted/self-signed increase risk. |
| 5     | ML Behavioral        | 50+ features → TFLite (or heuristic if no model). Malware probability → verdict. |
| 7     | Pattern Rules (YARA) | Pure-Kotlin YARA-subset engine matches bundled `*.yar` rules against `classes*.dex`, `AndroidManifest.xml`, and small native libs. Severity ≥90 fires MALICIOUS @ 0.92 confidence (single-hit BLOCK). Catches recompiled / resigned variants whose hashes, certs, permissions and ML signal all look clean. |
| 6     | Cloud Verification   | Metadata-only request to backend; verdict and evidence from threat intel (incl. Layers 2–5+7 local scores). Offline → UNKNOWN. |

Each layer returns a **LayerResult** (verdict, confidence, risk score, evidence). Layers run independently; the orchestrator passes prior results into Layer 6 only, so Cloud can incorporate local signal without coupling earlier layers to each other.

## Zero-trust decision rules

- Any layer **MALICIOUS** and confidence **> 0.95** → **BLOCK**.
- **≥ 2** layers **MALICIOUS** and confidence **> 0.85** → **BLOCK**.
- **≥ 3** layers **SUSPICIOUS** → treat as **MALICIOUS** → **BLOCK**.
- Any layer **MALICIOUS** → overall **SUSPICIOUS** → **WARN**.
- Any layer **UNKNOWN** → overall **SUSPICIOUS** → **WARN**.
- **All** layers **SAFE** and confidence **> 0.7** → **SAFE** → **ALLOW**.
- Default → **SUSPICIOUS** → **WARN**.

Recommended action is then mapped (e.g. BLOCK → QUARANTINE). Cached results can be expired after 30 days for re-verification.

## Data flow

- **Scan**: `ScanAPKUseCase` → `ScanOrchestrator.scan(File)` → each `ProtectionLayer.verify(File)` → `ZeroTrustDecisionEngine` + `RiskAssessmentEngine` → `ScanResult` → `ScanRepository.saveScanResult()`.
- **Quarantine**: `QuarantineRepository.quarantine(path, result)` copies APK to app-private dir, deletes original, stores record with `auto_delete_at` (e.g. +30 days).
- **Cloud**: Layer 6 builds a metadata-only DTO (hashes, package, permissions, size, SDK, signature fingerprint, local layer scores, device metadata) and calls `CloudVerificationRepository.verify(...)` → backend returns verdict, confidence, evidence, recommendation.

## Threat-feed observability

- **Repository**: `ThreatFeedRepository.observeStatus(): Flow<ThreatFeedStatus>` exposes the latest persisted sync row. Backed by `ThreatFeedStatusStore` (a thin adapter over `SecurePreferencesManager`'s atomic `writeThreatFeedStatus(...)`), so observers always see a coherent snapshot of `lastSuccessMs` / `lastAttemptMs` / `lastOutcome` / `lastFailureReason` / `lastInsertedCount`.
- **Worker**: `ThreatFeedSyncWorker` writes a `SUCCESS` row on the happy path (cursor advanced, attempt time = success time), a `FAILED` row on network/HTTP/DB/parser errors (preserving the previous `lastSuccessMs` and `lastInsertedCount` so a transient blip doesn't regress the dashboard tile), and a `SKIPPED` row when cloud verification is disabled.
- **Dashboard tile** (`ThreatFeedStatusFormatter`): three colour states — *green* (synced inside `STALE_THRESHOLD_MS = 48 h`), *amber* (stale or last attempt failed but a recent success exists), *red* (never synced or every attempt has failed). The formatter is a pure JVM object so its ten branches are pinned down with deterministic unit tests; the Compose card just renders the resulting `Display`.

## UI

- **Dashboard**: Protection status, security score, recent scans summary, quarantine count, threat-database freshness tile (Phase 2.4), “Scan an APK” and “View Quarantine”.
- **Scan results**: Per-scan verdict, risk score, layer breakdown, evidence, actions (e.g. Block & Quarantine, Allow, Report false positive).
- **Quarantine**: List of quarantined items; delete, view details, restore.
- **Settings**: Real-time monitoring on/off, notification level, deep scan on/off (persisted via `SecurePreferencesManager`).

Theme: high contrast, 18sp+ body text, 48dp minimum touch targets.

## Security and privacy

- DB encrypted with SQLCipher; key in EncryptedSharedPreferences.
- Only metadata sent to cloud (no full APK upload).
- Minimal permissions; no accessibility or overlay abuse.
- Designed for privacy-aware deployment (data minimization).
