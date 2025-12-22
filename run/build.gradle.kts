plugins {
    alias(libs.plugins.buildconfig)
}

dependencies {
    compileOnly(libs.kotlin.plugin)
    implementation(libs.gson)
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