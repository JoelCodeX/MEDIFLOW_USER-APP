import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.jotadev.mediflow"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.jotadev.mediflow"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val baseUrlEmulator: String = (project.findProperty("BASE_URL_EMULATOR") as String?) ?: "http://10.0.2.2:8000/"
        val baseUrlDevice: String = (project.findProperty("BASE_URL_DEVICE") as String?) ?: "http://192.168.1.95:8000/"
        val baseUrlFallback: String = (project.findProperty("BASE_URL") as String?) ?: baseUrlEmulator

        // BASE_URL queda como fallback; el cliente elegirá entre EMULATOR y DEVICE en runtime
        buildConfigField("String", "BASE_URL", "\"${baseUrlFallback}\"")
        buildConfigField("String", "BASE_URL_EMULATOR", "\"${baseUrlEmulator}\"")
        buildConfigField("String", "BASE_URL_DEVICE", "\"${baseUrlDevice}\"")
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
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    // Necesario para usar el API de pull-to-refresh
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.compose.runtime.saveable.lint)
    implementation(libs.androidx.compose.ui.text.google.fonts)
    implementation(libs.androidx.navigation.compose)
    // Networking
    implementation(libs.retrofit2)
    implementation(libs.retrofit2.converter.gson)
    implementation(libs.okhttp3)
    implementation(libs.okhttp3.logging.interceptor)
    // Firebase + Google Sign-In
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.play.services.auth)
    implementation(libs.firebase.messaging)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.androidx.datastore.core)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    //icons
    implementation(libs.androidx.compose.material.icons.extended)
}