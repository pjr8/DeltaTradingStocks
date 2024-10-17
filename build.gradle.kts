plugins {
    kotlin("jvm") version "2.0.20"
}

group = "me.paulrobinson"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven(url="https://jitpack.io")
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("com.github.polygon-io:client-jvm:5.1.2")
    implementation("org.mongodb:mongodb-driver-sync:5.1.0")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(18)
}