plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.safeguard.notification"
    compileSdk = 34
    defaultConfig { minSdk = 26 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    implementation(project(":core"))
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.core:core:1.12.0")
    testImplementation("junit:junit:4.13.2")
}
