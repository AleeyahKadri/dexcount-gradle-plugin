buildscript {
    val agpVersion: String by project

    extra.apply {
        set("minSdkVersion", 19)
        set("targetSdkVersion", 28)
        set("compileSdkVersion", 28)
    }

    repositories {
        mavenLocal {
            content {
                includeGroup("com.getkeepsafe.dexcount")
            }
        }
        gradlePluginPortal {
            content {
                excludeGroup("com.getkeepsafe.dexcount")
            }
        }
        google()
    }

    dependencies {
        classpath(
            dependencies.create("com.android.tools.build:gradle:$agpVersion") {
                // Android build tools (as of 3.2.0-alpha18) bundle the deprecated
                // 'jre' stdlib modules, which cause warnings at build-time that
                // fail the build.
                exclude(module = "kotlin-stdlib-jdk7")
                exclude(module = "kotlin-stdlib-jdk8")
            }
        )
        classpath("com.getkeepsafe.dexcount:dexcount-gradle-plugin:+")
    }
}

allprojects {
    repositories {
        mavenLocal {
            content {
                includeGroup("com.getkeepsafe.dexcount")
            }
        }
        gradlePluginPortal {
            content {
                excludeGroup("com.getkeepsafe.dexcount")
            }
        }
        google()
    }
}

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}
