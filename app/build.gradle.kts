plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.sergio.taskenda"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.sergio.taskenda"
        minSdk = 26
        targetSdk = 36

        // En GitHub Actions aumenta automáticamente con cada ejecución.
        // Si se compila fuera de GitHub, empieza en 2.
        versionCode = maxOf(
            2,
            System.getenv("GITHUB_RUN_NUMBER")?.toIntOrNull() ?: 2
        )

        versionName = "1.0"
    }

    signingConfigs {
        create("github") {
            val keystorePath = System.getenv("TASKENDA_KEYSTORE_PATH")

            if (!keystorePath.isNullOrBlank()) {
                storeFile = file(keystorePath)
                storePassword = System.getenv("TASKENDA_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("TASKENDA_KEY_ALIAS")
                keyPassword = System.getenv("TASKENDA_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        getByName("debug") {
            val keystorePath = System.getenv("TASKENDA_KEYSTORE_PATH")

            if (!keystorePath.isNullOrBlank()) {
                signingConfig = signingConfigs.getByName("github")
            }
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
        resources.excludes += "/META-INF/{LGPL2.1,AL2.0}"
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2026.08.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.10.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")
    implementation("androidx.navigation:navigation-compose:2.9.4")

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    implementation("androidx.room:room-runtime:2.8.3")
    implementation("androidx.room:room-ktx:2.8.3")
    ksp("androidx.room:room-compiler:2.8.3")
}
