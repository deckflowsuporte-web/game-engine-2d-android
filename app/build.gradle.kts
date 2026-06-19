plugins {
    id("com.android.application")
}

android {
    namespace = "com.engine2d.game"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.engine2d.game"
        minSdk = 16
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        externalNativeBuild {
            cmake {
                cppFlags += "-std=c++17 -O2 -ffast-math"
                arguments += listOf(
                    "-DANDROID_STL=c++_shared",
                    "-DANDROID_TOOLCHAIN=clang"
                )
            }
        }

        // Support all ABIs including 32-bit for weak devices
        ndk {
            abiFilters += listOf(
                "armeabi-v7a",  // 32-bit ARM (old phones)
                "arm64-v8a",    // 64-bit ARM
                "x86",          // Intel 32-bit (emulators, old Chromebooks)
                "x86_64"        // Intel 64-bit
            )
        }
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
        debug {
            isMinifyEnabled = false
            isDebuggable = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    buildFeatures {
        prefab = true
    }

    ndkVersion = "25.1.8937393"
}

configurations.all {
    resolutionStrategy {
        force("org.jetbrains.kotlin:kotlin-stdlib:1.9.0")
        force("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.9.0")
        force("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.9.0")
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.core:core:1.12.0")
}
