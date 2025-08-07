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

package org.screamingsandals.gradle.run.installer

import org.screamingsandals.gradle.run.api.Fill
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

class FillInstaller(private val fillApiUrl: String, private val project: String) : Installer {
    @Throws(Exception::class)
    override fun install(version: String, folder: File, forceUpdate: Boolean): File {
        if (!folder.exists()) {
            folder.mkdirs()
        }

        println("Preparing server.jar")
        val serverJar = File(folder, "server.jar") // TODO: shared-cache?
        if (!serverJar.exists() || forceUpdate) {
            val api = Fill(fillApiUrl)

            val downloadUrl = api.getDownloadUrl(project, version)

            downloadUrl.toURL().openStream().use { input ->
                Files.copy(input, serverJar.toPath(), StandardCopyOption.REPLACE_EXISTING)
            }
        }

        return serverJar
    }
}
