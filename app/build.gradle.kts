plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
}

object Baseline {
    // Locked dependency versions for Kotlin 2.0.x toolchain compatibility.
    const val coreKtx = "1.15.0"
    const val appcompat = "1.7.1"
    const val material = "1.13.0"
    const val constraintLayout = "2.2.1"
    const val lifecycle = "2.8.7"
    const val activity = "1.9.3"
    const val fragment = "1.8.5"
    const val composeBom = "2024.12.01"
    const val cloudy = "0.2.4"
    const val hilt = "2.55"
    const val leakCanary = "2.12"
    const val junit4 = "4.13.2"
    const val mockito = "5.8.0"
    const val mockitoKotlin = "5.2.1"
    const val kotlinTest = "2.0.21"
    const val coroutinesTest = "1.9.0"
    const val androidxTestCore = "1.6.1"
    const val robolectric = "4.13"
    const val androidxJunitExt = "1.3.0"
    const val espresso = "3.7.0"
    const val testRules = "1.7.0"
    const val testRunner = "1.7.0"
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

    implementation("androidx.core:core-ktx:${Baseline.coreKtx}")
    implementation("androidx.appcompat:appcompat:${Baseline.appcompat}")
    implementation("com.google.android.material:material:${Baseline.material}")
    implementation("androidx.constraintlayout:constraintlayout:${Baseline.constraintLayout}")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:${Baseline.lifecycle}")
    implementation("androidx.activity:activity-ktx:${Baseline.activity}")
    implementation("androidx.fragment:fragment-ktx:${Baseline.fragment}")

    // Compose
    val composeBom = platform("androidx.compose:compose-bom:${Baseline.composeBom}")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    
    implementation("androidx.activity:activity-compose:${Baseline.activity}")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:${Baseline.lifecycle}")
    implementation("androidx.lifecycle:lifecycle-process:${Baseline.lifecycle}")
    
    // Cloudy - Liquid glass blur effect
    implementation("com.github.skydoves:cloudy:${Baseline.cloudy}")
    
    // Hilt - Dependency Injection
    implementation("com.google.dagger:hilt-android:${Baseline.hilt}")
    ksp("com.google.dagger:hilt-compiler:${Baseline.hilt}")

    // Memory leak detection (debug only)
    debugImplementation("com.squareup.leakcanary:leakcanary-android:${Baseline.leakCanary}")
    
    // Unit Test dependencies
    testImplementation("junit:junit:${Baseline.junit4}")
    testImplementation("org.mockito:mockito-core:${Baseline.mockito}")
    testImplementation("org.mockito.kotlin:mockito-kotlin:${Baseline.mockitoKotlin}")
    testImplementation("org.jetbrains.kotlin:kotlin-test:${Baseline.kotlinTest}")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${Baseline.coroutinesTest}")
    testImplementation("androidx.test:core:${Baseline.androidxTestCore}")
    testImplementation("org.robolectric:robolectric:${Baseline.robolectric}")
    
    // Hilt Testing
    testImplementation("com.google.dagger:hilt-android-testing:${Baseline.hilt}")
    kspTest("com.google.dagger:hilt-compiler:${Baseline.hilt}")
    
    // Android Instrumented Test dependencies
    androidTestImplementation("androidx.test.ext:junit:${Baseline.androidxJunitExt}")
    androidTestImplementation("androidx.test.espresso:espresso-core:${Baseline.espresso}")
    androidTestImplementation("androidx.test:rules:${Baseline.testRules}")
    androidTestImplementation("androidx.test:runner:${Baseline.testRunner}")
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
