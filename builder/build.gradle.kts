dependencies {
    implementation(libs.jsch)
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
