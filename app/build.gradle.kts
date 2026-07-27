import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.kotlin.compiler)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
}

val env = Properties().apply {
    rootProject.file(".env").takeIf { it.exists() }?.let { load(FileInputStream(it)) }
}

val releaseVersionCodeInput = providers.environmentVariable("VERSION_CODE").orNull
val releaseVersionCode = releaseVersionCodeInput?.toIntOrNull()
val releaseVersionName = providers.environmentVariable("VERSION_NAME").orNull?.takeIf { it.isNotBlank() }
val releaseKeystorePath = providers.environmentVariable("ANDROID_KEYSTORE_PATH").orNull
val releaseKeystorePassword = providers.environmentVariable("ANDROID_KEYSTORE_PASSWORD").orNull
val releaseKeyAlias = providers.environmentVariable("ANDROID_KEY_ALIAS").orNull
val releaseKeyPassword = providers.environmentVariable("ANDROID_KEY_PASSWORD").orNull
val releaseSigningConfigured = listOf(
    releaseKeystorePath,
    releaseKeystorePassword,
    releaseKeyAlias,
    releaseKeyPassword,
).all { !it.isNullOrBlank() }
val releaseTaskRequested = gradle.startParameter.taskNames.any {
    it.contains("release", ignoreCase = true)
}

check(releaseVersionCodeInput == null || releaseVersionCode != null && releaseVersionCode > 0) {
    "VERSION_CODE must be a positive 32-bit integer."
}
check(!releaseTaskRequested || releaseSigningConfigured) {
    "Release signing is not configured. Set ANDROID_KEYSTORE_PATH, " +
        "ANDROID_KEYSTORE_PASSWORD, ANDROID_KEY_ALIAS, and ANDROID_KEY_PASSWORD."
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

tasks.withType<KotlinCompile>().configureEach {
    if (name.contains("Release", ignoreCase = true)) {
        compilerOptions {
            freeCompilerArgs.addAll(
                "-Xno-call-assertions",
                "-Xno-param-assertions",
                "-Xno-receiver-assertions",
            )
        }
    }
}

android {
    namespace = "ru.bitvibe.waggy"
    compileSdk = 37

    defaultConfig {
        applicationId = "ru.bitvibe.waggy"
        minSdk = 24
        targetSdk = 37
        versionCode = releaseVersionCode ?: 1
        versionName = releaseVersionName ?: "1.0"
        buildConfigField("String", "BASE_URL", env.getProperty("BASE_URL", "\"\""))
        buildConfigField(
            "String",
            "GITHUB_RELEASES_API_URL",
            "\"https://api.github.com/repos/SS1GGzxc/Waggy/\"",
        )
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (releaseSigningConfigured) {
            create("release") {
                storeFile = rootProject.file(requireNotNull(releaseKeystorePath))
                storePassword = releaseKeystorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            signingConfig = signingConfigs.findByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    packaging {
        resources {
            excludes += setOf(
                "META-INF/LICENSE*",
                "META-INF/NOTICE*",
                "META-INF/DEPENDENCIES",
                "META-INF/*.kotlin_module",
                "kotlin-tooling-metadata.json",
                "DebugProbesKt.bin",
                "**.kotlin_builtins",
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

dependencies {
    // Compose BOM
    implementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(platform(libs.androidx.compose.bom))

    // Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose UI
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.compose.material3)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Navigation + Lifecycle
    implementation(libs.androidx.navigation)
    implementation(libs.androidx.lifecycle)

    // Material
    implementation(libs.androidx.icons)
    implementation(libs.google.material)

    // Hilt
    implementation(libs.hilt.android)
    implementation(libs.hilt.compose)
    implementation(libs.hilt.work)
    ksp(libs.hilt.compiler)
    ksp(libs.hilt.compiler.androidx)

    // Retrofit + Kotlin serialization
    implementation(libs.retrofit2.retrofit)
    implementation(libs.retrofit2.kotlinx)
    implementation(libs.kotlin.json)

    // Room
    implementation(libs.room.runtime)
    ksp(libs.room.compiler)

    // Coil
    implementation(libs.coli.compose)
    implementation(libs.coli.okhttp3)

    // Glance + WorkManager
    implementation(libs.glance.appwidget)
    implementation(libs.work.runtime.ktx)

    // Tests
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    // MLKit Segmentation
    implementation(libs.mlkit.segmentation)

    // Firebase
    implementation(platform(libs.firebase.bom))

    // Crashlytics
    implementation(libs.firebase.crashlytics)

    // Analytics
    implementation(libs.firebase.analytics)
}
