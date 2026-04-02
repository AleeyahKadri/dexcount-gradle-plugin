// Sets the output JAR filename
rootProject.name = "dexcount-gradle-plugin"

buildCache {
    local {
        directory = File(rootDir, "build-cache")
        removeUnusedEntriesAfterDays = 30
    }
}

enableFeaturePreview("GROOVY_COMPILATION_AVOIDANCE")
