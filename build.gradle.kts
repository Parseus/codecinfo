plugins {
    alias(libs.plugins.agp) apply false
    alias(libs.plugins.kotlin) apply false
}

tasks.register<Delete>("clean") {
    delete(fileTree(rootProject.layout.buildDirectory))
}
