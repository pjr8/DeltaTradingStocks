import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "2.0.20"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    application
}

group = "me.paulrobinson"
version = ""

repositories {
    mavenCentral()
    maven(url="https://jitpack.io")
    maven(url="http://clojars.org/repo/") {
        isAllowInsecureProtocol = true
    }
}

application {
    mainClass = "me.paulrobinson.MainKt"
}


dependencies {
    testImplementation(kotlin("test"))
    implementation("com.github.polygon-io:client-jvm:5.1.2")
    implementation("org.mongodb:mongodb-driver-sync:5.1.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("com.esotericsoftware:kryo:5.6.2")
    //implementation("kryonet:kryonet:2.21")
    implementation("com.github.crykn:kryonet:2.22.9")
    implementation("org.apache.flink:flink-streaming-java:1.20.0")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(18)
}

tasks {
    withType<ShadowJar> {
        archiveClassifier.set("")
        manifest {
            attributes["Main-Class"] = application.mainClass.get()
        }
        mergeServiceFiles()
    }
}