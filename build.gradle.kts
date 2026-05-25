// Top-level build file for SafeGuard - APK Protection Application
plugins {
    id("com.android.application") version "8.2.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
    id("com.google.dagger.hilt.android") version "2.50" apply false
    id("com.google.devtools.ksp") version "1.9.22-1.0.17" apply false
    id("io.gitlab.arturbosch.detekt") version "1.23.4" apply false
}

// Detekt: apply in subprojects that have Kotlin. Run with: ./gradlew detekt
subprojects {
    plugins.withId("org.jetbrains.kotlin.android") {
        apply(plugin = "io.gitlab.arturbosch.detekt")
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}
