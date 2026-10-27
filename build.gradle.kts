// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    // Kotlin 2.4 + AGP 9.4 + Gradle 9.6: the toolchain Material 3 Expressive (material3 1.5)
    // and Compose 1.12 need (they require AGP 9.1+ and compileSdk 37).
    // Kotlin itself is built into AGP 9 (no org.jetbrains.kotlin.android plugin); this version
    // is for the Compose compiler and serialization plugins, which still come from Kotlin.
    val kotlinVersion = "2.4.20"
    id("com.android.application") version "9.4.1" apply false
    id("org.jetbrains.kotlin.plugin.compose") version kotlinVersion apply false
    id("org.jetbrains.kotlin.plugin.serialization") version kotlinVersion apply false
    // KSP 2.3.x is no longer tied to one Kotlin version (needs Kotlin 2.3+ and AGP 8.12+)
    id("com.google.devtools.ksp") version "2.3.12" apply false
    // 0.13.0 is the first oss-licenses plugin version built/tested against AGP 9
    id("com.google.android.gms.oss-licenses-plugin") version "0.13.0" apply false
}
