/*
 * Copyright 2024 ScreamingSandals
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.screamingsandals.gradle.builder

import com.github.jengelman.gradle.plugins.shadow.ShadowPlugin
import org.cadixdev.gradle.licenser.Licenser
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.SelfResolvingDependency
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.screamingsandals.gradle.builder.maven.NexusRepository
import org.screamingsandals.gradle.builder.repositories.Repositories
import io.freefair.gradle.plugins.lombok.LombokPlugin

class LiteBuilderPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        def ciCdOptimized = System.getenv("OPTIMIZE_FOR_CI_CD") == "1"

        project.apply {
            plugin MavenPublishPlugin.class
            plugin JavaLibraryPlugin.class
        }

        Repositories.registerRepositoriesMethods(project)

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
        }

        project.ext['configureLicenser'] = { iit ->
            var headerFile = project.getRootProject().file("license_header.txt")

            if (headerFile.exists()) {
                project.apply {
                    plugin Licenser.class
                }

                project.license {
                    header = headerFile
                    ignoreFailures = true
                    properties {
                        year = Calendar.getInstance().get(Calendar.YEAR)
                    }
                }
            }
        }

        if (System.getenv("NEXUS_URL_SNAPSHOT") != null && System.getenv("NEXUS_URL_RELEASE") != null) {
            new NexusRepository().setup(project, publishing)
        }

        List tasks = ["build"]

        if (!ciCdOptimized) {
            tasks.add("publishToMavenLocal")
        }

        if (System.getenv("NEXUS_URL_SNAPSHOT") != null && System.getenv("NEXUS_URL_RELEASE") != null) {
            tasks.add("publish")
        }

        project.tasks.create("screamCompile").dependsOn = tasks
    }

}