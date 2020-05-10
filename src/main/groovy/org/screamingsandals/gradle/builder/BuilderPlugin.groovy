package org.screamingsandals.gradle.builder

import com.github.jengelman.gradle.plugins.shadow.ShadowExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.SelfResolvingDependency
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.api.tasks.javadoc.Javadoc
import org.screamingsandals.gradle.builder.attributes.BungeePluginAttributes
import org.screamingsandals.gradle.builder.task.BungeeYamlCreateTask

import com.github.jengelman.gradle.plugins.shadow.ShadowPlugin

import kr.entree.spigradle.SpigradlePlugin

import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.compile.AbstractCompile

class BuilderPlugin implements Plugin<Project> {

    private Project project;

    @Override
    public void apply(Project project) {
        this.project = project;

        project.apply {
            plugin ShadowPlugin.class
            plugin SpigradlePlugin.class
            plugin MavenPublishPlugin.class
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
            compileOnly lombok()
            annotationProcessor lombok()
            compileOnly 'org.jetbrains:annotations:19.0.0'
        }

        setupBungeeYamlGeneration()

        new SpigradleAdditionalSetup(project);

        project.tasks.getByName("spigotPluginYaml").enabled = false
        //project.tasks.getByName("shadowJar").minimize()

        project.task('sourceJar', type: Jar) {
            it.classifier 'sources'
            from project.sourceSets.main.allJava
        }

        if (project.hasProperty("screamingDocs")) {
            project.javadoc {
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
                    if (! (it instanceof SelfResolvingDependency) && it.name != "spigradle") {
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

    def setupBungeeYamlGeneration() {
        def attrType = BungeePluginAttributes
        def attrs = project.extensions.create('bungee', attrType)
        def task = project.task('bungeePluginYaml', type: BungeeYamlCreateTask) {
            group = 'ScreamingPluginBuilder'
            description = 'Auto generate a bungee.yml file.'
            attributes = attrs
            enabled = false /* Disable by default */
        }
        project.tasks.withType(Jar) {
            it.dependsOn task
        }
        def compileTasks = project.tasks.withType(AbstractCompile)
        if (!compileTasks.isEmpty()) {
            compileTasks.first()?.with {
                task.dependsOn it
            }
        }

    }

}