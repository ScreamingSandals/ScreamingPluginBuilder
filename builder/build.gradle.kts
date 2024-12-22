import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version libs.versions.kotlin
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

dependencies {
    implementation(libs.licenser)
    implementation(libs.shadow)
}

gradlePlugin {
    plugins {
        create("plugin-builder") {
            id = "org.screamingsandals.plugin-builder"
            implementationClass = "org.screamingsandals.gradle.builder.BuilderPlugin"
        }
    }
}
