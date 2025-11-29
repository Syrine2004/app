plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.ihelp_app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.ihelp_app"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    // Firebase Auth
    implementation("com.google.firebase:firebase-auth:22.3.0")
    // Firestore database
    implementation("com.google.firebase:firebase-firestore:24.10.0")
    // RecyclerView
    implementation("androidx.recyclerview:recyclerview:1.3.1")
    // Material Design
    implementation("com.google.android.material:material:1.11.0")
    // FirebaseUI for Firestore
    implementation("com.firebaseui:firebase-ui-firestore:8.0.2")
}