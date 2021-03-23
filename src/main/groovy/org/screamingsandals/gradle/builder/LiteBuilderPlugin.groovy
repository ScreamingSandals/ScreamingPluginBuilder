package org.screamingsandals.gradle.builder

import com.github.jengelman.gradle.plugins.shadow.ShadowPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.SelfResolvingDependency
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.screamingsandals.gradle.builder.dependencies.Dependencies
import org.screamingsandals.gradle.builder.maven.GitlabRepository
import org.screamingsandals.gradle.builder.maven.NexusRepository
import org.screamingsandals.gradle.builder.repositories.Repositories

class LiteBuilderPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.apply {
            plugin MavenPublishPlugin.class
            plugin JavaLibraryPlugin.class
        }

        Repositories.registerRepositoriesMethods(project)
        Dependencies.registerDependenciesMethods(project)

        project.repositories {
            jcenter()
            mavenCentral()
        }

        project.dependencies.ext['screaming'] = { String lib, String version ->
            return "org.screamingsandals.lib:$lib:$version"
        }

        PublishingExtension publishing = project.extensions.getByName("publishing")
        def maven = publishing.publications.create("maven", MavenPublication) {
            it.artifact(project.tasks.jar)

            it.artifacts.every {
                it.classifier = ""
            }

            it.pom.withXml {
                def dependenciesNode = asNode().appendNode("dependencies")
                project.configurations.compileOnly.dependencies.each {
                    if (!(it instanceof SelfResolvingDependency)) {
                        def dependencyNode = dependenciesNode.appendNode('dependency')
                        dependencyNode.appendNode('groupId', it.group)
                        dependencyNode.appendNode('artifactId', it.name)
                        dependencyNode.appendNode('version', it.version)
                        dependencyNode.appendNode('scope', 'provided')
                    }
                }
                if (!project.tasks.findByName("shadowJar")) {
                    project.configurations.api.dependencies.each {
                        def dependencyNode = dependenciesNode.appendNode('dependency')
                        dependencyNode.appendNode('groupId', it.group)
                        dependencyNode.appendNode('artifactId', it.name)
                        dependencyNode.appendNode('version', it.version)
                        dependencyNode.appendNode('scope', 'compile')
                    }
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
                    it.buildDependencies.getDependencies().contains(project.tasks.jar)
                }
                it.artifact(project.tasks.shadowJar) {
                    it.classifier = ""
                }
            }
        }

        if (System.getenv("GITLAB_REPO") != null) {
            new GitlabRepository().setup(project, publishing)
        }

        if (System.getenv("NEXUS_URL_SNAPSHOT") != null && System.getenv("NEXUS_URL_RELEASE") != null) {
            new NexusRepository().setup(project, publishing)
        }

        List tasks = ["build", "publishToMavenLocal"]

        if (System.getenv("GITLAB_REPO") != null || (System.getenv("NEXUS_URL_SNAPSHOT") != null && System.getenv("NEXUS_URL_RELEASE") != null)) {
            tasks.add("publish")
        }

        project.tasks.create("screamCompile").dependsOn = tasks
    }

}