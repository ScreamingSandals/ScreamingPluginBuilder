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

package org.screamingsandals.gradle.run.installer;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.gradle.run.api.Bibliothek;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

@RequiredArgsConstructor
public class BibliothekInstaller implements Installer {
    private final @NotNull String bibliothekApiUrl;
    private final @NotNull String project;

    @Override
    public @NotNull File install(@NotNull String version, @NotNull File folder, boolean forceUpdate) throws Exception {
        if (!folder.exists()) {
            folder.mkdirs();
        }

        System.out.println("Preparing server.jar");
        var serverJar = new File(project, "server.jar"); // TODO: shared-cache?
        if (!serverJar.exists() || forceUpdate) {
            Bibliothek api = new Bibliothek(bibliothekApiUrl);

            var latestBuild = api.getLatestBuild(project, version);

            if (latestBuild == 0) {
                throw new RuntimeException("Can't obtain build number for version " + version);
            }

            var downloadUrl = api.getDownloadUrl(project, version, latestBuild);

            Files.copy(Path.of(downloadUrl), serverJar.toPath());
        }

        return serverJar;
    }
}
