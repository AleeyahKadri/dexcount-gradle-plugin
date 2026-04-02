import com.getkeepsafe.dexcount.DexCountExtension
import com.android.build.gradle.AppExtension

plugins {
    id("com.android.application")
    id("com.getkeepsafe.dexcount")
}

val compileSdkVersion: Int by rootProject.extra
val minSdkVersion: Int by rootProject.extra
val targetSdkVersion: Int by rootProject.extra

configure<AppExtension> {
    compileSdkVersion(compileSdkVersion)

    try {
        namespace = "com.getkeepsafe.dexcount.integration"
    } catch (_: Throwable) {
        // Expected on AGP < 8.0.0
    }

    defaultConfig {
        applicationId = "com.getkeepsafe.dexcount.integration"
        minSdk = minSdkVersion
        targetSdk = targetSdkVersion
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
            outputFileName = outputFileName.replace(".apk", "-it.apk")
        }
    }
}

configure<DexCountExtension> {
    verbose.set(true)
    printVersion.set(true)
    teamCitySlug.set(project.name)
}

dependencies {
    implementation("com.android.support:appcompat-v7:28.0.0")

    testImplementation("junit:junit:4.12")
}
