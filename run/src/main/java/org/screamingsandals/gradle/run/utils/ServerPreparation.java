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

package org.screamingsandals.gradle.run.utils;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.gradle.run.api.Bibliothek;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

@UtilityClass
public final class ServerPreparation {
    public static File prepareServer(@NotNull File testServerDirectory, @NotNull String version, boolean forceUpdate) throws URISyntaxException, IOException {
        if (!testServerDirectory.exists()) {
            testServerDirectory.mkdirs();
        }

        // TODO: abstract following lines
        Bibliothek api = new Bibliothek("https://api.papermc.io");

        System.out.println("Preparing server.jar");
        var serverJar = new File(testServerDirectory, "server.jar");
        if (!serverJar.exists() || forceUpdate) {
            var latestBuild = api.getLatestBuild("paper", version);

            if (latestBuild == 0) {
                throw new RuntimeException("Can't obtain build number for version " + version);
            }

            var downloadUrl = api.getDownloadUrl("paper", version, latestBuild);

            Files.copy(Path.of(downloadUrl), serverJar.toPath());
        }

        return serverJar;
    }
}
