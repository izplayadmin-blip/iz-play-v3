plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
}

// Nomeia os artefatos como IZPlay-V3-<variant>.apk em vez de app-<variant>.apk.
base {
    archivesName.set("IZPlay-V3")
}

android {
    namespace = "com.izplay.v3"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.izplay.v3"
        // As TV boxes rk322x alvo do IZ Play reportam API 25 (Android 7.1),
        // apesar de anunciarem "Android 11.1" no marketing. minSdk 24 as cobre.
        // Todo uso de API 26+ (PiP, canais de notificação, AudioFocusRequest)
        // tem guarda de runtime; java.time é reimplementado pelo desugaring.
        // Verificado por `lint` (checagem NewApi) com esta minSdk.
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "3.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Keep the APK lean: media-kit ships only these three ABIs, and the
        // CMake JNI bridge links against `jniLibs/<abi>/libmpv.so` — adding
        // another ABI here without staging an .so first would crash at load.
        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a", "x86_64")
        }

        externalNativeBuild {
            cmake {
                cppFlags += "-std=c++17"
            }
        }
    }

    externalNativeBuild {
        cmake {
            // CMake 3.22+ ships with the NDK side-by-side bundle on AGP 8;
            // pinning keeps reproducible JNI builds across machines.
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        // java.time em PlaylistSettingsScreen é API 26+; o desugaring o
        // reimplementa para API 24/25 sem alterar o código.
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        // Enables BuildConfig.VERSION_NAME used by the settings "About" row.
        buildConfig = true
    }

    // Room schemas are exported so future migrations can diff against them.
    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Room — SQLite persistence (Android counterpart of GRDB on iOS).
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Networking + JSON for the Xtream API (Android counterparts of URLSession +
    // JSONDecoder used by iOS `XtreamAPIClient`).
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.kotlinx.serialization.json)

    // Coil — image loading for the dashboard's category shelves
    // (Android counterpart of iOS `CachedImage`).
    implementation(libs.coil.compose)

    // WorkManager — long-running background downloads with a foreground
    // service. iOS uses `URLSessionConfiguration.background(withIdentifier:)`;
    // on Android the system-blessed equivalent is `CoroutineWorker`-as-
    // foreground-service so the OS doesn't reap a half-finished download.
    implementation(libs.androidx.work.runtime.ktx)

    // DocumentFile — wrapper around SAF tree URIs. The user picks an `.m3u`
    // file via `ACTION_OPEN_DOCUMENT`; DocumentFile gives us a stable handle
    // to read the bytes back without having to keep the original Uri around.
    implementation(libs.androidx.documentfile)

    // Reimplementa java.time (usado em PlaylistSettingsScreen) para API 24/25.
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
