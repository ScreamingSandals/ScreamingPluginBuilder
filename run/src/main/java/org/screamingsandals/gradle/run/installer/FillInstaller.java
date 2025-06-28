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

package org.screamingsandals.gradle.run.installer;

import org.jetbrains.annotations.NotNull;
import org.screamingsandals.gradle.run.api.Fill;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class FillInstaller implements Installer {
    private final @NotNull String fillApiUrl;
    private final @NotNull String project;

    public FillInstaller(@NotNull String fillApiUrl, @NotNull String project) {
        this.fillApiUrl = fillApiUrl;
        this.project = project;
    }

    @Override
    public @NotNull File install(@NotNull String version, @NotNull File folder, boolean forceUpdate) throws Exception {
        if (!folder.exists()) {
            folder.mkdirs();
        }

        System.out.println("Preparing server.jar");
        var serverJar = new File(folder, "server.jar"); // TODO: shared-cache?
        if (!serverJar.exists() || forceUpdate) {
            Fill api = new Fill(fillApiUrl);

            var downloadUrl = api.getDownloadUrl(project, version);

            try (InputStream in = downloadUrl.toURL().openStream()) {
                Files.copy(in, serverJar.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        }

        return serverJar;
    }
}
