import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

val localProperties = Properties()
val localPropsFile = rootProject.file("local.properties")
if (localPropsFile.exists()) {
    localPropsFile.inputStream().use { localProperties.load(it) }
}
// api.base.url в local.properties (порт как у uvicorn). По умолчанию ниже — типичный LAN для телефона; для эмулятора в local.properties: http://10.0.2.2:8001/
// Сервер: 0.0.0.0, брандмауэр, после смены URL — пересборка.
val apiBaseUrlRaw = localProperties.getProperty("api.base.url") ?: "http://192.168.0.5:8001/"
val apiBaseUrl = if (apiBaseUrlRaw.endsWith("/")) apiBaseUrlRaw else "$apiBaseUrlRaw/"
val apiBaseUrlForBuildConfig = apiBaseUrl.replace("\\", "\\\\").replace("\"", "\\\"")

android {
    namespace = "com.example.smartwallet"
    compileSdk = 36

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.example.smartwallet"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "API_BASE_URL", "\"$apiBaseUrlForBuildConfig\"")
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
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.viewpager2)
    
    // MPAndroidChart for analytics
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation("com.google.android.flexbox:flexbox:3.0.0")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")

    val cameraX = "1.4.2"
    implementation("androidx.camera:camera-core:$cameraX")
    implementation("androidx.camera:camera-camera2:$cameraX")
    implementation("androidx.camera:camera-lifecycle:$cameraX")
    implementation("androidx.camera:camera-view:$cameraX")
    implementation("com.google.mlkit:barcode-scanning:17.3.0")
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}