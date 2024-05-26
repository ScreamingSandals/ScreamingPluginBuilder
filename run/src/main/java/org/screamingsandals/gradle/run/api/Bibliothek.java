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

package org.screamingsandals.gradle.run.api;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class Bibliothek {
    private final @NotNull String baseUrl;
    private final @NotNull HttpClient httpClient = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
    private final @NotNull Gson gson = new Gson();

    public int getLatestBuild(@NotNull String project, @NotNull String version) throws URISyntaxException {
        var request = HttpRequest.newBuilder()
                .uri(new URI(this.baseUrl + "/v2/projects/" + project + "/versions/" + version))
                .GET()
                .build();

        try {
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Could not get latest build number of " + project + " v" + version + ": " + response.statusCode());
            }

            return Collections.max(gson.fromJson(response.body(), VersionResponse.class).builds);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("An exception occurred while trying to get the latest build number of " + project + " v" + version, e);
        }
    }

    public @NotNull URI getDownloadUrl(@NotNull String project, @NotNull String version, int build) throws URISyntaxException {
        return getDownloadUrl(project, version, build, "application");
    }

    public @NotNull URI getDownloadUrl(@NotNull String project, @NotNull String version, int build, @NotNull String artifactName) throws URISyntaxException {
        var request = HttpRequest.newBuilder()
                .uri(new URI(this.baseUrl + "/v2/projects/" + project + "/versions/" + version + "/builds/" + build))
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
            return new URI(this.baseUrl + "/v2/projects/" + project + "/versions/" + version + "/builds/" + build + "/downloads/" + artifact.name);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("An exception occurred while trying to get the download url of " + project + " v" + version + "#" + build, e);
        }
    }

    private static class VersionResponse {
        private @NotNull List<@NotNull Integer> builds;
    }

    private static class BuildInfo {
        private @NotNull Map<@NotNull String, Artifact> downloads;
    }

    private static class Artifact {
        private @NotNull String name;
        private @NotNull String sha1;
    }

}
