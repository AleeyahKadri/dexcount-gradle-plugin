import com.getkeepsafe.dexcount.DexCountExtension

plugins {
    java
    id("com.getkeepsafe.dexcount")
}

configure<DexCountExtension> {
    printDeclarations.set(true)
}

dependencies {
    testImplementation("junit:junit:4.12")
}
