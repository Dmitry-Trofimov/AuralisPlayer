import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.daimonxx.auralis"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.daimonxx.auralis"
        minSdk = 24
        targetSdk = 36
        versionCode = 3
        versionName = "1.2.0"
    }

    buildTypes {
        release {
            // R8 removes unused code; the resource shrinker removes unused resources.
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            // Keep debug builds easy to inspect in Android Studio.
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2025.10.00"))

    // Android entry point and window/system-bar integration.
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.activity:activity-compose:1.11.0")

    // Only Compose modules used by the UI are declared.
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.animation:animation-core")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    // Local audio playback and Android media controls.
    implementation("androidx.media3:media3-exoplayer:1.11.1")
    implementation("androidx.media3:media3-session:1.11.1")

    // Used by the MediaStore scanner; no image/network library is required.
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
}
