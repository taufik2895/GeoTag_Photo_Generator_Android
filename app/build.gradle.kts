import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

val localProperties = Properties().apply {
    rootProject.file("local.properties").takeIf { it.isFile }?.inputStream()?.use(::load)
}
val mapsApiKey = providers.gradleProperty("MAPS_API_KEY")
    .orElse(providers.environmentVariable("GOOGLE_MAPS_API_KEY"))
    .orElse(localProperties.getProperty("MAPS_API_KEY").orEmpty())
    .get()
    .trim()

android {
    namespace = "com.geotagphotogenerator"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.geotagphotogenerator"
        minSdk = 23
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey
        buildConfigField(
            "boolean",
            "MAPS_API_KEY_CONFIGURED",
            mapsApiKey.isNotEmpty().toString(),
        )
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.exifinterface)
    implementation(libs.maps.compose)
    implementation(libs.osmdroid.android)

    debugImplementation(libs.androidx.compose.ui.tooling)
}
