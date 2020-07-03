package org.screamingsandals.gradle.builder

import com.github.jengelman.gradle.plugins.shadow.ShadowExtension
import io.franzbecker.gradle.lombok.LombokPlugin
import kr.entree.spigradle.data.BungeeDependencies
import kr.entree.spigradle.data.Dependency
import kr.entree.spigradle.module.bungee.BungeePlugin
import kr.entree.spigradle.module.spigot.SpigotPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.SelfResolvingDependency
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import io.franzbecker.gradle.lombok.task.DelombokTask

import com.github.jengelman.gradle.plugins.shadow.ShadowPlugin

import org.gradle.api.tasks.bundling.Jar

class BuilderPlugin implements Plugin<Project> {

    public static Dependency WATERFALL = new Dependency(
            "io.github.waterfallmc",
            "waterfall-api",
            BungeeDependencies.BUNGEE_CORD.getVersion(),
            BungeeDependencies.BUNGEE_CORD.getVersionModifier()
    )

    private Project project;

    @Override
    public void apply(Project project) {
        this.project = project;

        project.apply {
            plugin ShadowPlugin.class
            plugin SpigotPlugin.class
            plugin BungeePlugin.class
            plugin MavenPublishPlugin.class
            plugin LombokPlugin.class
        }

        project.repositories {
            jcenter()
            mavenCentral()
            mavenLocal()

            maven {
                url = 'https://repo.screamingsandals.org'
            }
        }

        project.dependencies {
            compileOnly 'org.jetbrains:annotations:19.0.0'
        }

        project.dependencies.ext['waterfall'] = { String version = null ->
            WATERFALL.format(version)
        }

        project.tasks.getByName('detectSpigotMain').enabled = false
        project.tasks.getByName('generateSpigotDescription').enabled = false
        project.tasks.getByName('detectBungeeMain').enabled = false
        project.tasks.getByName('generateBungeeDescription').enabled = false

        project.task('sourceJar', type: Jar) {
            it.classifier 'sources'
            from project.sourceSets.main.allJava
        }

        if (project.hasProperty("screamingDocs")) {
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

            project.javadoc {
                if (processDelombok) {
                    dependsOn 'delombokForJavadoc'
                    source = project.tasks.getByName('delombokForJavadoc').outputDir
                }
                def mainScreamingDir = project.hasProperty('customMainScreamingDir') ? project.property('customMainScreamingDir') : project.rootProject.name.toLowerCase().endsWith('-parent') ? project.rootProject.name.toLowerCase().substring(0, project.rootProject.name.toLowerCase().length() - 7) : project.rootProject.name.toLowerCase()
                destinationDir = project.file(project.property('screamingDocs') + '/' + mainScreamingDir + '/' + project.name.toLowerCase())
                options {
                    links 'https://docs.oracle.com/en/java/javase/11/docs/api/'
                }
                options.addBooleanOption('html5', true)
            }
        }

        PublishingExtension publishing = project.extensions.getByName("publishing")
        publishing.publications.create("maven", MavenPublication) {
            ShadowExtension shadow = project.extensions.getByName("shadow")
            shadow.component(it)

            it.artifact(project.tasks.sourceJar)

            it.artifacts.every {
                it.classifier = ""
            }

            it.pom.withXml {
                if (asNode().get("dependencies") != null) {
                    asNode().remove(asNode().get("dependencies"))
                }

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
            }
        }

        if (project.hasProperty("screamingRepository")) {
            publishing.repositories {
                it.maven({ MavenArtifactRepository repository ->
                    repository.url = project.property("screamingRepository")
                })
            }
        }

        List tasks = ["shadowJar", "publishToMavenLocal"]

        if (project.hasProperty("screamingRepository")) {
            tasks.add("publish")
        }

        if (project.hasProperty("screamingDocs")) {
            tasks.add("javadoc")
        }

        project.tasks.create("screamCompile").dependsOn = tasks
    }

}