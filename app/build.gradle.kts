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
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
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

// Task to check and setup chime audio files
tasks.register("setupChimeAudios") {
    description = "Check and setup hourly chime audio files"
    group = "build"
    
    doLast {
        val rawDir = file("src/main/res/raw")
        val sourceFile = file("$rawDir/chime_sound.mp3")
        
        // Check if source file exists
        if (!sourceFile.exists()) {
            throw GradleException("""
                ❌ chime_sound.mp3 not found!
                
                Please add chime_sound.mp3 to: app/src/main/res/raw/
                
                This is the base audio file used to generate hourly chimes.
            """.trimIndent())
        }
        
        // Check if all chime files exist
        val missingFiles = (1..12).filter { count ->
            !file("$rawDir/chime_${String.format("%02d", count)}.mp3").exists()
        }
        
        if (missingFiles.isEmpty()) {
            println("✓ All chime audio files exist.")
            return@doLast
        }
        
        // Check ffmpeg
        val checkFfmpeg = Runtime.getRuntime().exec("which ffmpeg")
        checkFfmpeg.waitFor()
        if (checkFfmpeg.exitValue() != 0) {
            throw GradleException("""
                ❌ ffmpeg not found!
                
                Hourly Chime audio generation requires ffmpeg.
                
                Install with:
                  macOS: brew install ffmpeg
                  Ubuntu: sudo apt-get install ffmpeg
                  Windows: https://ffmpeg.org/download.html
                
                Or manually generate audio files and place them in:
                app/src/main/res/raw/
                
                Required files:
                ${missingFiles.joinToString("\n") { "  - chime_${String.format("%02d", it)}.mp3" }}
            """.trimIndent())
        }
        
        println("🎵 Generating ${missingFiles.size} chime audio files...")
        
        // Generate missing files
        missingFiles.forEach { count ->
            val outputFile = file("$rawDir/chime_${String.format("%02d", count)}.mp3")
            
            if (count == 1) {
                // Just copy for 1 chime
                sourceFile.copyTo(outputFile, overwrite = true)
                println("  ✓ chime_01.mp3 (copied)")
            } else {
                // Generate with ffmpeg
                val inputs = (0 until count).joinToString(" ") { "-i ${sourceFile.absolutePath}" }
                val delays = (0 until count).joinToString(";") { i ->
                    val delayMs = (i * 1200).toString()
                    "[$i:a]adelay=$delayMs|$delayMs[a$i]"
                }
                val mix = (0 until count).joinToString("") { "[a$it]" }
                val filter = "$delays;$mix" + "amix=inputs=$count:duration=longest:normalize=0[out]"
                
                val cmdList = mutableListOf("ffmpeg", "-y")
                repeat(count) { cmdList.addAll(listOf("-i", sourceFile.absolutePath)) }
                cmdList.addAll(listOf(
                    "-filter_complex", filter,
                    "-map", "[out]",
                    "-c:a", "libmp3lame",
                    "-q:a", "2",
                    outputFile.absolutePath
                ))
                
                val process = ProcessBuilder(cmdList)
                    .redirectErrorStream(true)
                    .start()
                process.waitFor()
                
                if (process.exitValue() == 0) {
                    println("  ✓ chime_${String.format("%02d", count)}.mp3 ($count chimes)")
                } else {
                    val error = process.inputStream.bufferedReader().readText()
                    println("  ❌ ffmpeg error: $error")
                    throw GradleException("Failed to generate chime_${String.format("%02d", count)}.mp3")
                }
            }
        }
        
        println("🎉 Chime audio generation complete!")
    }
}

val autoGenerateChimes = providers.environmentVariable("OPENFLIP_AUTO_GENERATE_CHIMES")
    .map { it.equals("true", ignoreCase = true) }
    .orElse(false)

// Keep chime generation explicit to avoid non-hermetic preBuild behavior.
if (autoGenerateChimes.get()) {
    tasks.named("preBuild").configure {
        dependsOn("setupChimeAudios")
    }
}

// Work around AGP/Kotlin lint analyzer crashes in test-source pipelines.
// Keep main-source lint tasks enabled.
tasks.matching {
    it.name.startsWith("lintAnalyze") &&
        (it.name.endsWith("UnitTest") || it.name.endsWith("AndroidTest"))
}.configureEach {
    enabled = false
}
