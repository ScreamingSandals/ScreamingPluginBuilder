/*
 * Copyright 2026 ScreamingSandals
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

import java.io.File
import java.net.URL
import java.nio.file.Files
import java.nio.file.StandardCopyOption

class BuildToolsInstaller(
    private val buildToolsUrl: String
) : Installer {

    override fun install(version: String, folder: File, forceUpdate: Boolean, cacheDir: File): File {
        if (!folder.exists()) {
            folder.mkdirs()
        }

        val buildToolsDir = File(cacheDir, "buildtools") // TODO: shared system-wide cache?

        if (!buildToolsDir.exists()) {
            buildToolsDir.mkdirs()
        }

        val buildToolsJar = File(buildToolsDir, "BuildTools.jar")
        val spigotJar = File(buildToolsDir, "spigot-$version.jar")

        if (spigotJar.exists() && !forceUpdate) {
            println("Using cached Spigot jar: ${spigotJar.name}")
            return spigotJar
        }

        if (!buildToolsJar.exists()) {
            println("Downloading BuildTools...")
            URL(buildToolsUrl).openStream().use { input ->
                Files.copy(input, buildToolsJar.toPath(), StandardCopyOption.REPLACE_EXISTING)
            } // TODO: BuildTools updating
        }

        println("Running BuildTools for version $version (this may take a while)")

        val process = ProcessBuilder(
            File(System.getProperty("java.home"),"bin/java").absolutePath,
            "-Xmx2G",
            "-jar", buildToolsJar.absolutePath,
            "--rev", version,
            "--output-dir", buildToolsDir.absolutePath,
            "--final-name", spigotJar.name,
            "--disable-java-check",
            "--nogui"
        )
            .directory(buildToolsDir)
            .inheritIO()
            .start() // TODO: something more Gradle?

        val exit = process.waitFor()
        if (exit != 0) {
            throw RuntimeException("BuildTools failed ($exit). See $buildToolsDir/BuildTools.log.txt for details.")
        }

        if (!spigotJar.exists()) {
            throw RuntimeException("Expected output jar not found: ${spigotJar.name}. See $buildToolsDir/BuildTools.log.txt for details.")
        }

        return spigotJar
    }
}
