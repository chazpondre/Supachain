plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}

file("examples").listFiles()?.forEach { dir ->
    if (dir.isDirectory) include(":examples:${dir.name}")
}

include(":")
rootProject.name = "Supachain"

