/*
 * Copyright (C) 2015-2016 KeepSafe Software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.TestDescriptor
import org.gradle.jvm.tasks.Jar

plugins {
    id("groovy")
    id("java-gradle-plugin")

    alias(libs.plugins.gradlePluginPublish)
    alias(libs.plugins.mavenPublish)
    alias(libs.plugins.thrifty)
    alias(libs.plugins.versions)
}

val GROUP: String by project
val VERSION_NAME: String by project
val POM_DESCRIPTION: String by project
val POM_ARTIFACT_ID: String by project
val POM_NAME: String by project
val POM_URL: String by project
val POM_SCM_URL: String by project

group = GROUP
version = VERSION_NAME
description = POM_DESCRIPTION

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_11
}

sourceSets {
    create("integrationTest")
}

configurations {
    // Runtime dependencies provided by consuming Android projects - if you're
    // building an Android project, you've already got these on the build classpath
    // and we can't bundle our own copies.
    create("androidBuildTool")

    // Runtime dependencies that are run only in Gradle Workers
    // and must be restricted to an isolated classpath
    create("workerImplementation")

    getByName("testImplementation") {
        extendsFrom(configurations.getByName("androidBuildTool"))
        extendsFrom(configurations.getByName("workerImplementation"))
    }

    getByName("integrationTestImplementation") {
        extendsFrom(configurations.getByName("testImplementation"))
    }

    getByName("compileOnly") {
        extendsFrom(configurations.getByName("androidBuildTool"))
        extendsFrom(configurations.getByName("workerImplementation"))
    }
}

tasks.jar {
    manifest {
        attributes(
                "Implementation-Title" to POM_ARTIFACT_ID,
                "Implementation-Version" to VERSION_NAME)
    }
}

repositories {
    mavenLocal {
        content {
            includeGroup("com.getkeepsafe.dexcount")
        }
    }
    gradlePluginPortal()
    google()
}

dependencies {
    compileOnlyApi(libs.autoValue.annotations)
    annotationProcessor(libs.autoValue.processor)

    implementation(libs.commons.io)

    "androidBuildTool"(libs.androidTools.agp)
    "androidBuildTool"(libs.androidTools.r8)
    "androidBuildTool"(libs.androidTools.repository)

    "workerImplementation"(libs.gson)
    "workerImplementation"(libs.javassist)
    "workerImplementation"(libs.thriftyRuntime)

    testImplementation(libs.spock) {
        exclude(module = "groovy-all")
    }
    testImplementation(libs.thriftyRuntime)
}

thrifty {
    java {}
}

val generateDependencyResource = tasks.register("generateDependencyResource") {
    // This task generates a text file containing versions of our worker-specific
    // dependencies.  At runtime, we'll stuff these into a custom configuration
    // and hand that to Gradle Worker tasks.

    val generatedResourcesDir = project.layout.buildDirectory.dir(listOf("generated", "sources", "dexcount", "src", "main", "resources").joinToString(File.separator))
    val outputFile = generatedResourcesDir.map { it.file("dependencies.list") }
    val dependencies = configurations.getByName("workerImplementation").dependencies

    dependencies.forEach { dep ->
        inputs.property(dep.name, dep.version)
    }

    outputs.dir(generatedResourcesDir)

    doFirst {
        val file = outputFile.get().asFile
        file.parentFile.mkdirs()
        file.delete()

        dependencies
            .map { "${it.group}:${it.name}:${it.version}" }
            .forEach { file.appendText("$it\n") }
    }
}

sourceSets {
    main {
        resources {
            srcDir(generateDependencyResource)
        }
    }
}

val installForTesting = tasks.register("installForIntegrationTests") {
    dependsOn("publishToMavenLocal")
}

val integrationTest = tasks.register<Test>("integrationTest") {
    dependsOn(installForTesting)
    mustRunAfter(tasks.named("test"))

    outputs.cacheIf { false }
    outputs.upToDateWhen { false }

    group = "verification"
    description = "Runs integration tests."

    testClassesDirs = sourceSets["integrationTest"].output.classesDirs
    classpath = sourceSets["integrationTest"].runtimeClasspath

    // Workaround for https://github.com/gradle/gradle/issues/4506#issuecomment-570815277
    systemProperty("org.gradle.testkit.dir", file("build/tmp/.test-kit"))

    jvmArgs(
        "-XX:+HeapDumpOnOutOfMemoryError", "-XX:GCTimeLimit=20", "-XX:GCHeapFreeLimit=10",
        "-XX:MaxMetaspaceSize=2g"
    )

    beforeTest(closureOf<TestDescriptor> {
        logger.lifecycle("Running test: $this")
    })
}

tasks.named("check") {
    dependsOn(integrationTest)
}

tasks.register("publishEverywhere") {
    group = "publishing"
    description = "Publish to Maven Central and the Gradle Plugin Portal"

    dependsOn("publish", "publishPlugins")
}

// Compiler settings

tasks.withType<JavaCompile>().configureEach {
    sourceCompatibility = JavaVersion.VERSION_11.toString()
    targetCompatibility = JavaVersion.VERSION_11.toString()

    // Show all warnings except boot classpath
    options.apply {
        compilerArgs.add("-Xlint:all")            // Turn on all warnings
        compilerArgs.add("-Xlint:-options")       // Turn off "missing" bootclasspath warning
        compilerArgs.add("-Xlint:-processing")    // Turn off "no processor claimed these annotations" warning
        compilerArgs.add("-Werror")               // Turn warnings into errors
        encoding = "utf-8"
        isFork = true
    }
}

tasks.withType<GroovyCompile>().configureEach {
    sourceCompatibility = JavaVersion.VERSION_11.toString()
    targetCompatibility = JavaVersion.VERSION_11.toString()

    // Show all warnings except boot classpath
    options.apply {
        compilerArgs.add("-Xlint:all")            // Turn on all warnings
        compilerArgs.add("-Xlint:-options")       // Turn off "missing" bootclasspath warning
        compilerArgs.add("-Werror")               // Turn warnings into errors
        isIncremental = true
        encoding = "utf-8"
        isFork = true
    }

    groovyOptions.apply {
        encoding = "utf-8"
        isFork = true
    }
}

// Don't fork too much on CI - it tends to run out of metaspace memory.
val isCi = providers.environmentVariable("CI").isPresent

tasks.withType<Test>().configureEach {
    // Turn on logging for all tests, filter to show failures/skips only
    testLogging {
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        showCauses = true
        showExceptions = true
        showStackTraces = true
        events("failed", "skipped")
    }

    useJUnitPlatform()

    failFast = true
    maxParallelForks = if (isCi) 1 else (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1)
}

tasks.withType<Javadoc>().configureEach {
    title = "${project.name} ${project.version}"
    (options as StandardJavadocDocletOptions).apply {
        source = JavaVersion.VERSION_11.toString()
        header = project.name
        encoding("UTF-8")
        docEncoding("UTF-8")
        charSet("UTF-8")
        linkSource(true)
        author(true)
        links("https://docs.oracle.com/en/java/javase/11/docs/api/")
        addStringOption("Xdoclint:none", "-quiet")
    }
}

tasks.withType<Groovydoc>().configureEach {
    docTitle = "${project.name} ${project.version}"
    header = project.name
    link("http://docs.oracle.com/javase/8/docs/api/",
        "http://docs.oracle.com/javaee/7/api/",
        "http://groovy.codehaus.org/gapi/")
    exclude("**/*Spec.java")
}

signing {
    useGpgCmd()
}

// Plugin Portal publishing

gradlePlugin {
    website = POM_URL
    vcsUrl = POM_SCM_URL

    plugins {
        create("dexcount") {
            id = "com.getkeepsafe.dexcount"
            implementationClass = "com.getkeepsafe.dexcount.DexMethodCountPlugin"
            displayName = POM_NAME
            description = POM_DESCRIPTION
            tags.set(listOf("android", "dex", "method count"))
        }
    }
}

// Convenient entrypoint for the "upload snapshot" CI action.
// It's easier to do the version check here than in a Github action.
val isSnapshot = providers.gradleProperty("VERSION_NAME").map { it.endsWith("-SNAPSHOT") }
tasks.register("uploadSnapshot") {
    if (isSnapshot.get()) {
        dependsOn(tasks.named("publish"))
    } else {
        doFirst {
            logger.lifecycle("Skipping upload of non-snapshot version '{}'", VERSION_NAME)
        }
    }
}

tasks.wrapper {
    gradleVersion = "8.4"
    distributionType = Wrapper.DistributionType.ALL
}
