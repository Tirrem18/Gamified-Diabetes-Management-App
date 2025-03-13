plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services") // 🔹 Add this line
}

android {
    namespace = "com.b1097780.glucohub"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.b1097780.glucohub"
        minSdk = 27
        targetSdk = 35
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.fragment.ktx)
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation(libs.firebase.crashlytics.buildtools)
    dependencies {
        dependencies {
            // 🔥 Firebase BOM (Ensures all Firebase libraries use compatible versions)
            implementation(platform("com.google.firebase:firebase-bom:32.2.2"))

            // 🔥 Firebase Authentication
            implementation("com.google.firebase:firebase-auth")

            // 🔥 Firebase Firestore
            implementation("com.google.firebase:firebase-firestore")

            // 🔥 Firebase Realtime Database (If needed)
            implementation("com.google.firebase:firebase-database")

            // 🔥 Firebase Storage (If needed)
            implementation("com.google.firebase:firebase-storage")
        }

    }

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation("com.google.code.gson:gson:2.10.1")
}
