import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.google.devtools.ksp")
    id("com.google.android.gms.oss-licenses-plugin")
}

val privateProps = Properties().also {
    it.load(project.rootProject.file("private.properties").reader())
}
val versionProps = Properties().also {
    it.load(project.rootProject.file("version.properties").reader())
}

android {
    // 37 = Android 17 SDK, required by Compose 1.12 / material3 1.5. This only changes which
    // APIs we compile against; app behaviour on devices is controlled by targetSdk (still 36).
    compileSdk = 37

    base {
        archivesName = "yumelist-v${versionProps.getProperty("name")}"
    }

    defaultConfig {
        applicationId = "com.ahmad.yumelist"
        // material3 1.5 (Expressive) requires Android 7.0+ (API 24)
        minSdk = 24
        targetSdk = 36
        versionCode = versionProps.getProperty("code").toInt()
        versionName = versionProps.getProperty("name")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    androidResources {
        localeFilters += listOf("en")
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-DEBUG"
            isDebuggable = true
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "CLIENT_ID", "\"${privateProps.getProperty("CLIENT_ID")}\"")
        }
        release {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "CLIENT_ID", "\"${privateProps.getProperty("CLIENT_ID")}\"")
        }
    }
    splits {
        abi {
            isEnable = true
            reset()
            include("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
            isUniversalApk = true
        }
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    namespace = "com.axiel7.moelist"
}

dependencies {

    // Phase 7B: every version below is the latest stable as of 24 Oct 2026 (material3 stays on
    // the 1.5 beta line: Expressive is only public there)

    //AndroidX
    implementation("androidx.core:core-ktx:1.19.1")
    implementation("androidx.appcompat:appcompat:1.8.0")

    implementation("androidx.browser:browser:1.10.0")
    implementation("androidx.datastore:datastore-preferences:1.3.0")
    implementation("androidx.work:work-runtime:2.12.0")
    implementation("androidx.core:core-splashscreen:1.2.0")

    //Compose
    // Newest BOM (Compose 1.12.1), the line material3 1.5.0-beta02 is built on
    implementation(platform("androidx.compose:compose-bom:2026.09.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    // material-icons-extended removed (Phase 7): the ~110 icons the app uses live in ui/icons
    debugImplementation("androidx.compose.ui:ui-tooling")

    // Material 3 Expressive (MaterialExpressiveTheme, MotionScheme, ButtonGroup, LoadingIndicator,
    // flexible top app bars, floating toolbars...) is only public from 1.5.0. The BOM maps
    // material3 to 1.4.x, so pin it explicitly. Other material3 artifacts align to this version.
    implementation("androidx.compose.material3:material3:1.5.0-beta02")
    implementation("androidx.compose.material3:material3-window-size-class")

    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.navigation:navigation-compose:2.10.2")

    val lifecycleVersion = "2.11.0"
    implementation("androidx.lifecycle:lifecycle-runtime-compose:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-viewmodel-savedstate:$lifecycleVersion")

    implementation("androidx.glance:glance-appwidget:1.2.0")

    // accompanist-permissions removed (Phase 7): the notification permission now uses the
    // standard AndroidX Activity Result API

    // placeholder-material3 removed: replaced by our own lightweight skeleton modifier (Skeleton.kt)

    //Test
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")

    //Networking: Retrofit 3 + OkHttp 5 (replaced Ktor in Phase 6)
    // The OkHttp BOM keeps okhttp, logging-interceptor and the copy Coil uses on one version.
    implementation(platform("com.squareup.okhttp3:okhttp-bom:5.5.1"))
    implementation("com.squareup.okhttp3:okhttp")
    implementation("com.squareup.okhttp3:logging-interceptor")
    val retrofitVersion = "3.0.0"
    implementation("com.squareup.retrofit2:retrofit:$retrofitVersion")
    // Official kotlinx.serialization converter (part of Retrofit since 2.11)
    implementation("com.squareup.retrofit2:converter-kotlinx-serialization:$retrofitVersion")

    //Utils
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")
    implementation("org.apache.commons:commons-text:1.15.0")

    //Image
    val coilVersion = "3.6.3"
    implementation("io.coil-kt.coil3:coil-compose:$coilVersion")
    implementation("io.coil-kt.coil3:coil-network-okhttp:$coilVersion")

    //Koin
    implementation(platform("io.insert-koin:koin-bom:4.2.2"))
    implementation("io.insert-koin:koin-androidx-compose")
    implementation("io.insert-koin:koin-androidx-compose-navigation")
    implementation("io.insert-koin:koin-androidx-workmanager")
    implementation("io.insert-koin:koin-androidx-startup")

    //OSS Licenses
    implementation("com.google.android.gms:play-services-oss-licenses:17.5.2")

    //Room
    val roomVersion = "2.8.5"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")
}
