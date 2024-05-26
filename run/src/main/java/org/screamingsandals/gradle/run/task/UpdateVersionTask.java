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

import lombok.Setter;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;
import org.jetbrains.annotations.Nullable;
import org.screamingsandals.gradle.run.utils.ServerPreparation;

import java.io.IOException;
import java.net.URISyntaxException;

public class UpdateVersionTask extends DefaultTask {
    @Internal
    @Setter
    private @Nullable String version;
    @Internal
    @Setter
    private @Nullable String subDirectory;

    @TaskAction
    public void run() throws URISyntaxException, IOException {
        var testServerDirectory = this.getProject().file("test-environment/" + subDirectory + "/" + version);

        ServerPreparation.prepareServer(testServerDirectory, version, true);
    }
}
