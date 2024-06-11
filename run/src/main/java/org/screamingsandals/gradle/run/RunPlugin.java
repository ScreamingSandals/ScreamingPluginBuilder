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

package org.screamingsandals.gradle.run;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.gradle.run.task.RunServerTask;
import org.screamingsandals.gradle.run.task.UpdateVersionTask;
import org.screamingsandals.gradle.run.utils.Constants;

import java.util.Locale;

public class RunPlugin implements Plugin<Project> {
    @Override
    public void apply(@NotNull Project project) {
        var extension = project.getExtensions().create("runTestServer", RunTestServerExtension.class);

        project.afterEvaluate(p1 -> {
            if (!extension.getVersions().isEmpty()) {
                extension.getVersions().forEach(version -> {
                    String serverName = version.getPlatform().name().charAt(0) + version.getPlatform().name().substring(1).toLowerCase(Locale.ROOT) + "Server" + version.getVersion();
                    project.getTasks().create("update" + serverName, UpdateVersionTask.class, it -> {
                        it.setDescription("Updates " + version.getPlatform().name().toLowerCase(Locale.ROOT) + " server version " + version.getVersion() + " to the latest build.");
                        it.getPlatform().set(version.getPlatform());
                        it.getVersion().set(version.getVersion());
                        it.getSubDirectory().set(extension.getTestingDirectory() + "/" + version.getSubDirectory());
                    });
                    project.getTasks().create("run" + serverName, RunServerTask.class, it -> {
                        it.setDescription("Runs a " + version.getPlatform().name().toLowerCase(Locale.ROOT) + " server version " + version.getVersion() + " with newly compiled plugin jar artifact.");

                        it.getPlatform().set(version.getPlatform());
                        it.getVersion().set(version.getVersion());
                        it.getSubDirectory().set(extension.getTestingDirectory() + "/" + version.getSubDirectory());
                        if (version.getPlatform().supportsServerProperties()) {
                            it.getServerProperties().set(version.getServerProperties().getServerProperties());
                        }

                        if (extension.getPluginJar() != null) {
                            it.getPluginJar().set(extension.getPluginJar());
                        } else if (project.getTasks().getNames().contains(Constants.SHADOW_JAR_TASK)) {
                            it.getPluginJar().set(project.getTasks().named(Constants.SHADOW_JAR_TASK, AbstractArchiveTask.class).flatMap(AbstractArchiveTask::getArchiveFile));
                        } else {
                            it.getPluginJar().set(project.getTasks().named(JavaPlugin.JAR_TASK_NAME, AbstractArchiveTask.class).flatMap(AbstractArchiveTask::getArchiveFile));
                        }

                        it.args(version.getArgs());
                        it.jvmArgs(version.getJvmArgs());
                    });
                });
            }
        });
    }
}
