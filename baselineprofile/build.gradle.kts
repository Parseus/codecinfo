plugins {
    alias(libs.plugins.android.test)
    alias(libs.plugins.baselineprofile)
}

android {
    namespace = "pl.parseus.baselineprofile"
    compileSdk {
        version = release(37) {
            minorApiLevel = 0
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    defaultConfig {
        minSdk = 28
        targetSdk = 37

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        buildConfig = true
    }

    targetProjectPath = ":app"

    flavorDimensions += listOf("app", "platform")
    productFlavors {
        create("nonFree") { dimension = "app" }
        create("standard") { dimension = "app" }
        create("mobile") { dimension = "platform" }
        create("tv") { dimension = "platform" }
    }

}

baselineProfile {
    useConnectedDevices = true
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.junit)
    implementation(libs.androidx.espresso.core)
    implementation(libs.androidx.uiautomator)
    implementation(libs.androidx.benchmark.macro.junit4)
}

androidComponents {
    beforeVariants(selector().all()) { variantBuilder ->
        if ("nonFreeTv" == variantBuilder.flavorName) {
            variantBuilder.enable = false
        }
    }
    onVariants { v ->
        val artifactsLoader = v.artifacts.getBuiltArtifactsLoader()
        v.instrumentationRunnerArguments.put(
            "targetAppId",
            v.testedApks.map { artifactsLoader.load(it)?.applicationId }
        )
    }
}