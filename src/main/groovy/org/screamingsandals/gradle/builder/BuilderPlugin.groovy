package org.screamingsandals.gradle.builder

import com.github.jengelman.gradle.plugins.shadow.ShadowExtension
import com.github.jengelman.gradle.plugins.shadow.ShadowPlugin
import io.franzbecker.gradle.lombok.LombokPlugin
import io.franzbecker.gradle.lombok.task.DelombokTask
import kr.entree.spigradle.data.Dependency
import kr.entree.spigradle.data.Repositories
import kr.entree.spigradle.data.SpigotRepositories
import kr.entree.spigradle.data.VersionModifier
import kr.entree.spigradle.module.common.SpigradlePlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.SelfResolvingDependency
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.credentials.HttpHeaderCredentials
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.api.tasks.bundling.Jar
import org.gradle.authentication.http.HttpHeaderAuthentication

class BuilderPlugin implements Plugin<Project> {

    public static Dependency WATERFALL = new Dependency(
            "io.github.waterfallmc",
            "waterfall-api",
            "1.16-R0.4-SNAPSHOT",
            false,
            VersionModifier.INSTANCE.createAdjuster("R0.4", "SNAPSHOT")
    )

    public static Dependency VELOCITY = new Dependency(
            "com.velocitypowered",
            "velocity-api",
            "1.1.2-SNAPSHOT",
            false,
            VersionModifier.INSTANCE.getSNAPSHOT_APPENDER()
    )

    public static Dependency PAPERLIB = new Dependency(
            "io.papermc",
            "paperlib",
            "1.0.6-SNAPSHOT",
            false,
            VersionModifier.INSTANCE.getSNAPSHOT_APPENDER()
    )

    public static Dependency PLACEHOLDERAPI = new Dependency(
            "me.clip",
            "placeholderapi",
            "2.10.9",
            false,
            VersionModifier.INSTANCE.createAdjuster("")
    )

    private Project project

    @Override
    void apply(Project project) {
        this.project = project

        project.apply {
            plugin SpigradlePlugin.class
            plugin MavenPublishPlugin.class
            plugin LombokPlugin.class
            plugin JavaLibraryPlugin.class
        }

        project.repositories {
            jcenter()
            mavenCentral()
            //mavenLocal()

            maven { url Repositories.SONATYPE }
            maven { url SpigotRepositories.PAPER_MC }
            maven { url SpigotRepositories.SPIGOT_MC }
            maven { url 'https://repo.screamingsandals.org/public/' }
            maven { url 'https://repo.velocitypowered.com/snapshots/' }
            maven { url 'https://repo.extendedclip.com/content/repositories/placeholderapi/' }
        }

        project.dependencies {
            compileOnly 'org.jetbrains:annotations:20.1.0'
        }

        project.dependencies.ext['waterfall'] = { String version = null ->
            WATERFALL.format(version)
        }

        project.dependencies.ext['velocity'] = { String version = null ->
            VELOCITY.format(version)
        }

        project.dependencies.ext['paperlib'] = { String version = null ->
            PAPERLIB.format(version)
        }

        project.dependencies.ext['placeholder_api'] = { String version = null ->
            PLACEHOLDERAPI.format(version)
        }

        project.dependencies.ext['screaming'] = { String lib, String version ->
            return "org.screamingsandals.lib:$lib:$version"
        }

        if (System.getenv("GITLAB_REPO") != null) {
            def srcmain = project.file("src/main");
            def processDelombok = srcmain.exists() && srcmain.listFiles().length > 0
            if (processDelombok) {
                project.task('delombokForJavadoc', type: DelombokTask, dependsOn: 'compileJava') {
                    ext.outputDir = project.file("$project.buildDir/delombok")
                    outputs.dir(outputDir)
                    project.sourceSets.main.java.srcDirs.each {
                        inputs.dir(it)
                        args(it, "-d", outputDir)
                    }
                    doFirst {
                        outputDir.deleteDir()
                    }
                }
            }

            project.task('sourceJar', type: Jar) {
                it.classifier 'sources'
                from project.sourceSets.main.allJava
            }

            project.javadoc {
                if (processDelombok) {
                    dependsOn 'delombokForJavadoc'
                    source = project.tasks.getByName('delombokForJavadoc').outputDir
                }
                options.addBooleanOption('html5', true)
            }
            project.task('javadocJar', type: Jar, dependsOn: project.javadoc) {
                it.classifier = 'javadoc'
                from project.javadoc
            }
        }

        PublishingExtension publishing = project.extensions.getByName("publishing")
        def maven = publishing.publications.create("maven", MavenPublication) {
            it.artifact(project.tasks.jar)

            it.artifacts.every {
                it.classifier = ""
            }


            if (System.getenv("GITLAB_REPO") != null) {
                it.artifact(project.tasks.sourceJar)
                it.artifact(project.tasks.javadocJar)
            }

            it.pom.withXml {
                def dependenciesNode = asNode().appendNode("dependencies")
                project.configurations.compileOnly.dependencies.each {
                    if (!(it instanceof SelfResolvingDependency) && it.name != "spigradle") {
                        def dependencyNode = dependenciesNode.appendNode('dependency')
                        dependencyNode.appendNode('groupId', it.group)
                        dependencyNode.appendNode('artifactId', it.name)
                        dependencyNode.appendNode('version', it.version)
                        dependencyNode.appendNode('scope', 'provided')
                    }
                }
                project.configurations.api.dependencies.each {
                    def dependencyNode = dependenciesNode.appendNode('dependency')
                    dependencyNode.appendNode('groupId', it.group)
                    dependencyNode.appendNode('artifactId', it.name)
                    dependencyNode.appendNode('version', it.version)
                    dependencyNode.appendNode('scope', 'compile')
                }
            }
        }

        project.ext['enableShadowPlugin'] = {
            project.apply {
                plugin ShadowPlugin.class
            }

            project.tasks.getByName("screamCompile").dependsOn -= "build"
            project.tasks.getByName("screamCompile").dependsOn += "shadowJar"

            maven.each {
                it.getArtifacts().removeIf {
                    it.getBuildDependencies().getDependencies().contains(project.tasks.jar)
                }
                it.artifact(project.tasks.shadowJar) {
                    it.classifier = ""
                }
            }
        }

        if (System.getenv("GITLAB_REPO") != null) {
            publishing.repositories {
                it.maven { MavenArtifactRepository repository ->
                    repository.url System.getenv("GITLAB_REPO")
                    repository.name "GitLab"
                    repository.credentials(HttpHeaderCredentials) {
                        name = 'Private-Token'
                        value = System.getenv("GITLAB_TOKEN")
                    }
                    repository.authentication {
                        header(HttpHeaderAuthentication)
                    }
                }
            }
        }

        List tasks = ["build", "publishToMavenLocal"]

        if (System.getenv("GITLAB_REPO") != null) {
            tasks.add("publish")
            tasks.add("javadoc")
        }

        project.tasks.create("screamCompile").dependsOn = tasks
    }

}