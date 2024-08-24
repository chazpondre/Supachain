plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}

// Subprojects
include(":examples:1Simple")
include(":examples:2StructuredOutputDaysOfTheWeek")
include(":docBuilder")

include(":")

rootProject.name = "Supachain"

