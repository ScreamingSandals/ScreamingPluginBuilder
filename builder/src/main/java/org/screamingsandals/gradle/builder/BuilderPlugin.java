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

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaLibraryPlugin;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.gradle.builder.tasks.JavadocUploadTask;

public class BuilderPlugin implements Plugin<Project> {
    @Override
    public void apply(@NotNull Project project) {
        var ciCdOptimized = "1".equals(System.getenv("OPTIMIZE_FOR_CI_CD"));

        project.apply(it -> {
            it.plugin(MavenPublishPlugin.class);
            it.plugin(JavaLibraryPlugin.class);
        });

        Utilities.configureLombok(project);
        Utilities.configureLicenser(project);

        if (System.getenv("NEXUS_URL_SNAPSHOT") != null && System.getenv("NEXUS_URL_RELEASE") != null) {
            Utilities.configureSourceJarTasks(project);
        }

        if (System.getenv("JAVADOC_HOST") != null) {
            Utilities.configureJavadocTasks(project);

            if (System.getenv("JAVADOC_HOST") != null && System.getenv("JAVADOC_USER") != null && System.getenv("JAVADOC_SECRET") != null) {
                project.getTasks().register("uploadJavadoc", JavadocUploadTask.class, it -> {
                    it.getSftpHost().set(System.getenv("JAVADOC_HOST"));
                    it.getSftpUser().set(System.getenv("JAVADOC_USER"));
                    it.getSftpPassword().set(System.getenv("JAVADOC_SECRET"));
                    it.getJavaDocCustomDirectoryPath().set(System.getProperty("JavadocUploadCustomDirectoryPath"));
                });
            }
        }

        var maven = Utilities.setupPublishing(project, false, System.getenv("NEXUS_URL_SNAPSHOT") != null && System.getenv("NEXUS_URL_RELEASE") != null, false);

        Utilities.configureShadowPlugin(project, maven.getPublication());

        Utilities.setupAllowJavadocUploadTask(project);

        if (!project.hasProperty("disablePublishingToMaven") || !"true".equalsIgnoreCase(String.valueOf(project.property("disablePublishingToMaven")))) {
            Utilities.setupMavenRepositoriesFromProperties(project);
        }

        Utilities.configureScreamCompileTask(project, !project.hasProperty("disablePublishingToMaven") || !"true".equalsIgnoreCase(String.valueOf(project.property("disablePublishingToMaven"))), ciCdOptimized);
    }
}
