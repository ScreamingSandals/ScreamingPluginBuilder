dependencies {
    compileOnly(libs.kotlin.plugin)
    compileOnly(libs.kotlin.sam)
    compileOnly(libs.shadow)
}

gradlePlugin {
    plugins {
        create("plugin-slib") {
            id = "org.screamingsandals.plugin-slib"
            implementationClass = "org.screamingsandals.gradle.slib.SLibPlugin"
        }
    }
}