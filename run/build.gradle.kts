import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.buildconfig)
    kotlin("jvm")
}

dependencies {
    compileOnly(libs.kotlin.plugin)
    implementation(libs.gson)
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

buildConfig {
    useKotlinOutput()
    className("VersionInfo")
    packageName("org.screamingsandals.gradle.run")

    buildConfigField("String", "VERSION", "\"${project.version}\"")
}

gradlePlugin {
    plugins {
        create("plugin-run") {
            id = "org.screamingsandals.plugin-run"
            implementationClass = "org.screamingsandals.gradle.run.RunPlugin"
        }
    }
}