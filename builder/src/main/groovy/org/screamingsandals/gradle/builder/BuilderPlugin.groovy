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

package org.screamingsandals.gradle.builder

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.screamingsandals.gradle.builder.maven.NexusRepository
import org.screamingsandals.gradle.builder.tasks.JavadocUploadTask
import org.screamingsandals.gradle.builder.webhook.DiscordWebhookExtension

class BuilderPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        def ciCdOptimized = System.getenv("OPTIMIZE_FOR_CI_CD") == "1"

        project.apply {
            plugin MavenPublishPlugin.class
            plugin JavaLibraryPlugin.class
        }

        Utilities.configureLombok(project)
        Utilities.configureLicenser(project)

        if (System.getenv("NEXUS_URL_SNAPSHOT") != null && System.getenv("NEXUS_URL_RELEASE") != null) {
            Utilities.configureSourceJarTasks(project)
        }

        if (System.getenv('JAVADOC_HOST') != null) {
            Utilities.configureJavadocTasks(project)

            if (System.getenv('JAVADOC_HOST') != null && System.getenv('JAVADOC_USER') != null && System.getenv('JAVADOC_SECRET') != null) {
                project.tasks.register("uploadJavadoc", JavadocUploadTask) {
                    it.sftpHost = System.getenv('JAVADOC_HOST')
                    it.sftpUser = System.getenv('JAVADOC_USER')
                    it.sftpPassword = System.getenv('JAVADOC_SECRET')
                    it.javaDocCustomDirectoryPath = System.getProperty("JavadocUploadCustomDirectoryPath")
                }
            }
        }

        def maven = Utilities.setupPublishing(project, false, System.getenv("NEXUS_URL_SNAPSHOT") != null && System.getenv("NEXUS_URL_RELEASE") != null, false)

        project.ext['enableShadowPlugin'] = {
            Utilities.enableShadowPlugin(project, maven.publication)
        }

        List tasks = ["build"]

        if (!project.hasProperty('disablePublishingToMaven') || !project.property('disablePublishingToMaven')) {
            if (System.getenv("NEXUS_URL_SNAPSHOT") != null && System.getenv("NEXUS_URL_RELEASE") != null) {
                new NexusRepository().setup(project, maven.extension)
            }

            if (!ciCdOptimized) {
                tasks.add("publishToMavenLocal")
            }

            if (System.getenv("NEXUS_URL_SNAPSHOT") != null && System.getenv("NEXUS_URL_RELEASE") != null) {
                tasks.add("publish")
            }
        }

        project.getExtensions().create("discord", DiscordWebhookExtension)

        project.tasks.create("screamCompile").dependsOn = tasks

        project.tasks.create("allowJavadocUpload") {
            if (project.tasks.findByName("uploadJavadoc") != null) {
                dependsOn("uploadJavadoc")
            }
        }
    }
}