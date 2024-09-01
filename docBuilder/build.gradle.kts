import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalDistributionDsl

plugins {
    kotlin("multiplatform")
}

repositories {
    mavenCentral()
}

kotlin {
    js(IR) {
        browser {
            @OptIn(ExperimentalDistributionDsl::class)
            distribution { outputDirectory.set(projectDir.resolve("build").resolve("public")) }
        }

        binaries.executable()

        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            freeCompilerArgs.add("-Xcontext-receivers")
        }

        dependencies {
            commonMainImplementation("org.jetbrains:markdown:0.7.3")
        }
    }

    sourceSets {
        val jsMain by getting {
            resources.srcDir(rootDir.resolve("docs"))
        }
    }
}