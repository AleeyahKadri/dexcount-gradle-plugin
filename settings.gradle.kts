// Sets the output JAR filename
rootProject.name = "dexcount-gradle-plugin"

buildCache {
    local {
        directory = file("build-cache")
        removeUnusedEntriesAfterDays = 30
    }
}

enableFeaturePreview("GROOVY_COMPILATION_AVOIDANCE")
