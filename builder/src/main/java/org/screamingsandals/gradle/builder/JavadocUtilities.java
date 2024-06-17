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

import org.gradle.api.Project;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.external.javadoc.CoreJavadocOptions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.screamingsandals.gradle.builder.tasks.JavadocUploadTask;

public final class JavadocUtilities {
    private JavadocUtilities() {
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

    public static void setupAllowJavadocUploadTask(@NotNull Project project) {
        project.getTasks().create("allowJavadocUpload", it -> {
            if (project.getTasks().findByName("uploadJavadoc") != null) {
                it.dependsOn("uploadJavadoc");
            }
        });
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
            it.getSftpHost().set(javadocHost);
            it.getSftpUser().set(javadocUser);
            it.getSftpPassword().set(javadocPassword);
            it.getJavaDocCustomDirectoryPath().set(customDirectoryPath);
        });
    }
}
