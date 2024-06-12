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
import io.freefair.gradle.plugins.lombok.LombokPlugin;
import lombok.Data;
import lombok.experimental.UtilityClass;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.function.Predicate;

@UtilityClass
public class Utilities {
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

    public static void configureLombok(@NotNull Project project) {
        project.apply(it -> it.plugin(LombokPlugin.class));
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
        if (System.getenv("NEXUS_URL_SNAPSHOT") != null && System.getenv("NEXUS_URL_RELEASE") != null) {
            new NexusRepository().setup(project, publishing);
        }
    }

    public static void configureScreamCompileTask(@NotNull Project project, boolean mavenPublishing, boolean disableMavenLocal) {
        project.afterEvaluate(it -> {
            var hasShadowPlugin = project.getTasks().findByName("shadowJar") != null;

            var tasks = new ArrayList<String>();

            if (hasShadowPlugin) {
                tasks.add("shadowJar");
            } else {
                tasks.add("build");
            }

            if (mavenPublishing) {
                if (!disableMavenLocal) {
                    tasks.add("publishToMavenLocal");
                }

                var publishing = (PublishingExtension) project.getExtensions().findByName("publishing");
                if (publishing != null && !publishing.getRepositories().isEmpty()) {
                    tasks.add("publish");
                }
            }
            project.getTasks().create("screamCompile").setDependsOn(tasks);
        });
    }

    @Data
    public static final class MavenConfiguration {
        private final @NotNull PublishingExtension extension;
        private final @NotNull MavenPublication publication;
    }
}
