plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("kotlin-parcelize")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

android {
    namespace = "com.cosmic.gatherly"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.cosmic.gatherly"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            isDebuggable = true
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            // Enable test coverage for debug builds
            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Enable R8 optimizations
            isDebuggable = false
            // Enable build config optimization
            buildConfigField("boolean", "DEBUG", "false")
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }
    
    kotlinOptions {
        jvmTarget = "17"
        // Enable incremental compilation and optimizations
        freeCompilerArgs += listOf(
            "-opt-in=kotlin.RequiresOptIn",
            "-Xjvm-default=all"
        )
        // Suppress warnings for better build performance
        allWarningsAsErrors = false
    }
    
    buildFeatures {
        viewBinding = true
        dataBinding = true
        buildConfig = true
        // Disable unused features for better build performance
        aidl = false
        renderScript = false
        resValues = false
        shaders = false
    }
    
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
        animationsDisabled = true
    }
    
    // Packaging options to avoid conflicts (updated to use packaging block)
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/DEPENDENCIES"
            excludes += "/META-INF/LICENSE"
            excludes += "/META-INF/LICENSE.txt"
            excludes += "/META-INF/NOTICE"
            excludes += "/META-INF/NOTICE.txt"
        }
    }
    
    // Lint options for better build performance
    lint {
        checkReleaseBuilds = false
        abortOnError = false
        disable += setOf("MissingTranslation", "ExtraTranslation", "UnusedResources")
        baseline = file("lint-baseline.xml")
    }
}

// KAPT Configuration for optimized annotation processing
kapt {
    correctErrorTypes = true
    useBuildCache = true
    mapDiagnosticLocations = true
    javacOptions {
        // Increase memory for KAPT processing
        option("-Xmx1024m")
    }
    arguments {
        arg("room.schemaLocation", "$projectDir/schemas")
        arg("room.incremental", "true")
        arg("room.expandProjection", "true")
    }
}

// Dependency resolution strategy for AndroidX consistency
configurations.all {
    resolutionStrategy {
        // Force consistent AndroidX versions
        force("androidx.annotation:annotation:1.8.2")
        force("androidx.collection:collection:1.4.4")
        force("androidx.lifecycle:lifecycle-common:2.8.6")
        force("androidx.lifecycle:lifecycle-runtime:2.8.6")
        force("androidx.lifecycle:lifecycle-livedata-core:2.8.6")
        force("androidx.lifecycle:lifecycle-viewmodel:2.8.6")
        
        // Prefer stable releases over alpha/beta versions where possible
        preferProjectModules()
        
        // Enable dependency locking for reproducible builds
        activateDependencyLocking()
    }
}

dependencies {
    // Core library desugaring for Java 8+ APIs on older Android versions
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")
    
    // Core Android - Latest stable versions (optimized for compatibility)
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.fragment:fragment-ktx:1.8.4")
    implementation("androidx.activity:activity-ktx:1.9.2")
    
    // Architecture Components - Aligned to consistent versions
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.6")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.6")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation("androidx.navigation:navigation-fragment-ktx:2.8.2")
    implementation("androidx.navigation:navigation-ui-ktx:2.8.2")
    
    // UI Components - Updated to latest stable versions
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("androidx.viewpager2:viewpager2:1.1.0")
    
    // Room Database (for offline caching) - Fixed duplicates and optimized
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    implementation("androidx.room:room-rxjava3:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    
    // WorkManager (for background sync) - Updated to latest stable
    implementation("androidx.work:work-runtime-ktx:2.9.1")
    
    // Security (for encrypted preferences) - Updated to stable release
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    
    // Networking (Enhanced)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.retrofit2:adapter-rxjava3:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.squareup.okhttp3:okhttp-sse:4.12.0")
    
    // WebSocket for real-time features
    implementation("org.java-websocket:Java-WebSocket:1.5.3")
    
    // Reactive Programming
    implementation("io.reactivex.rxjava3:rxjava:3.1.8")
    implementation("io.reactivex.rxjava3:rxandroid:3.0.2")
    
    // JSON Processing
    implementation("com.google.code.gson:gson:2.10.1")
    
    // Image loading
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("de.hdodenhof:circleimageview:3.1.0")
    kapt("com.github.bumptech.glide:compiler:4.16.0")
    
    // Permissions
    implementation("com.karumi:dexter:6.2.3")
    
    // Date/Time
    implementation("com.jakewharton.threetenabp:threetenabp:1.4.6")
    
    // Firebase - Latest stable versions for authentication and core services
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.firebase:firebase-config-ktx")
    
    // Logging
    implementation("com.jakewharton.timber:timber:5.0.1")
    implementation("org.slf4j:slf4j-android:1.7.36")
    
    // Testing - Updated to consistent versions
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.7.0")
    testImplementation("org.mockito:mockito-android:5.7.0")
    testImplementation("org.robolectric:robolectric:4.13")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("androidx.test:core:1.6.1")
    testImplementation("androidx.test:runner:1.6.2")
    testImplementation("androidx.test:rules:1.6.1")
    testImplementation("androidx.test.ext:junit:1.2.1")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    
    // Android Instrumented Testing - Updated to consistent versions
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.6.1")
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.6.1")
    androidTestImplementation("androidx.test:runner:1.6.2")
    androidTestImplementation("androidx.test:rules:1.6.1")
    androidTestImplementation("androidx.room:room-testing:2.6.1")
    androidTestImplementation("org.mockito:mockito-android:5.7.0")
}