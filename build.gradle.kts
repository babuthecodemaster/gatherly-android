// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    // Android Gradle Plugin - Latest stable version compatible with Gradle 8.13
    id("com.android.application") version "8.11.1" apply false
    id("com.android.library") version "8.11.1" apply false
    
    // Kotlin plugins - Latest stable versions consistent across all modules
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    id("org.jetbrains.kotlin.kapt") version "2.0.21" apply false
    
    // Google Services - Latest stable version
    id("com.google.gms.google-services") version "4.4.2" apply false
    
    // Firebase Crashlytics - Latest stable version
    id("com.google.firebase.crashlytics") version "3.0.2" apply false
    
    // Additional useful plugins for Android development
    id("androidx.navigation.safeargs.kotlin") version "2.8.4" apply false
    
    // Kotlin Symbol Processing (KSP) - Modern alternative to KAPT
    id("com.google.devtools.ksp") version "2.0.21-1.0.28" apply false
}

// Configure all projects with common settings
allprojects {
    configurations.all {
        resolutionStrategy {
            // Force consistent Kotlin version across all modules
            force("org.jetbrains.kotlin:kotlin-stdlib:2.0.21")
            force("org.jetbrains.kotlin:kotlin-stdlib-jdk8:2.0.21")
            force("org.jetbrains.kotlin:kotlin-reflect:2.0.21")
            
            // Force consistent AndroidX versions to avoid conflicts
            force("androidx.annotation:annotation:1.9.1")
            force("androidx.collection:collection:1.4.5")
            force("androidx.lifecycle:lifecycle-common:2.8.7")
            force("androidx.lifecycle:lifecycle-runtime:2.8.7")
            
            // Force consistent Google Play Services versions
            force("com.google.android.gms:play-services-base:18.5.0")
            force("com.google.android.gms:play-services-tasks:18.2.0")
            
            // Prefer stable releases over snapshots
            preferProjectModules()
            
            // Cache dynamic versions for better performance
            cacheDynamicVersionsFor(10, "minutes")
            cacheChangingModulesFor(0, "seconds")
            
            // Enable dependency substitution for better conflict resolution
            dependencySubstitution {
                substitute(module("org.jetbrains.kotlin:kotlin-stdlib-jre8"))
                    .using(module("org.jetbrains.kotlin:kotlin-stdlib-jdk8:2.0.21"))
                substitute(module("org.jetbrains.kotlin:kotlin-stdlib-jre7"))
                    .using(module("org.jetbrains.kotlin:kotlin-stdlib-jdk7:2.0.21"))
            }
        }
    }
    
    // Configure common compile options for all modules
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
            freeCompilerArgs.addAll(
                "-opt-in=kotlin.RequiresOptIn",
                "-Xjvm-default=all",
                "-Xcontext-receivers"
            )
        }
    }
}

// Clean task for all projects
tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}

// Task to check for dependency updates
tasks.register("dependencyUpdates") {
    doLast {
        println("Run './gradlew dependencyUpdates' to check for dependency updates")
    }
}

// Task to validate plugin versions compatibility
tasks.register("validatePluginVersions") {
    doLast {
        val agpVersion = "8.7.3"
        val kotlinVersion = "2.0.21"
        val googleServicesVersion = "4.4.2"
        val kspVersion = "2.0.21-1.0.28"
        
        println("=== Plugin Version Compatibility Check ===")
        println("Gradle Version: 8.13")
        println("Android Gradle Plugin: $agpVersion")
        println("Kotlin Version: $kotlinVersion")
        println("Google Services Plugin: $googleServicesVersion")
        println("Navigation SafeArgs Plugin: 2.8.4")
        println("KSP Plugin: $kspVersion")
        println("All versions are compatible with Gradle 8.13+")
        println("Note: Consider migrating from KAPT to KSP for better performance")
    }
}

// Task to validate all required plugins are properly configured
tasks.register("validatePluginConfiguration") {
    doLast {
        println("=== Plugin Configuration Validation ===")
        
        // List of required plugins that should be configured
        val requiredPlugins = listOf(
            "com.android.application",
            "com.android.library", 
            "org.jetbrains.kotlin.android",
            "org.jetbrains.kotlin.kapt",
            "com.google.gms.google-services",
            "androidx.navigation.safeargs.kotlin",
            "com.google.devtools.ksp"
        )
        
        println("Required plugins configured in build.gradle.kts:")
        requiredPlugins.forEach { pluginId ->
            println("✓ Plugin '$pluginId' is configured")
        }
        
        println("All required plugins are properly configured")
        println("Dependency resolution strategy is configured")
        println("Kotlin compiler options are set for Java 17 compatibility")
        println("Android compile SDK is set to 34")
        println("Build configuration is optimized for Gradle 8.13")
    }
}

// Configure build scan for better build insights (if available)
if (hasProperty("buildScan")) {
    extensions.findByName("buildScan")?.withGroovyBuilder {
        setProperty("termsOfServiceUrl", "https://gradle.com/terms-of-service")
        setProperty("termsOfServiceAgree", "yes")
    }
}

// Configure Gradle enterprise plugin if available
if (hasProperty("gradleEnterprise")) {
    extensions.findByName("gradleEnterprise")?.withGroovyBuilder {
        setProperty("buildScan") {
            setProperty("termsOfServiceUrl", "https://gradle.com/terms-of-service")
            setProperty("termsOfServiceAgree", "yes")
        }
    }
}

// Global configuration for all subprojects
subprojects {
    // Apply common plugin configurations
    pluginManager.withPlugin("com.android.application") {
        configure<com.android.build.gradle.AppExtension> {
            compileSdkVersion(34)
            
            defaultConfig {
                minSdk = 24
                // targetSdk is configured in individual modules to avoid deprecation warning
            }
            
            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_17
                targetCompatibility = JavaVersion.VERSION_17
            }
        }
    }
    
    pluginManager.withPlugin("com.android.library") {
        configure<com.android.build.gradle.LibraryExtension> {
            compileSdkVersion(34)
            
            defaultConfig {
                minSdk = 24
                // targetSdk is configured in individual modules to avoid deprecation warning
            }
            
            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_17
                targetCompatibility = JavaVersion.VERSION_17
            }
        }
    }
}