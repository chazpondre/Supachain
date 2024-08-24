import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.gradle.api.tasks.Copy

import java.nio.file.Paths

plugins {
    kotlin("multiplatform")
}

repositories {
    mavenCentral()
}

kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        binaries.executable()
        browser()
    }

    sourceSets {
        val wasmJsMain by getting
        val wasmJsTest by getting
    }
}

tasks.register<Copy>("makeDocs") {
    val resourcesDir = "$projectDir/src/wasmJsMain/resources"
    val productionExecutableDir = "$projectDir/build/compileSync/wasmJs/main/productionExecutable/kotlin"
    val outputDir = "$projectDir/docs"
    val parentName = project.parent?.name?.let { "$it-" } ?: ""
    var isHtmlModified = false

    group = "build"
    description =
        "Copies the necessary Wasm, JS (or MJS), and HTML files to the " +
                "dist/wasmJs/productionExecutable directory."

    dependsOn("wasmJsProductionExecutableCompileSync")

    // Delete existing files in the output directory
    doFirst { delete(fileTree(outputDir)) }

    from(Paths.get(productionExecutableDir)) {
        include("*.wasm", "*.mjs", "*.js.map")
        rename("${parentName}${project.name}-wasm-js.mjs", "${project.name}.mjs")
    }

    from(Paths.get(resourcesDir)) {
        include("index.html")
        filter { line ->
            if (!isHtmlModified) {
                isHtmlModified = true
                "<!--timeCreated:${System.currentTimeMillis()}-->\n$line"
            }
            else line
        }
    }

    into(Paths.get(outputDir))

    doLast {
        println("Docs Completed in: $outputDir")
    }
}

tasks.getByName("build").dependsOn("makeDocs")