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
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.screamingsandals.gradle.builder.maven.NexusRepository

class LiteBuilderPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        def ciCdOptimized = System.getenv("OPTIMIZE_FOR_CI_CD") == "1"

        project.apply {
            plugin MavenPublishPlugin.class
            plugin JavaLibraryPlugin.class
        }

        PublishingExtension publishing = project.extensions.getByName("publishing")
        if (System.getProperty("LITE_SKIP_PUBLICATION_CREATION") != "yes") {
            def pair = Utilities.setupPublishing(project, !project.ext.has('onlyPomArtifact') || !project.ext['onlyPomArtifact'], false, false).publication

            project.ext['enableShadowPlugin'] = {
                Utilities.enableShadowPlugin(project, maven)
            }
        }

        project.ext['configureLombok'] = { iit ->
            Utilities.configureLombok(project)
        }

        project.ext['configureLicenser'] = { iit ->
            Utilities.configureLicenser(project)
        }

        if (System.getenv("NEXUS_URL_SNAPSHOT") != null && System.getenv("NEXUS_URL_RELEASE") != null) {
            new NexusRepository().setup(project, publishing)
        }

        List tasks = ["build"]

        if (!ciCdOptimized) {
            tasks.add("publishToMavenLocal")
        }

        if (System.getenv("NEXUS_URL_SNAPSHOT") != null && System.getenv("NEXUS_URL_RELEASE") != null) {
            tasks.add("publish")
        }

        project.tasks.create("screamCompile").dependsOn = tasks
    }

}