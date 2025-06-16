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

import com.github.jengelman.gradle.plugins.shadow.ShadowJavaPlugin;
import com.github.jengelman.gradle.plugins.shadow.ShadowPlugin;
import com.github.jengelman.gradle.plugins.shadow.relocation.RelocatePathContext;
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar;
import org.cadixdev.gradle.licenser.LicenseExtension;
import org.cadixdev.gradle.licenser.Licenser;
import org.gradle.api.Action;
import org.gradle.api.JavaVersion;
import org.gradle.api.Project;
import org.gradle.api.file.CopySpec;
import org.gradle.api.file.DuplicatesStrategy;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.compile.JavaCompile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.gradle.dsl.JvmTarget;
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension;
import org.screamingsandals.gradle.builder.shadow.RelocateFilterReader;

import java.util.Calendar;
import java.util.Map;
import java.util.function.Predicate;

public final class Utilities {
    private Utilities() {
    }

    public static @NotNull TaskProvider<ShadowJar> configureShadowPlugin(@NotNull Project project) {
        project.apply(it -> it.plugin(ShadowPlugin.class));

        var jarTask = project.getTasks().withType(Jar.class).getByName("jar");
        var oldClassifier = jarTask.getArchiveClassifier().get();
        jarTask.getArchiveClassifier().set("unshaded");
        var shadowJarTask = project.getTasks().named(ShadowJavaPlugin.SHADOW_JAR_TASK_NAME, ShadowJar.class, shadowJar -> {
            shadowJar.getArchiveClassifier().set(oldClassifier);
        });
        project.getTasks().named("build", build -> {
            build.dependsOn(ShadowJavaPlugin.SHADOW_JAR_TASK_NAME);
        });
        return shadowJarTask;
    }

    public static @Nullable LicenseExtension configureLicenser(@NotNull Project project) {
        var headerFile = project.getRootProject().file("license_header.txt");

        if (!headerFile.exists()) {
            return null;
        }

        project.apply(it -> it.plugin(Licenser.class));

        var extension = project.getExtensions().getByType(LicenseExtension.class);
        extension.setHeader(headerFile);
        extension.ignoreFailures(true);
        extension.properties(it -> {
            it.set("year", Calendar.getInstance().get(Calendar.YEAR));
        });
        return extension;
    }

    public static @NotNull JarPair configureSourcesJar(@NotNull Project project) {
        return configureSourcesJar(project, null);
    }

    public static @NotNull JarPair configureSourcesJar(@NotNull Project project, @Nullable Predicate<@NotNull SourceSet> sourceSetSelector) {
        var shadowJar = project.getTasks().withType(ShadowJar.class).findByName(ShadowJavaPlugin.SHADOW_JAR_TASK_NAME);
        Action<CopySpec> specAction;
        if (shadowJar != null) {
            specAction = spec -> {
                // Use our FilterReader with relocators from shadow
                spec.filter(Map.of("relocators", shadowJar.getRelocators()), RelocateFilterReader.class);

                spec.eachFile(fileCopyDetails -> {
                    String path = fileCopyDetails.getPath();
                    for (var relocator : shadowJar.getRelocators().get()) {
                        if (relocator.canRelocatePath(path)) {
                            String relocatedPath = relocator.relocatePath(new RelocatePathContext(path));
                            fileCopyDetails.setPath(relocatedPath);
                            break;
                        }
                    }
                });
            };
        } else {
            specAction = null;
        }

        return new JarPair(project.getTasks().register(Constants.SOURCES_JAR_TASK_NAME, Jar.class, it -> {
            it.setDuplicatesStrategy(DuplicatesStrategy.EXCLUDE);
            it.getArchiveClassifier().set("sources");

            var sourceSets = project.getExtensions().getByType(JavaPluginExtension.class).getSourceSets();
            if (sourceSetSelector != null) {
                sourceSets.forEach(sourceSet -> {
                    if (sourceSetSelector.test(sourceSet)) {
                        if (specAction != null) {
                            it.from(sourceSet.getAllJava(), specAction);
                        } else {
                            it.from(sourceSet.getAllJava());
                        }
                    }
                });
            } else {
                if (specAction != null) {
                    it.from(sourceSets.getByName("main").getAllJava(), specAction);
                } else {
                    it.from(sourceSets.getByName("main").getAllJava());
                }
            }
        }), specAction);
    }

    public static void configureJavac(@NotNull Project project, @NotNull JavaVersion javaVersion) {
        project.getExtensions().configure(JavaPluginExtension.class, extension -> {
            extension.setSourceCompatibility(javaVersion);
        });
        project.getTasks().withType(JavaCompile.class, task -> {
            task.getOptions().getCompilerArgs().add("-Xlint:deprecation");
            task.getOptions().getRelease().set(Integer.parseInt(javaVersion.getMajorVersion()));
        });
        // Automatically configure Kotlin if present
        if (project.getPlugins().hasPlugin("org.jetbrains.kotlin.jvm")) {
           project.getExtensions().getByType(KotlinJvmProjectExtension.class).getCompilerOptions().getJvmTarget().set(JvmTarget.fromTarget(javaVersion.toString()));
        }
    }

    public static final class JarPair {
        private final @NotNull TaskProvider<Jar> task;
        private final @Nullable Action<@NotNull CopySpec> copySpecAction;

        public JarPair(@NotNull TaskProvider<Jar> task, @Nullable Action<@NotNull CopySpec> copySpecAction) {
            this.task = task;
            this.copySpecAction = copySpecAction;
        }

        public @NotNull TaskProvider<Jar> getTask() {
            return task;
        }

        public @Nullable Action<@NotNull CopySpec> getCopySpecAction() {
            return copySpecAction;
        }
    }
}
