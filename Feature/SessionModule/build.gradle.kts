plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}

android {
    namespace = "fr.hozakan.flysightcompanion.sessionmodule"
    compileSdk = 36

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
        compose = true
    }
}

secrets {
    // To add your Maps API key to this project:
    // 1. If the secrets.properties file does not exist, create it in the same folder as the local.properties file.
    // 2. Add this line, where YOUR_API_KEY is your API key:
    //        MAPS_API_KEY=YOUR_API_KEY
    propertiesFileName = "secrets.properties"

    // A properties file containing default secret values. This file can be
    // checked in version control.
    defaultPropertiesFileName = "local.defaults.properties"
}

dependencies {
    implementation(project(":model"))
    implementation(project(":Feature:FSDeviceModule"))
    implementation(project(":Feature:RecordsModule"))
    implementation(project(":Tooling:DialogModule"))
    implementation(project(":Tooling:framework"))
    implementation(project(":Tooling:DesignSystem"))
    implementation(project(":Tooling:ComposableCommons"))
    implementation(project(":Middleware:AudioModule"))
    implementation(project(":Middleware:ExternalDisplayModule"))
    implementation(project(":Feature:ConfigFilesModule"))
    implementation(project(":Middleware:LocationModule"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    //Dagger
    implementation(libs.dagger)
//    implementation(libs.dagger.android)
//    implementation(libs.dagger.android.support)

    //Compose
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    debugImplementation(libs.androidx.ui.tooling)
//    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.activity.compose)

    //GMap
    implementation(libs.maps.compose)

    //Tests
    testImplementation(project(":model"))
    testImplementation(project(":Tooling:framework"))
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}