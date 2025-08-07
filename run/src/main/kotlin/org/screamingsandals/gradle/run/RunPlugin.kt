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

package org.screamingsandals.gradle.run

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.screamingsandals.gradle.run.task.RunServerTask
import org.screamingsandals.gradle.run.task.UpdateVersionTask

class RunPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create("runTestServer", RunTestServerExtension::class.java)

        project.afterEvaluate {
            if (!extension.versions.isEmpty()) {
                extension.versions.forEach { version ->
                    val serverName = buildString {
                        append(version.platform.name[0].toString())
                        append(version.platform.name.substring(1).lowercase())
                        append("Server")
                        append(version.version)
                    }
                    val directory = project.file("${extension.testingDirectory}/${version.subDirectory}").absolutePath
                    project.tasks.register("update$serverName", UpdateVersionTask::class.java) {
                        it.description = "Updates ${version.platform.name.lowercase()} server version ${version.version} to the latest build."
                        it.platform.set(version.platform)
                        it.version.set(version.version)
                        it.directory.set(directory)
                    }
                    project.tasks.register("run$serverName", RunServerTask::class.java) {
                        it.description = "Runs a ${version.platform.name.lowercase()} server version ${version.version} with newly compiled plugin jar artifact."
                        it.platform.set(version.platform)
                        it.version.set(version.version)
                        it.directory.set(directory)
                        if (version.platform.supportsServerProperties) {
                            it.serverProperties.set(version.internalServerProperties?.serverProperties)
                        }

                        it.pluginJar.set(
                            if (extension.pluginJar != null) {
                                extension.pluginJar!!
                            } else if (project.tasks.names.contains(SHADOW_JAR_TASK)) {
                                project.tasks
                                    .named(SHADOW_JAR_TASK, AbstractArchiveTask::class.java)
                                    .flatMap { obj -> obj.archiveFile }
                            } else {
                                project.tasks.named(JavaPlugin.JAR_TASK_NAME, AbstractArchiveTask::class.java)
                                    .flatMap { obj -> obj.archiveFile }
                            }
                        )

                        it.args(version.args)
                        it.jvmArgs(version.jvmArgs)
                    }
                }
            }
        }
    }
}
