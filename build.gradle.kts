import dev.yumi.gradle.licenser.YumiLicenserGradleExtension
import dev.yumi.gradle.licenser.YumiLicenserGradlePlugin
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import java.util.Calendar

plugins {
    alias(libs.plugins.licenser) apply false
    kotlin("jvm") version libs.versions.kotlin apply false
}

subprojects {
    apply<JavaPlugin>()
    apply<JavaGradlePluginPlugin>()
    apply<MavenPublishPlugin>()
    apply<YumiLicenserGradlePlugin>()
    apply(plugin = "org.jetbrains.kotlin.jvm")

    repositories {
        mavenCentral()
        gradlePluginPortal()
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

    extensions.configure<KotlinJvmProjectExtension> {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    extensions.configure<YumiLicenserGradleExtension> {
        rule(rootProject.file("license_header.txt"))
        projectCreationYear = Calendar.getInstance().get(Calendar.YEAR)
    }
}

