import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm")
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

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