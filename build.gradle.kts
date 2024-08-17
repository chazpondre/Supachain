val ktorVersion: String = "2.3.11"
val logbackVersion: String = "1.5.6"
val kotlinVersion: String = "2.0.0"
val kotlinxSerializationJson: String = "1.6.3"
val slf4jApi: String = "1.7.32"
val kotlinxDatetime: String = "0.6.0"

plugins {
    kotlin("jvm") version "2.0.0"
    kotlin("plugin.serialization") version "2.0.0"
}

group = "ai.anything"
version = "0.1.0-alpha"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("reflect"))
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:$kotlinxDatetime")

    // KTOR Client
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-client-logging:$ktorVersion")

    // Serializer
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationJson")
    implementation("org.slf4j:slf4j-api:$slf4jApi")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}

