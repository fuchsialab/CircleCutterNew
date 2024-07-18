plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.google.gms.google.services)

}

android {
    namespace = "com.fuchsialab.circlecutter"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.fuchsialab.circlecutter"
        minSdk = 23
        targetSdk = 34
        versionCode = 44
        versionName = "4.4"

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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation("com.isseiaoki:simplecropview:1.1.8")

    implementation ("com.google.firebase:firebase-database:21.0.0")
    implementation ("com.google.firebase:firebase-auth:23.0.0")
    implementation ("com.google.firebase:firebase-analytics:22.0.2")

    implementation ("com.google.android.gms:play-services-ads:23.2.0")
    implementation ("com.google.ads.mediation:applovin:11.1.0.0")
    implementation ("com.google.ads.mediation:adcolony:4.6.4.0")
    implementation ("com.google.ads.mediation:facebook:6.7.0.0")
    //implementation 'com.google.android.ads:mediation-test-suite:2.0.0'
    implementation ("com.github.hotchemi:android-rate:1.0.1")
    implementation ("com.google.android.ump:user-messaging-platform:2.1.0")

}
