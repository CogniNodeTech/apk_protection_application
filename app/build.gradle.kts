import java.util.Properties
import org.gradle.api.Project
import org.gradle.api.GradleException

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
}

private fun Project.loadLocalProperties(): Properties = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}

/** Escape for Java string literal inside BuildConfig. */
private fun String.escapeForBuildConfigField(): String =
    replace("\\", "\\\\").replace("\"", "\\\"")

android {
    namespace = "com.safeguard"
    compileSdk = 34

    val localProps = project.loadLocalProperties()
    fun prop(key: String, default: String = ""): String =
        localProps.getProperty(key, default)?.trim() ?: default

    defaultConfig {
        applicationId = "com.safeguard"
        minSdk = 26
        targetSdk = 34
        versionCode = 2
        versionName = "1.0.1"
        // Optional overrides — see local.properties.example and docs/README.md (Configuration).
        val certPin = prop("safeguard.cert.pin")
        val appSigningCertSha256 = prop("safeguard.app.signature.sha256", "")
        // Default dev endpoint (Android emulator -> local Node auth server).
        val apiBaseUrl = prop("safeguard.api.base.url", "http://10.0.2.2:3001/api/")
        val apiKey = prop("safeguard.api.key")
        val googleAndroidClientId = prop("safeguard.oauth.google.android.client.id")
        val googleWebClientId = prop("safeguard.oauth.google.web.client.id")
        val privacyUrl = prop("safeguard.privacy.policy.url", "https://safeguard.example.com/privacy")
        val termsUrl = prop("safeguard.terms.url", "https://safeguard.example.com/terms")
        // Phase 3.1: Ed25519 public key the on-device verifier pins for the
        // /v1/threat-feed signed-envelope check. Empty = signing disabled (legacy path).
        // Populate via local.properties in any environment that runs against a server
        // configured with THREAT_FEED_SIGNING_PRIVATE_KEY_B64.
        val threatFeedSigningKeyId = prop("safeguard.threatfeed.signing.key.id", "")
        val threatFeedPublicKeyB64 = prop("safeguard.threatfeed.signing.public.key.b64", "")
        // Phase 3.4: Play Integrity API cross-check. Empty = NoOpPlayIntegrityChecker
        // wins (the default; appropriate for dev / CI / non-Play builds). When set to
        // a numeric GCP cloud project number, the Hilt module wires up the real
        // checker. We deliberately don't ship a default value — getting this wrong
        // would silently mint integrity tokens against an attacker-controlled project.
        val playIntegrityCloudProjectNumber = prop("safeguard.play.integrity.cloud.project.number", "")
        buildConfigField("String", "CERT_PIN", "\"${certPin.escapeForBuildConfigField()}\"")
        buildConfigField("String", "APP_SIGNING_CERT_SHA256", "\"${appSigningCertSha256.escapeForBuildConfigField()}\"")
        buildConfigField("String", "API_BASE_URL", "\"${apiBaseUrl.escapeForBuildConfigField()}\"")
        buildConfigField("String", "API_KEY", "\"${apiKey.escapeForBuildConfigField()}\"")
        buildConfigField("String", "GOOGLE_ANDROID_CLIENT_ID", "\"${googleAndroidClientId.escapeForBuildConfigField()}\"")
        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"${googleWebClientId.escapeForBuildConfigField()}\"")
        buildConfigField("String", "PRIVACY_POLICY_URL", "\"${privacyUrl.escapeForBuildConfigField()}\"")
        buildConfigField("String", "TERMS_OF_SERVICE_URL", "\"${termsUrl.escapeForBuildConfigField()}\"")
        buildConfigField("String", "THREAT_FEED_SIGNING_KEY_ID", "\"${threatFeedSigningKeyId.escapeForBuildConfigField()}\"")
        buildConfigField("String", "THREAT_FEED_SIGNING_PUBLIC_KEY_B64", "\"${threatFeedPublicKeyB64.escapeForBuildConfigField()}\"")
        buildConfigField(
            "String",
            "PLAY_INTEGRITY_CLOUD_PROJECT_NUMBER",
            "\"${playIntegrityCloudProjectNumber.escapeForBuildConfigField()}\""
        )
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
    // android.util.Log is unmocked in plain JVM tests; returning defaults makes log calls
    // become no-ops instead of throwing RuntimeException, so unit tests can construct
    // production classes that call into android.util.Log (e.g. GooglePlayIntegrityChecker).
    testOptions {
        unitTests.isReturnDefaultValues = false
    }
}

val releaseRequested = gradle.startParameter.taskNames.any { task ->
    task.contains("Release", ignoreCase = true)
}
val allowInsecureRelease = project.findProperty("allowInsecureRelease")?.toString()?.toBooleanStrictOrNull() == true
if (releaseRequested && !allowInsecureRelease) {
    val releaseCertPin = project.loadLocalProperties().getProperty("safeguard.cert.pin", "").trim()
    val releaseAppSigningCertSha256 = project.loadLocalProperties().getProperty("safeguard.app.signature.sha256", "").trim()
    val releaseApiBaseUrl = project.loadLocalProperties().getProperty("safeguard.api.base.url", "https://api.safeguard.example.com/").trim()
    val releasePrivacyUrl = project.loadLocalProperties().getProperty(
        "safeguard.privacy.policy.url",
        "https://safeguard.example.com/privacy"
    ).trim()
    val releaseTermsUrl = project.loadLocalProperties().getProperty(
        "safeguard.terms.url",
        "https://safeguard.example.com/terms"
    ).trim()
    if (releaseCertPin.isBlank()) {
        throw GradleException("Release build blocked: set safeguard.cert.pin in local.properties")
    }
    if (releaseAppSigningCertSha256.isBlank()) {
        throw GradleException("Release build blocked: set safeguard.app.signature.sha256 in local.properties")
    }
    if (!releaseApiBaseUrl.startsWith("https://")) {
        throw GradleException("Release build blocked: safeguard.api.base.url must use https://")
    }
    if (releaseApiBaseUrl.contains("example.com")) {
        throw GradleException("Release build blocked: safeguard.api.base.url points to placeholder domain")
    }
    if (!releasePrivacyUrl.startsWith("https://")) {
        throw GradleException("Release build blocked: safeguard.privacy.policy.url must use https://")
    }
    if (releasePrivacyUrl.contains("example.com") || releasePrivacyUrl.contains(".example")) {
        throw GradleException("Release build blocked: safeguard.privacy.policy.url points to placeholder domain")
    }
    if (!releaseTermsUrl.startsWith("https://")) {
        throw GradleException("Release build blocked: safeguard.terms.url must use https://")
    }
    if (releaseTermsUrl.contains("example.com") || releaseTermsUrl.contains(".example")) {
        throw GradleException("Release build blocked: safeguard.terms.url points to placeholder domain")
    }
}

dependencies {
    implementation(project(":core"))
    implementation(project(":data"))
    implementation(project(":security"))
    implementation(project(":mlmodel"))
    implementation(project(":notification"))

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.8.1")
    implementation(platform("androidx.compose:compose-bom:2023.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3:1.1.2")
    implementation("androidx.compose.material:material-icons-extended:1.5.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.6.2")
    implementation("androidx.navigation:navigation-compose:2.7.5")
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    implementation("com.google.dagger:hilt-android:2.50")
    ksp("com.google.dagger:hilt-compiler:2.50")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    implementation("androidx.hilt:hilt-work:1.1.0")
    ksp("androidx.hilt:hilt-compiler:1.1.0")
    implementation("com.squareup.moshi:moshi:1.15.1")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.1")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.10.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("io.mockk:mockk:1.13.13")
    testImplementation("app.cash.turbine:turbine:1.1.0")
    testImplementation(kotlin("test"))
}
