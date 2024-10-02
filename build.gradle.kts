group = "dev.supachain"
version = "0.1.0"

val ktorVersion: String = "2.3.11"
val logbackVersion: String = "1.5.6"
val kotlinVersion: String = "2.0.0"
val kotlinxSerializationJson: String = "1.6.3"
val slf4jApi: String = "1.7.32"
val kotlinxDatetime: String = "0.6.0"

plugins {
    val kotlinVersion = "2.0.20"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion
    id("maven-publish")
}

repositories {
    mavenCentral()
}

publishing {
    publications {
        create<MavenPublication>("gpr") {
            from(components["java"]) // Publish the Java component
            groupId = project.group.toString() // Group ID for Maven
            artifactId = project.name // Artifact ID (name of the project)
            version = project.version.toString() // Project version
        }
    }

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/chazpondre/supachain")
            credentials {
                username = project.findProperty("GITHUB_ACTOR") as String? ?: System.getenv("USERNAME")
                password = project.findProperty("GITHUB_TOKEN") as String? ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
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

    // Log
    implementation("org.slf4j:slf4j-api:$slf4jApi")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")

    // Tests
    testImplementation(kotlin("test"))
    testImplementation("io.ktor:ktor-client-mock:$ktorVersion")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}


subprojects {
    if (project.parent?.name == "examples") {
        apply(plugin = "org.jetbrains.kotlin.jvm")
    }

    repositories {
        mavenCentral()
    }

    dependencies {

    }
}
