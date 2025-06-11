apply(from = rootProject.file("gradle/publish_jitpack.gradle.kts"))
plugins {
    id("java-library")
    kotlin("jvm")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    compileOnly(libs.kotlin.stdlib)
    testImplementation(libs.kotlin.stdlib)
    testImplementation(libs.junit)
}