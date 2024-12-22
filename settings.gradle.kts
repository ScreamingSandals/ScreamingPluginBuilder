pluginManagement {
    repositories {
        gradlePluginPortal()
        maven {
            // TODO: remove repository when (if) uploaded to gradle plugin portal
            url = uri("https://maven.neoforged.net/releases")
            content {
                includeGroup("net.neoforged.licenser")
            }
        }
    }
}

rootProject.name = "screaming-gradle"

include(":builder")
include(":slib")
include(":run")