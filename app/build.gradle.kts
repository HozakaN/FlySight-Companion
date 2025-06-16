plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
//    alias(libs.plugins.kotlin.serialization)
    id("kotlin-kapt")
}

fun List<String>.removeAfter(nbElement: Int = 1): List<String> {
    return if (this.size <= nbElement) this else this.subList(0, nbElement)
}

fun versionName(): String {
    // release/1.0.0-RC1-7-ga2f6cd1
    val gitOutput = providers.exec {
        commandLine("git", "describe", "--tags")
    }.standardOutput.asText.get().trim()
    
    val intermediate = gitOutput.split("/")[1] // possible outputs are 1.0.0-RC1-7-ga2f6cd1, 1.0.0-7-ga2f6cd1, 1.0.0-RC1-ga2f6cd1, 1.0.0-ga2f6cd1
    val output =
        intermediate.split("-").removeAfter(if (intermediate.contains("RC")) 2 else 1).joinToString("-") // possible outputs are 1.0.0-RC1, 1.0.0
    return output
}

// format on 7 characters following pattern Major/Minor/Patch/Patch/Release/Release/Playstore
fun versionCode(): Int {
    val versionName = versionName()
    val major = versionName.first().toString().toInt()
    val minor = versionName.substring(2 until 3).toInt()
    val patch = versionName.substring(4 until 5).toInt()
    val release = if (versionName.contains("RC")) {
        versionName.substring(versionName.indexOf("RC") + 2).toInt()
    } else {
        0
    }
    val playstore = if (versionName.contains("playstore")) {
        1
    } else {
        0
    }
    return String.format("%d%d%02d%02d%d", major, minor, patch, release, playstore).toInt()
}

android {
    namespace = "fr.hozakan.flysightcompanion"
    compileSdk = 36

    defaultConfig {
        applicationId = "fr.hozakan.flysightcompanion"
        minSdk = 26
        targetSdk = 35
        versionCode = versionCode()
        versionName = versionName() //"1.0.0-RC1"

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
        buildConfig = true
        compose = true
    }
}

dependencies {

    implementation(project(":model"))
    implementation(project(":Tooling:framework"))
    implementation(project(":Middleware:BluetoothModule"))
    implementation(project(":Middleware:AudioModule"))
    implementation(project(":Middleware:ExternalDisplayModule"))
    implementation(project(":Feature:FSDeviceModule"))
    implementation(project(":Tooling:UsbModule"))
    implementation(project(":Feature:ConfigFilesModule"))
    implementation(project(":Feature:RecordsModule"))
    implementation(project(":Tooling:ComposableCommons"))
    implementation(project(":Tooling:DesignSystem"))
    implementation(project(":Feature:UserPreferencesModule"))
    implementation(project(":Tooling:DialogModule"))
    implementation(project(":Tooling:NetworkModule"))
    implementation(project(":Tooling:LoggerModule"))
    implementation(project(":Feature:SessionModule"))
    implementation(project(":Middleware:LocationModule"))
    implementation(project(":Feature:FirmwareModule"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.material.icons.extended)

    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    //Dagger
    compileOnly(libs.dagger)
    implementation(libs.dagger.android)
    implementation(libs.dagger.android.support)
    kapt(libs.dagger.android.processor)
    kapt(libs.dagger.compiler)

    // Google Play services location API
    implementation(libs.play.services.location)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}