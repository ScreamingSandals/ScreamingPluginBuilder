import org.cadixdev.gradle.licenser.LicenseExtension
import org.cadixdev.gradle.licenser.Licenser
import java.util.Calendar

plugins {
    alias(libs.plugins.licenser) apply false
    kotlin("jvm") version libs.versions.kotlin apply false
}

subprojects {
    apply<JavaPlugin>()
    apply<JavaGradlePluginPlugin>()
    apply<MavenPublishPlugin>()
    apply<Licenser>()

    repositories {
        mavenCentral()
        gradlePluginPortal()
        // TODO: remove repository when (if) uploaded to gradle plugin portal
        maven("https://maven.neoforged.net/releases") {
            content {
                includeGroup("net.neoforged.licenser")
            }
        }
    }

    dependencies {
        "compileOnly"(rootProject.libs.jetbrains.annotations)
    }

    extensions.configure<PublishingExtension> {
        val nexusUrlSnapshot: String? = System.getenv("NEXUS_URL_SNAPSHOT")
        val nexusUrlRelease: String? = System.getenv("NEXUS_URL_RELEASE")
        val nexusUsername: String? = System.getenv("NEXUS_USERNAME")
        val nexusPassword: String? = System.getenv("NEXUS_PASSWORD")

        if (nexusUrlSnapshot != null && nexusUrlRelease != null && nexusUsername != null && nexusPassword != null) {
            repositories {
                maven {
                    url = if ((project.version as String).contains("SNAPSHOT")) {
                        uri(nexusUrlSnapshot)
                    } else {
                        uri(nexusUrlRelease)
                    }
                    credentials {
                        username = nexusUsername
                        password = nexusPassword
                    }
                }
            }
        }
    }

    extensions.configure<JavaPluginExtension> {
        sourceCompatibility = JavaVersion.VERSION_11
    }

    tasks.withType<JavaCompile> {
        options.compilerArgs.add("-Xlint:deprecation")
        options.release = 17
    }

    extensions.configure<LicenseExtension> {
        header(rootProject.file("license_header.txt"))
        properties {
            set("year", Calendar.getInstance().get(Calendar.YEAR))
        }
        exclude("org/screamingsandals/gradle/run/VersionInfo.java")
    }
}

