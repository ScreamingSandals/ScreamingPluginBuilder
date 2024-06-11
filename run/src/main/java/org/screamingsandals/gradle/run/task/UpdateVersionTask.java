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

package org.screamingsandals.gradle.run.task;

import org.gradle.api.DefaultTask;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.gradle.run.config.Platform;
import org.screamingsandals.gradle.run.utils.Constants;

public abstract class UpdateVersionTask extends DefaultTask {
    @Input
    public abstract @NotNull Property<Platform> getPlatform();

    @Input
    public abstract @NotNull Property<String> getVersion();

    @Input
    public abstract @NotNull Property<String> getSubDirectory();

    public UpdateVersionTask() {
        setGroup(Constants.TASK_GROUP);
    }

    @TaskAction
    public void run() throws Exception {
        var testServerDirectory = this.getProject().file(getSubDirectory().get());

        try {
            getPlatform().get().obtainInstaller().install(getVersion().get(), testServerDirectory, true);
        } catch (Exception e) {
            throw new RuntimeException("Unable to update server " + getPlatform().get() + " version " + getVersion().get(), e);
        }
    }
}
