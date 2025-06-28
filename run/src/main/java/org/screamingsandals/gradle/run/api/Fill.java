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

package org.screamingsandals.gradle.run.api;

import com.google.gson.Gson;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.gradle.run.VersionInfo;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public class Fill {
    private final @NotNull String baseUrl;
    private final @NotNull HttpClient httpClient = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
    private final @NotNull Gson gson = new Gson();

    public Fill(@NotNull String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public @NotNull URI getDownloadUrl(@NotNull String project, @NotNull String version) throws URISyntaxException {
        return getDownloadUrl(project, version, "latest");
    }

    public @NotNull URI getDownloadUrl(@NotNull String project, @NotNull String version, @NotNull String build) throws URISyntaxException {
        return getDownloadUrl(project, version, build, "server:default");
    }

    public @NotNull URI getDownloadUrl(@NotNull String project, @NotNull String version, @NotNull String build, @NotNull String artifactName) throws URISyntaxException {
        var request = HttpRequest.newBuilder()
                .uri(new URI(this.baseUrl + "/v3/projects/" + project + "/versions/" + version + "/builds/" + build))
                .header("User-Agent", "screaming-gradle/" + VersionInfo.VERSION + " (https://github.com/ScreamingSandals/ScreamingPluginBuilder)")
                .GET()
                .build();

        try {
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Could not get the download url of " + project + " v" + version + "#" + build + ": " + response.statusCode());
            }

            var buildInfo = gson.fromJson(response.body(), BuildInfo.class);

            var artifact = buildInfo.downloads.get(artifactName);
            if (artifact == null) {
                throw new RuntimeException("Could not get the download url of " + project + " v" + version + "#" + build + ": artifact " + artifactName + " is unknown");
            }
            return new URI(artifact.url);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("An exception occurred while trying to get the download url of " + project + " v" + version + "#" + build, e);
        }
    }

    private static class BuildInfo {
        private @NotNull Map<@NotNull String, Artifact> downloads;
    }

    private static class Artifact {
        private @NotNull String url;
    }
}
