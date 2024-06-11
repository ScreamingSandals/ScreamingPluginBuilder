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
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Calendar;

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

        var dependsOn = project.getTasks().getByName("screamCompile").getDependsOn();
        dependsOn.remove("build");
        dependsOn.add("shadowJar");
        project.getTasks().getByName("screamCompile").setDependsOn(dependsOn);

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

    @Data
    public static final class MavenConfiguration {
        private final @NotNull PublishingExtension extension;
        private final @NotNull MavenPublication publication;
    }
}
