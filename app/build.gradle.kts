plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.myapplicationvoice"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.myapplicationvoice"
        minSdk = 27
        targetSdk = 34
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
    implementation(libs.recyclerview.v7)
    implementation(libs.cardview.v7)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)


    implementation("com.yandex.android:maps.mobile:4.13.0-lite")
    implementation("io.github.ParkSangGwon:tedpermission-normal:3.4.2")
    implementation("io.github.ParkSangGwon:tedpermission-coroutine:3.4.2")

    implementation("com.google.android.material:material:1.6.0")
    implementation("androidx.appcompat:appcompat:1.4.2")
    implementation ("androidx.cardview:cardview:1.0.0")

    implementation ("com.google.code.gson:gson:2.10.1")
    implementation ("io.github.cdimascio:java-dotenv:5.2.2")
}