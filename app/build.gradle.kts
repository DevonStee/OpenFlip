import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}


android {
    namespace = "com.bokehforu.openflip"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.bokehforu.openflip"
        minSdk = 26
        targetSdk = 36
        versionCode = 11
        versionName = "0.6.0-beta"


        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            val localProperties = project.rootProject.file("local.properties").let { file ->
                if (file.exists()) {
                    Properties().apply { load(file.inputStream()) }
                } else {
                    null
                }
            }

            val keystorePath = System.getenv("KEYSTORE_FILE")
                ?: localProperties?.getProperty("signing.keystore")

            val keystoreFile = keystorePath?.let { file(it) }

            if (keystoreFile != null && keystoreFile.exists()) {
                storeFile = keystoreFile
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                    ?: localProperties?.getProperty("signing.storePassword")
                keyAlias = System.getenv("KEY_ALIAS")
                    ?: localProperties?.getProperty("signing.keyAlias")
                keyPassword = System.getenv("KEY_PASSWORD")
                    ?: localProperties?.getProperty("signing.keyPassword")
            }
        }
    }

    buildTypes {
        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-DEBUG"
        }
        release {
            isMinifyEnabled = true
            val releaseConfig = signingConfigs.getByName("release")
            if (releaseConfig.storeFile != null) {
                signingConfig = releaseConfig
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlinOptions {
        jvmTarget = "21"
    }
    buildFeatures {
        viewBinding = true
        compose = true
    }
    lint {
        // Work around current AGP/Kotlin lint crashes in test source analysis.
        // Keep app source lint active for CI signal quality.
        checkTestSources = false
    }
}

dependencies {
    implementation(project(":core"))
    implementation(project(":data"))
    implementation(project(":domain"))
    implementation(project(":feature-settings"))
    implementation(project(":feature-clock"))
    implementation(project(":feature-chime"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.fragment.ktx)

    // Compose
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
    
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.process)
    
    // Cloudy - Liquid glass blur effect
    implementation(libs.skydoves.cloudy)
    
    // Hilt - Dependency Injection
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Memory leak detection (debug only)
    debugImplementation(libs.leakcanary.android)
    
    // Unit Test dependencies
    testImplementation(libs.junit)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.robolectric)
    
    // Hilt Testing
    testImplementation(libs.hilt.android.testing)
    kspTest(libs.hilt.compiler)
    
    // Android Instrumented Test dependencies
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.androidx.test.runner)
}



// Work around AGP/Kotlin lint analyzer crashes in test-source pipelines.
// Keep main-source lint tasks enabled.
tasks.matching {
    it.name.startsWith("lintAnalyze") &&
        (it.name.endsWith("UnitTest") || it.name.endsWith("AndroidTest"))
}.configureEach {
    enabled = false
}

kotlin {
    jvmToolchain(21)
}
