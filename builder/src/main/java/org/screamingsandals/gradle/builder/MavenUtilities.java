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

package org.screamingsandals.gradle.builder;

import org.gradle.api.Project;
import org.gradle.api.artifacts.SelfResolvingDependency;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.gradle.builder.maven.NexusRepository;

public final class MavenUtilities {
    private MavenUtilities() {
    }

    public static @NotNull MavenPublication setupPublishing(@NotNull Project project) {
        return setupPublishing(project, false, false, false);
    }

    public static @NotNull MavenPublication setupPublishing(@NotNull Project project, boolean onlyPomArtifact, boolean addSourceJar, boolean addJavadocJar) {
        var publishing = (PublishingExtension) project.getExtensions().getByName("publishing");
        return publishing.getPublications().create("maven", MavenPublication.class, it -> {
            var shadowJar = project.getTasks().findByName("shadowJar");
            if (!onlyPomArtifact) {
                it.artifact(shadowJar != null ? shadowJar: project.getTasks().getByName("jar"));
            }

            it.getArtifacts().forEach(a -> a.setClassifier(""));

            if (addSourceJar) {
                it.artifact(project.getTasks().getByName(Constants.SOURCES_JAR_TASK_NAME));
            }

            if (addJavadocJar) {
                it.artifact(project.getTasks().getByName(Constants.JAVADOC_JAR_TASK_NAME));
            }

            it.getPom().withXml(xml -> {
                var dependenciesNode = xml.asNode().appendNode("dependencies");
                project.getConfigurations().getByName("compileOnly").getDependencies().forEach(dep -> {
                    if (!(dep instanceof SelfResolvingDependency)) {
                        var dependencyNode = dependenciesNode.appendNode("dependency");
                        dependencyNode.appendNode("groupId", dep.getGroup());
                        dependencyNode.appendNode("artifactId", dep.getName());
                        dependencyNode.appendNode("version", dep.getVersion());
                        dependencyNode.appendNode("scope", "provided");
                    }
                });
                if (shadowJar == null) {
                    project.getConfigurations().getByName("api").getDependencies().forEach(dep -> {
                        var dependencyNode = dependenciesNode.appendNode("dependency");
                        dependencyNode.appendNode("groupId", dep.getGroup());
                        dependencyNode.appendNode("artifactId", dep.getName());
                        dependencyNode.appendNode("version", dep.getVersion());
                        dependencyNode.appendNode("scope", "compile");
                    });
                }
            });
        });
    }

    public static void setupMavenRepositoriesFromProperties(@NotNull Project project) {
        var publishing = (PublishingExtension) project.getExtensions().getByName("publishing");
        if (System.getenv(Constants.NEXUS_URL_RELEASE_PROPERTY) != null
                && System.getenv(Constants.NEXUS_URL_SNAPSHOT_PROPERTY) != null
                && System.getenv(Constants.NEXUS_USERNAME_PROPERTY) != null
                && System.getenv(Constants.NEXUS_PASSWORD_PROPERTY) != null
        ) {
            new NexusRepository().setup(project, publishing);
        }
    }
}
