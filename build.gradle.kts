import org.cadixdev.gradle.licenser.LicenseExtension
import org.cadixdev.gradle.licenser.Licenser
import java.util.Calendar

plugins {
    alias(libs.plugins.licenser) apply false
}

subprojects {
    apply<JavaPlugin>()
    apply<JavaGradlePluginPlugin>()
    apply<MavenPublishPlugin>()
    apply<Licenser>()

    repositories {
        mavenCentral()
        maven {
            url = uri("https://plugins.gradle.org/m2/")
        }
    }

    dependencies {
        "compileOnly"(rootProject.libs.jetbrains.annotations)

        if (project.name != "builder") { // Lombok is not required in builder. TODO: delombok slib and run
            "compileOnly"(rootProject.libs.projectlombok)
            "annotationProcessor"(rootProject.libs.projectlombok)
        }
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
    }

    extensions.configure<LicenseExtension> {
        header(rootProject.file("license_header.txt"))
        properties {
            set("year", Calendar.getInstance().get(Calendar.YEAR))
        }
    }
}

