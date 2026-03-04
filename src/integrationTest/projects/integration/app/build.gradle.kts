plugins {
    id("com.android.application")
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
        applicationId = "com.getkeepsafe.dexcount.integration"
        minSdk = rootProject.extra["minSdkVersion"] as Int
        targetSdk = rootProject.extra["targetSdkVersion"] as Int
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = true
        }

        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }

    applicationVariants.all {
        outputs.all {
            (this as com.android.build.gradle.internal.api.BaseVariantOutputImpl).outputFileName =
                outputFileName.replace(".apk", "-it.apk")
        }
    }
}

configure<com.getkeepsafe.dexcount.DexMethodCountExtension> {
    verbose = true
    printVersion = true
    teamCitySlug = project.name
}

dependencies {
    implementation("com.android.support:appcompat-v7:28.0.0")

    testImplementation("junit:junit:4.12")
}
