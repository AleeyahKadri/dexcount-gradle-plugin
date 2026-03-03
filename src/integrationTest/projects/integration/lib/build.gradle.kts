plugins {
    id("com.android.library")
    id("com.getkeepsafe.dexcount")
}

android {
    compileSdk = rootProject.extra["compileSdkVersion"] as Int

    try {
        namespace = "com.getkeepsafe.dexcount.integration"
    } catch (ignored: Throwable) {
        // Expected on AGP < 8.0.0
    }

    defaultConfig {
        minSdk = rootProject.extra["minSdkVersion"] as Int
        targetSdk = rootProject.extra["targetSdkVersion"] as Int
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
        }

        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }
}

configure<com.getkeepsafe.dexcount.DexMethodCountExtension> {
    verbose = true
    teamCitySlug = project.name
    printDeclarations = true
}

dependencies {
    implementation("com.android.support:appcompat-v7:28.0.0")

    testImplementation("junit:junit:4.12")
}
