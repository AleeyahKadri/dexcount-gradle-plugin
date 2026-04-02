rootProject.name = "integration"
include(":app")
include(":java-app")
include(":java-library")
include(":lib")
include(":tests")

buildCache {
    local {
        directory = File(rootDir, "build-cache")
        removeUnusedEntriesAfterDays = 30
    }
}
