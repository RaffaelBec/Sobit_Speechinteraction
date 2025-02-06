plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.javaspeechrecognizer"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.javaspeechrecognizer"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        ndkVersion = "25.2.9519653"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

        configurations {
            all { // You should exclude one of them not both of them
                resolutionStrategy {
                    force("net.java.dev.jna:jna:5.13.0")
                }
            }
        }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    packaging {
        resources.excludes.add("META-INF/AL2.0")
        resources.excludes.add("META-INF/LGPL2.1")
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.core.splashscreen)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    testImplementation("org.robolectric:robolectric:4.14.1")
    testImplementation("org.mockito:mockito-core:5.15.2")
    implementation("com.microsoft.onnxruntime:onnxruntime-android:1.20.0")
    implementation("net.java.dev.jna:jna:5.13.0@aar")
    implementation("com.alphacephei:vosk-android:0.3.47@aar")




}