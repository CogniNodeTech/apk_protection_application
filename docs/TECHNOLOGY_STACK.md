# SafeGuard Technology Stack

This document summarizes the complete technology stack used in the SafeGuard project.

## 1) Language stack

- **Frontend (Android app):** Kotlin
- **Backend (local/mock API):** Python 3 + FastAPI (see `server/`)
- **Build/config scripts:** Kotlin DSL (`*.gradle.kts`)
- **CI/CD config:** YAML (GitHub Actions workflow)

## 2) Frontend / Android app stack

- **UI framework:** Jetpack Compose
- **Navigation:** `androidx.navigation:navigation-compose`
- **Architecture style:** MVVM-style with ViewModel + StateFlow
- **Dependency injection:** Hilt (`com.google.dagger:hilt-android`)
- **Background work:** WorkManager (scheduled tasks); **real-time APK monitoring** uses `FileObserver` + foreground service (`FileObserverService`), not WorkManager alone
- **JSON:** Moshi (Retrofit + Room layer-result persistence; Kotlin-friendly)
- **Android services/components:** Foreground Service, BroadcastReceiver, Notifications
- **SDK targets:** `minSdk 26`, `targetSdk 34`, `compileSdk 34`
- **Java toolchain:** JDK 17

## 3) Networking stack (Android side)

- **HTTP transport:** OkHttp
- **API client:** Retrofit
- **JSON converter:** `retrofit2-converter-moshi`
- **Network hardening used in app:**
  - Optional certificate pinning
  - `Authorization: Bearer <token>` support
  - Timeouts and retry-safe behavior
  - Debug-only HTTP body logging

## 4) API layer used in this project

- **Primary app API interface:** `ThreatIntelligenceApi`
  - Endpoint: `POST /v1/verify`
- **Mock backend endpoints (Node/Express):**
  - `POST /v1/verify`
  - `GET /health`

> Note: Python/FastAPI is **not** used in the current codebase.

## 5) Database and local storage

- **Local relational database:** Room (SQLite abstraction)
- **Database encryption:** SQLCipher (`net.zetetic:android-database-sqlcipher`)
- **Secure key-value storage:** EncryptedSharedPreferences + MasterKey

## 6) Security and malware-analysis stack

- **APK parsing:** `net.dongliu:apk-parser`
- **DEX inspection:** `org.smali:dexlib2`
- **On-device ML inference:** TensorFlow Lite
  - `tensorflow-lite`
  - `tensorflow-lite-support`
  - `tensorflow-lite-gpu`
- **Custom multi-layer security pipeline:** Implemented across `core`, `security`, and `mlmodel` modules

## 7) Project module structure

- `app` — UI, navigation, app entry, presentation layer
- `core` — domain models, orchestration, repository contracts
- `data` — Room DB, Retrofit APIs, repository implementations
- `security` — layered protection logic
- `mlmodel` — feature extraction + TFLite integration
- `notification` — notification handling and channels

## 8) Backend in repository

- **Runtime:** Python 3
- **Framework:** FastAPI + Uvicorn
- **Role:** Local/mock Layer 6 HTTP API for development
- **Production status:** Stub only; replace with a hardened TI platform (see architecture notes in `server/README.md`)

## 9) Build, quality, and CI/CD

- **Build system:** Gradle (Kotlin DSL)
- **Static analysis:** Detekt
- **Android quality checks:** Lint
- **Automated tests:** Unit tests + connected Android tests
- **CI platform:** GitHub Actions
- **Dependency/security scan in CI:** Trivy

## 10) Key Android permissions and platform features

- `INTERNET`
- `READ_EXTERNAL_STORAGE`, `READ_MEDIA_IMAGES`
- `POST_NOTIFICATIONS`
- `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_SPECIAL_USE`
- `WAKE_LOCK`

Plus:

- Network Security Config
- Disabled default WorkManager initializer (manual/provider configuration in app)

