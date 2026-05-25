# Security release checklist

Use this checklist before publishing any production build. For phased backend/testing/ops work, see **[PRODUCTION_READINESS_IMPLEMENTATION_PLAN.md](PRODUCTION_READINESS_IMPLEMENTATION_PLAN.md)**.

- `./gradlew :app:assembleRelease` passes without `-PallowInsecureRelease=true`.
- `local.properties` contains non-empty `safeguard.cert.pin`.
- `local.properties` production `safeguard.api.base.url` is HTTPS and not a placeholder.
- Unit tests pass: `:core:testDebugUnitTest`, `:security:testDebugUnitTest`, `:data:testDebugUnitTest` (includes `ForensicReasoningEngineTest`, `CloudVerificationRepositoryImplTest`, `LayerResultDtoMoshiTest`).
- Optional: run `:app:compileDebugAndroidTestKotlin` and device/UI smoke (`ComposeThemeSmokeTest`) on a real device or emulator.
- Lint and Detekt pass in CI with no soft-fail.
- Backend `/health` returns healthy and `/v1/verify` contract matches `VerificationResponse` (production: set server `MALWAREBAZAAR_AUTH_KEY` for MalwareBazaar; see `server/README.md`).
- Incident response contacts and runbooks are up to date.

