apply(from = rootProject.file("gradle/publish_jitpack.gradle.kts"))
plugins {
    id("java-library")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    testImplementation(libs.junit)
}