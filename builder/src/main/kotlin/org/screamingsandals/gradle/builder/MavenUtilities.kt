/*
 * Copyright 2025 ScreamingSandals
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

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.Project
import org.gradle.api.artifacts.FileCollectionDependency
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.screamingsandals.gradle.builder.maven.NexusRepository

object MavenUtilities {
    fun setupPublishing(
        project: Project,
        onlyPomArtifact: Boolean = false,
        addSourceJar: Boolean = false,
        addJavadocJar: Boolean = false
    ): MavenPublication {
        val publishing = project.extensions.getByName("publishing") as PublishingExtension
        return publishing.publications
            .create("maven", MavenPublication::class.java) {
                val shadowJar = project.tasks.findByName(ShadowJar.SHADOW_JAR_TASK_NAME)
                if (!onlyPomArtifact) {
                    it.artifact(shadowJar ?: project.tasks.getByName("jar"))
                }

                it.artifacts.forEach { a -> a.classifier = "" }

                if (addSourceJar) {
                    it.artifact(project.tasks.getByName(SOURCES_JAR_TASK_NAME))
                }

                if (addJavadocJar) {
                    it.artifact(project.tasks.getByName(JAVADOC_JAR_TASK_NAME))
                }

                val pom = it.pom
                if ("true" == System.getenv("GITHUB_ACTIONS")) {
                    val github = System.getenv("GITHUB_SERVER_URL")
                    val githubNoProtocol: String = github.split("://".toRegex(), limit = 2).toTypedArray()[1]
                    val repository = System.getenv("GITHUB_REPOSITORY")
                    val runId = System.getenv("GITHUB_RUN_ID")

                    pom.scm { scm ->
                        scm.connection.set("scm:git:$githubNoProtocol/$repository.git")
                        scm.developerConnection.set("scm:git:ssh://$githubNoProtocol/$repository.git")
                        scm.url.set("$github/$repository")
                        if (System.getenv("GITHUB_SHA") != null) {
                            scm.tag.set(System.getenv("GITHUB_SHA"))
                        }
                    }

                    pom.properties.put("github.actions.url", "$github/$repository/actions/runs/$runId")
                    if (System.getenv("GIT_COMMIT_MESSAGE") != null) {
                        pom.properties.put("git.commit.message", System.getenv("GIT_COMMIT_MESSAGE"))
                    }
                }
                pom.withXml { xml ->
                    val dependenciesNode = xml.asNode().appendNode("dependencies")
                    project.configurations.getByName("compileOnly").dependencies
                        .forEach { dep ->
                            // Skip file collection dependencies
                            if (dep is FileCollectionDependency && dep.name == "unspecified") {
                                return@forEach
                            }

                            val dependencyNode = dependenciesNode.appendNode("dependency")
                            dependencyNode.appendNode("groupId", dep.group)
                            dependencyNode.appendNode("artifactId", dep.name)
                            dependencyNode.appendNode("version", dep.version)
                            dependencyNode.appendNode("scope", "provided")
                        }
                    if (shadowJar == null) {
                        project.configurations.getByName("api").dependencies
                            .forEach { dep ->
                                val dependencyNode = dependenciesNode.appendNode("dependency")
                                dependencyNode.appendNode("groupId", dep.group)
                                dependencyNode.appendNode("artifactId", dep.name)
                                dependencyNode.appendNode("version", dep.version)
                                dependencyNode.appendNode("scope", "compile")
                            }
                    }
                }
            }
    }

    fun setupMavenRepositoriesFromProperties(project: Project) {
        val publishing = project.extensions.getByName("publishing") as PublishingExtension
        if (
            System.getenv(NEXUS_URL_RELEASE_PROPERTY) != null
            && System.getenv(NEXUS_URL_SNAPSHOT_PROPERTY) != null
            && System.getenv(NEXUS_USERNAME_PROPERTY) != null
            && System.getenv(NEXUS_PASSWORD_PROPERTY) != null
        ) {
            NexusRepository().setup(project, publishing)
        }
    }
}