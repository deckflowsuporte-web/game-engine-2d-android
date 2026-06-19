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
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    // Pure Java game - no external dependencies
}
