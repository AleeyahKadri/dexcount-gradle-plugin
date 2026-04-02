import com.getkeepsafe.dexcount.DexCountExtension
import com.android.build.gradle.LibraryExtension

plugins {
    id("com.android.library")
    id("com.getkeepsafe.dexcount")
}

val compileSdkVersion: Int by rootProject.extra
val minSdkVersion: Int by rootProject.extra
val targetSdkVersion: Int by rootProject.extra

configure<LibraryExtension> {
    compileSdkVersion(compileSdkVersion)

    try {
        namespace = "com.getkeepsafe.dexcount.integration"
    } catch (_: Throwable) {
        // Expected on AGP < 8.0.0
    }

    defaultConfig {
        minSdk = minSdkVersion
        targetSdk = targetSdkVersion
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

configure<DexCountExtension> {
    verbose.set(true)
    teamCitySlug.set(project.name)
    printDeclarations.set(true)
}

dependencies {
    implementation("com.android.support:appcompat-v7:28.0.0")

    testImplementation("junit:junit:4.12")
}
