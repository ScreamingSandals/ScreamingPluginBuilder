dependencies {
    implementation(libs.gson)
}

gradlePlugin {
    plugins {
        create("plugin-run") {
            id = "org.screamingsandals.plugin-run"
            implementationClass = "org.screamingsandals.gradle.run.RunPlugin"
        }
    }
}