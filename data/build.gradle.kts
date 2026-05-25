plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.safeguard.data"
    compileSdk = 34
    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    // android.util.Log is unmocked in plain JVM tests; returning defaults makes log calls
    // become no-ops instead of throwing RuntimeException, so repositories that log
    // diagnostics can be unit-tested without Robolectric.
    testOptions {
        unitTests.isReturnDefaultValues = false
    }
}

dependencies {
    implementation(project(":core"))
    implementation("androidx.room:room-runtime:2.6.0")
    implementation("androidx.room:room-ktx:2.6.0")
    ksp("androidx.room:room-compiler:2.6.0")
    implementation("net.zetetic:android-database-sqlcipher:4.5.4")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
    implementation("com.squareup.moshi:moshi:1.15.1")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.1")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    // Pure-Java Ed25519 (RFC 8032 PureEdDSA) JCA provider, ~88 KB. Used to verify the
    // signed threat-feed envelopes shipped by the SafeGuard server (Phase 3.1). We
    // deliberately avoid pulling in the full BouncyCastle (~6 MB) since the only
    // primitive we need on-device is Ed25519 signature verification.
    implementation("net.i2p.crypto:eddsa:0.3.0")
    implementation("com.google.dagger:hilt-android:2.50")
    ksp("com.google.dagger:hilt-compiler:2.50")
    implementation("javax.inject:javax.inject:1")
    testImplementation("junit:junit:4.13.2")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:core:1.5.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.room:room-testing:2.6.0")
}
