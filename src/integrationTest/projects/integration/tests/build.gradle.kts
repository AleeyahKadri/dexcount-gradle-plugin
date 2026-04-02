import com.android.build.gradle.TestExtension

plugins {
    id("com.android.test")
    id("com.getkeepsafe.dexcount")
}

val compileSdkVersion: Int by rootProject.extra
val minSdkVersion: Int by rootProject.extra
val targetSdkVersion: Int by rootProject.extra

configure<TestExtension> {
    try {
        namespace = "com.getkeepsafe.dexcount.integration.tests"
    } catch (_: Throwable) {
        // Expected on AGP < 8.0.0
    }

    compileSdkVersion(compileSdkVersion)

    defaultConfig {
        minSdk = minSdkVersion
        targetSdk = targetSdkVersion

        // The package name of the test app
        testApplicationId = "com.getkeepsafe.dexcount.integration.tests"
        // The Instrumentation test runner used to run tests.
        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
    }

    // Set the target app project. The module specified here should contain the production code
    // test should run against.
    targetProjectPath = ":app"

    lintOptions {
        isAbortOnError = false
    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = true
        }
    }
}

dependencies {
    // Android Testing Support Library"s runner and rules and hamcrest matchers
    implementation("com.android.support.test:runner:1.0.2")
    implementation("com.android.support.test:rules:1.0.2")
    implementation("org.hamcrest:hamcrest-core:1.3")
}
