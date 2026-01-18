plugins {
    alias(libs.plugins.agp) apply false
}

buildscript {
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin") {
            version { strictly(libs.versions.kotlin.get()) }
        }
    }
}

tasks.register<Delete>("clean") {
    delete(fileTree(rootProject.layout.buildDirectory))
}
