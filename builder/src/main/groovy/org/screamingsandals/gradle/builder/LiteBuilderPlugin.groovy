package org.screamingsandals.gradle.builder

import com.github.jengelman.gradle.plugins.shadow.ShadowPlugin
import io.franzbecker.gradle.lombok.LombokPlugin
import io.franzbecker.gradle.lombok.LombokPluginExtension
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
        def ciCdOptimized = System.getenv("OPTIMIZE_FOR_CI_CD") == "1"

        project.apply {
            plugin MavenPublishPlugin.class
            plugin JavaLibraryPlugin.class
        }

        Repositories.registerRepositoriesMethods(project)
        Dependencies.registerDependenciesMethods(project)

        project.repositories {
            mavenCentral()
        }

        project.dependencies.ext['screaming'] = { String lib, String version ->
            return "org.screamingsandals.lib:$lib:$version"
        }

        PublishingExtension publishing = project.extensions.getByName("publishing")
        if (System.getProperty("LITE_SKIP_PUBLICATION_CREATION") != "yes") {
            def maven = publishing.publications.create("maven", MavenPublication) {
                if (!project.ext.has('onlyPomArtifact') || !project.ext['onlyPomArtifact']) {
                    it.artifact(project.tasks.jar)
                }

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
        }

        project.ext['configureLombok'] = { iit ->
            project.apply {
                plugin LombokPlugin.class
            }

            project.extensions.getByName(LombokPluginExtension.NAME).each { LombokPluginExtension it ->
                it.version = "1.18.22"
                it.sha256 = "ecef1581411d7a82cc04281667ee0bac5d7c0a5aae74cfc38430396c91c31831"
            }
        }

        if (System.getenv("GITLAB_REPO") != null) {
            new GitlabRepository().setup(project, publishing)
        }

        if (System.getenv("NEXUS_URL_SNAPSHOT") != null && System.getenv("NEXUS_URL_RELEASE") != null) {
            new NexusRepository().setup(project, publishing)
        }

        List tasks = ["build"]

        if (!ciCdOptimized) {
            tasks.add("publishToMavenLocal")
        }

        if (System.getenv("GITLAB_REPO") != null || (System.getenv("NEXUS_URL_SNAPSHOT") != null && System.getenv("NEXUS_URL_RELEASE") != null)) {
            tasks.add("publish")
        }

        project.tasks.create("screamCompile").dependsOn = tasks
    }

}