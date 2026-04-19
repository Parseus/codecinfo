plugins {
    alias(libs.plugins.agp)
    alias(libs.plugins.serialization)
    alias(libs.plugins.baselineprofile)
}

android {
    namespace = "com.parseus.codecinfo"
    compileSdk {
        version = release(37) {
            minorApiLevel = 0
        }
    }
    defaultConfig {
        applicationId = "com.parseus.codecinfo"
        minSdk = 23
        targetSdk = 37
        versionCode = 29
        versionName = "2.10.0"
    }
    buildTypes {
        getByName("debug") {
            versionNameSuffix = "-dev"
        }
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")

            @Suppress("UnstableApiUsage")
            optimization.keepRules {
                ignoreFrom("androidx.core:core")
                ignoreFrom("androidx.recyclerview:recyclerview")
            }

            packaging {
                resources.excludes += "DebugProbesKt.bin"
            }
        }
    }

    buildFeatures {
        buildConfig = true
    }

    androidResources {
        @Suppress("UnstableApiUsage")
        localeFilters += listOf("en")
    }

    flavorDimensions += listOf("app", "platform")

    productFlavors {
        create("nonFree") {
            dimension = "app"

            dependenciesInfo {
                includeInApk = false
            }
        }

        create("standard") {
            dimension = "app"

            dependenciesInfo {
                includeInApk = false
            }
        }

        create("mobile") {
            dimension = "platform"
            versionCode = 10000 + (android.defaultConfig.versionCode ?: 0)
        }

        create("tv") {
            dimension = "platform"
            versionCode = 20000 + (android.defaultConfig.versionCode ?: 0)
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    packaging {
        jniLibs {
            excludes += listOf("kotlin/**")
        }
        resources {
            excludes += listOf("kotlin/**", "**/*.kotlin_metadata", "META-INF/*.kotlin_module",
                         "META-INF/*.version")
        }
    }
}

baselineProfile {
    dexLayoutOptimization = true
    saveInSrc = true
    
    warnings {
        disabledVariants = false
    }
}

androidComponents {
    beforeVariants(selector().all()) { variantBuilder ->
        if ("nonFreeTv" == variantBuilder.flavorName) {
            variantBuilder.enable = false
        }
    }
}

kotlin {
    jvmToolchain(17)

    compilerOptions {
        freeCompilerArgs.addAll(
            "-Xannotation-default-target=param-property",
            "-Xjsr305=strict",
            "-Xemit-jvm-type-annotations",
            "-jvm-default=enable",
            "-Xjspecify-annotations=strict"
        )
    }
}

val nonFreeMobileImplementation: Configuration by configurations.creating
val standardMobileImplementation: Configuration by configurations.creating

dependencies {
    implementation(libs.coroutines)

    implementation(libs.androidx.activity)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core)
    implementation(libs.androidx.datastore.okio)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.preference)
    implementation(libs.androidx.profileinstaller)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.startup)

    implementation(libs.plumber.android)
    implementation(libs.okio)
    implementation(libs.serialization.json)

    debugImplementation(libs.leakcanary.android)

    "tvImplementation"(libs.androidx.leanback)
    "tvImplementation"(libs.androidx.leanback.preference)

    standardMobileImplementation(libs.hiddenapibypass)

    "mobileImplementation"(libs.androidx.browser)
    "mobileImplementation"(libs.androidx.constraintlayout)
    "mobileImplementation"(libs.androidx.core.splashscreen)
    "mobileImplementation"(libs.androidx.palette)
    "mobileImplementation"(libs.androidx.window)
    "mobileImplementation"(libs.monetcompat)
    "mobileImplementation"(libs.licenser)
    "mobileImplementation"(libs.material)

    nonFreeMobileImplementation(libs.app.update)
    nonFreeMobileImplementation(libs.app.update.ktx)
    nonFreeMobileImplementation(libs.review)
    nonFreeMobileImplementation(libs.review.ktx)

    "baselineProfile"(project(":baselineprofile"))
}