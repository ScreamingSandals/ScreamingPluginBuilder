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
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.external.javadoc.CoreJavadocOptions;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class JavadocUtilities {
    private JavadocUtilities() {
    }

    public static @NotNull Javadoc configureJavadocTasks(@NotNull Project project, boolean useSourcesJarAsInput) {
        var task = project.getTasks().getByName(Constants.JAVADOC_TASK_NAME, javadocTask -> {
            if (!(javadocTask instanceof Javadoc)) {
                throw new IllegalArgumentException("Expected javadoc task, got " + javadocTask);
            }
            var javadoc = (Javadoc) javadocTask;
            javadoc.options(op -> {
                ((CoreJavadocOptions) op).addBooleanOption("html5", true);

                var javac = project.getTasks().withType(JavaCompile.class).getByName("compileJava");

                var release = javac.getOptions().getRelease();
                if (release.isPresent()) {
                    ((CoreJavadocOptions) op).addStringOption("-release", release.get().toString());
                }
                ((CoreJavadocOptions) op).addMultilineStringsOption("tag").setValue(List.of("apiNote:a:API Note:", "implSpec:a:Implementation Requirements:", "implNote:a:Implementation Note:"));
            });
            if (useSourcesJarAsInput) {
                var sourcesJarTask = project.getTasks().withType(Jar.class).getByName(Constants.SOURCES_JAR_TASK_NAME);
                javadoc.dependsOn(sourcesJarTask);
                javadoc.setSource(project.zipTree(sourcesJarTask.getArchiveFile()).matching(patternFilterable -> {
                    patternFilterable.include("**/*.java");
                }));
            }
        });

        project.getTasks().register(Constants.JAVADOC_JAR_TASK_NAME, Jar.class, it -> {
            it.dependsOn(task);
            it.getArchiveClassifier().set("javadoc");
            it.from(task);
        });
        return (Javadoc) task;
    }
}
