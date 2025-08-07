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

package org.screamingsandals.gradle.run.api

import com.google.gson.Gson
import org.screamingsandals.gradle.run.VersionInfo
import java.io.IOException
import java.net.URI
import java.net.URISyntaxException
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class Fill(private val baseUrl: String) {
    private val httpClient: HttpClient = HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build()
    private val gson = Gson()

    @Throws(URISyntaxException::class)
    fun getDownloadUrl(project: String, version: String): URI {
        return getDownloadUrl(project, version, "latest")
    }

    @Throws(URISyntaxException::class)
    fun getDownloadUrl(project: String, version: String, build: String): URI {
        return getDownloadUrl(project, version, build, "server:default")
    }

    @Throws(URISyntaxException::class)
    fun getDownloadUrl(project: String, version: String, build: String, artifactName: String): URI {
        val request = HttpRequest.newBuilder()
            .uri(URI("${this.baseUrl}/v3/projects/$project/versions/$version/builds/$build"))
            .header(
                "User-Agent",
                "screaming-gradle/${VersionInfo.VERSION} (https://github.com/ScreamingSandals/ScreamingPluginBuilder)"
            )
            .GET()
            .build()

        try {
            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())

            if (response.statusCode() != 200) {
                throw RuntimeException("Could not get the download url of $project v$version#$build: ${response.statusCode()}")
            }

            val buildInfo: BuildInfo = gson.fromJson(response.body(), BuildInfo::class.java)

            val artifact = buildInfo.downloads[artifactName]
                ?: throw RuntimeException("Could not get the download url of $project v$version#$build: artifact $artifactName is unknown")
            return URI(artifact.url)
        } catch (e: IOException) {
            throw RuntimeException("An exception occurred while trying to get the download url of $project v$version#$build", e)
        } catch (e: InterruptedException) {
            throw RuntimeException("An exception occurred while trying to get the download url of $project v$version#$build", e)
        }
    }

    private data class BuildInfo(val downloads: MutableMap<String, Artifact>)

    private data class Artifact(val url: String)
}
