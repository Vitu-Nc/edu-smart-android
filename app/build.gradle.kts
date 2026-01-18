plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.example.edu_smart"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.edu_smart"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        // Kotlin 1.9.24 ↔ Compose Compiler from libs.versions.toml
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    // Read Gemini key from gradle.properties
    val geminiApiKey: String = project.findProperty("GEMINI_API_KEY") as? String ?: ""

    buildTypes {
        debug {
            buildConfigField(
                "String",
                "GEMINI_API_KEY",
                "\"$geminiApiKey\""
            )
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField(
                "String",
                "GEMINI_API_KEY",
                "\"$geminiApiKey\""
            )
        }
    }

    // (Optional) Kotlin stdlib pinning
    configurations.all {
        resolutionStrategy {
            force("org.jetbrains.kotlin:kotlin-stdlib:1.9.24")
            force("org.jetbrains.kotlin:kotlin-stdlib-common:1.9.24")
            force("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.9.24")
            force("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.9.24")
        }
    }
}

dependencies {
    // Kotlin stdlib
    implementation(kotlin("stdlib"))

    // Compose BOM
    implementation(platform(libs.androidx.compose.bom))

    // Compose UI & Material3
    implementation(libs.androidx.ui)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.ui.tooling.preview)
    debugImplementation(libs.androidx.ui.tooling)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.runtime.saveable)

    // Material Components (XML-based)
    implementation("com.google.android.material:material:1.12.0")

    // Icons (inherits version from BOM)
    implementation("androidx.compose.material:material-icons-extended")

    // Core & Lifecycle
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // OkHttp / Retrofit
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:okhttp-sse:4.12.0")
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    // WebView APIs
    implementation("androidx.webkit:webkit:1.11.0")

    // Coil
    implementation("io.coil-kt:coil-compose:2.4.0")

    // Accompanist
    implementation("com.google.accompanist:accompanist-placeholder-material:0.32.0")

    // Firebase (BOM)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)

    // Splash Screen
    implementation(libs.androidx.core.splashscreen)

    // Tests
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
