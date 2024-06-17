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

package org.screamingsandals.gradle.builder;

import com.github.jengelman.gradle.plugins.shadow.ShadowPlugin;
import org.cadixdev.gradle.licenser.LicenseExtension;
import org.cadixdev.gradle.licenser.Licenser;
import org.gradle.api.Project;
import org.gradle.api.artifacts.SelfResolvingDependency;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.external.javadoc.CoreJavadocOptions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.screamingsandals.gradle.builder.maven.NexusRepository;
import org.screamingsandals.gradle.builder.tasks.JavadocUploadTask;

import java.util.Calendar;
import java.util.function.Predicate;

public final class Utilities {
    private Utilities() {
    }

    public static @NotNull MavenConfiguration setupPublishing(@NotNull Project project) {
        return setupPublishing(project, false, false, false);
    }

    public static @NotNull MavenConfiguration setupPublishing(@NotNull Project project, boolean onlyPomArtifact, boolean addSourceJar, boolean addJavadocJar) {
        var publishing = (PublishingExtension) project.getExtensions().getByName("publishing");
        var publication = publishing.getPublications().create("maven", MavenPublication.class, it -> {
            if (!onlyPomArtifact) {
                it.artifact(project.getTasks().getByName("jar"));
            }

            if (addSourceJar) {
                it.artifact(project.getTasks().getByName("sourceJar"));
            }

            if (addJavadocJar) {
                it.artifact(project.getTasks().getByName("javadocJar"));
            }

            it.getArtifacts().forEach(a -> a.setClassifier(""));

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
                if (project.getTasks().findByName("shadowJar") == null) {
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
        return new MavenConfiguration(publishing, publication);
    }

    public static void configureShadowPlugin(@NotNull Project project, @Nullable MavenPublication maven) {
        project.apply(it -> it.plugin(ShadowPlugin.class));

        if (maven != null) {
            maven.getArtifacts().removeIf(it -> it.getBuildDependencies().getDependencies(null).contains(project.getTasks().getByName("jar")));
            maven.artifact(project.getTasks().getByName("shadowJar"), it -> it.setClassifier(""));
        }
    }

    public static void configureLicenser(@NotNull Project project) {
        var headerFile = project.getRootProject().file("license_header.txt");

        if (headerFile.exists()) {
            project.apply(it -> it.plugin(Licenser.class));

            var extension = project.getExtensions().getByType(LicenseExtension.class);
            extension.setHeader(headerFile);
            extension.ignoreFailures(true);
            extension.properties(it -> {
                it.set("year", Calendar.getInstance().get(Calendar.YEAR));
            });
        }
    }

    public static void configureJavadocTasks(@NotNull Project project) {
        var task = project.getTasks().getByName("javadoc", javadocTask -> {
           if (!(javadocTask instanceof Javadoc)) {
               throw new IllegalArgumentException("Expected javadoc task, got " + javadocTask);
           }
           var javadoc = (Javadoc) javadocTask;
           javadoc.options(op -> {
               ((CoreJavadocOptions) op).addBooleanOption("html5", true);
           });
        });

        project.getTasks().create("javadocJar", Jar.class, it -> {
            it.dependsOn(task);
            it.getArchiveClassifier().set("javadoc");
            it.from(task);
        });
    }

    public static void configureSourceJarTasks(@NotNull Project project) {
        configureSourceJarTasks(project, null);
    }

    public static void configureSourceJarTasks(@NotNull Project project, @Nullable Predicate<@NotNull SourceSet> sourceSetSelector) {
        project.getTasks().create("sourceJar", Jar.class, it -> {
            it.getArchiveClassifier().set("sources");
            var sourceSets = project.getExtensions().getByType(JavaPluginExtension.class).getSourceSets();
            if (sourceSetSelector != null) {
                sourceSets.forEach(sourceSet -> {
                    if (sourceSetSelector.test(sourceSet)) {
                        it.from(sourceSet.getAllJava());
                    }
                });
            } else {
                it.from(sourceSets.getByName("main").getAllJava());
            }
        });
    }

    public static void setupAllowJavadocUploadTask(@NotNull Project project) {
        project.getTasks().create("allowJavadocUpload", it -> {
            if (project.getTasks().findByName("uploadJavadoc") != null) {
                it.dependsOn("uploadJavadoc");
            }
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

    public static void setupSftpJavadocPublishingTaskFromProperties(@NotNull Project project) {
        if (System.getenv(Constants.JAVADOC_HOST_PROPERTY) != null
                && System.getenv(Constants.JAVADOC_USER_PROPERTY) != null
                && System.getenv(Constants.JAVADOC_SECRET_PROPERTY) != null
        ) {
            setupSftpJavadocPublishingTask(
                    project,
                    System.getenv(Constants.JAVADOC_HOST_PROPERTY),
                    System.getenv(Constants.JAVADOC_USER_PROPERTY),
                    System.getenv(Constants.JAVADOC_SECRET_PROPERTY),
                    System.getenv(Constants.JAVADOC_UPLOAD_CUSTOM_DIRECTORY_PATH_PROPERTY)
            );
        }
    }

    public static void setupSftpJavadocPublishingTask(@NotNull Project project, @NotNull String javadocHost, @NotNull String javadocUser, @NotNull String javadocPassword, @Nullable String customDirectoryPath) {
        if (project.getTasks().findByName("javadoc") == null) {
            throw new IllegalStateException("Please call configureJavadocTasks() first!");
        }

        project.getTasks().register("uploadJavadoc", JavadocUploadTask.class, it -> {
            it.getSftpHost().set(System.getenv(javadocHost));
            it.getSftpUser().set(System.getenv(javadocUser));
            it.getSftpPassword().set(System.getenv(javadocPassword));
            it.getJavaDocCustomDirectoryPath().set(customDirectoryPath);
        });
    }

    public static final class MavenConfiguration {
        private final @NotNull PublishingExtension extension;
        private final @NotNull MavenPublication publication;

        public MavenConfiguration(@NotNull PublishingExtension extension, @NotNull MavenPublication publication) {
            this.extension = extension;
            this.publication = publication;
        }

        public @NotNull PublishingExtension getExtension() {
            return extension;
        }

        public @NotNull MavenPublication getPublication() {
            return publication;
        }
    }
}
