# Google Play Data safety (checklist)

Use this when completing the **Data safety** section in Play Console. Align answers with your [privacy policy](https://safeguard.example.com/privacy) (replace with production URL) and [DATA_PROCESSING_INVENTORY.md](compliance/DATA_PROCESSING_INVENTORY.md).

## Data collected (typical for SafeGuard)

| Data type | Sent off device? | Purpose | Encryption in transit |
|-----------|------------------|---------|------------------------|
| App info (package name, version) | Yes (Layer 6 cloud) | Malware / threat intelligence | HTTPS (TLS) |
| App activity (scan events, verdicts) | Optional only if you enable telemetry backend | Analytics | HTTPS |
| Files and docs (hashes, APK metadata) | Hashes and metadata only, not full APK file | Threat check | HTTPS |
| Device or other IDs | Avoid collecting advertising IDs; device locale / API level may be sent as metadata | Threat / compatibility | HTTPS |

**Does not apply:** Precise location, contacts, SMS, call logs unless you add such features.

## Permissions

- **Internet:** Cloud verification and optional telemetry.
- **Storage / media read:** Scan APK files (declare per [AndroidManifest.xml](../app/src/main/AndroidManifest.xml)).
- **Notifications:** Scan results.
- **Foreground service:** File monitoring (declare `specialUse` subtype per manifest).

## Account deletion

If you do not offer accounts, state that users can delete local data via **Settings → Delete all data** and uninstall the app.

## Certificate pinning

Production builds should set the SHA-256 pin via `safeguard.cert.pin` in `local.properties` (see `local.properties.example` in the project root). Empty value keeps pinning disabled. Implementation: [NetworkModule.kt](../data/src/main/kotlin/com/safeguard/data/di/NetworkModule.kt), wired through `BuildConfig.CERT_PIN` in `app/build.gradle.kts`.
