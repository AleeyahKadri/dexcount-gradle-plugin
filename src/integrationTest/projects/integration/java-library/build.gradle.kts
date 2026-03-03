plugins {
    id("java-library")
    id("com.getkeepsafe.dexcount")
}

configure<com.getkeepsafe.dexcount.DexMethodCountExtension> {
    printDeclarations = true
}

dependencies {
    testImplementation("junit:junit:4.12")
}
