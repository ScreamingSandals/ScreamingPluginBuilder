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

import com.github.jengelman.gradle.plugins.shadow.ShadowJavaPlugin;
import com.github.jengelman.gradle.plugins.shadow.ShadowPlugin;
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar;
import org.cadixdev.gradle.licenser.LicenseExtension;
import org.cadixdev.gradle.licenser.Licenser;
import org.gradle.api.Project;
import org.gradle.api.file.DuplicatesStrategy;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.Jar;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Calendar;
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

    public static @NotNull TaskProvider<Jar> configureSourcesJar(@NotNull Project project) {
        return configureSourcesJar(project, null);
    }

    public static @NotNull TaskProvider<Jar> configureSourcesJar(@NotNull Project project, @Nullable Predicate<@NotNull SourceSet> sourceSetSelector) {
        return project.getTasks().register(Constants.SOURCES_JAR_TASK_NAME, Jar.class, it -> {
            it.setDuplicatesStrategy(DuplicatesStrategy.EXCLUDE);
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
}
