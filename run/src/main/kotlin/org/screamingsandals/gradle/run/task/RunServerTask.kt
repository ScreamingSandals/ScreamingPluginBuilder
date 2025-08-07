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

package org.screamingsandals.gradle.run.task

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.JavaExec
import org.screamingsandals.gradle.run.config.Platform
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.Properties

abstract class RunServerTask : JavaExec() {
    @get:Input
    abstract val platform: Property<Platform>

    @get:Input
    abstract val version: Property<String>

    @get:Input
    abstract val directory: Property<String>

    @get:InputFile
    abstract val pluginJar: RegularFileProperty

    @get:Input
    abstract val serverProperties: MapProperty<String, String>

    init {
        group = TASK_GROUP
        this.serverProperties.convention(mapOf())
        standardInput = System.`in`
    }

    override fun exec() {
        val platform = this.platform.get()
        val version = this.version.get()
        val testServerDirectory = File(this.directory.get())
        val pluginJar = this.pluginJar.asFile.get().toPath()

        val serverExecutable: File
        try {
            serverExecutable = platform.obtainInstaller().install(version, testServerDirectory, false)
        } catch (e: Exception) {
            throw RuntimeException("Unable to install server $platform version $version", e)
        }

        if (platform.hasEula) {
            val eulaTxt = File(testServerDirectory, "eula.txt")
            if (!eulaTxt.exists()) {
                this.logger.info("By using this testing service, you agree to the EULA. Please refer to it at https://account.mojang.com/documents/minecraft_eula")
                try {
                    Files.writeString(eulaTxt.toPath(), "eula=true")
                } catch (e: IOException) {
                    throw RuntimeException("Unable to create file eula.txt", e)
                }
            }
        }

        if (platform.supportsServerProperties) {
            this.logger.info("Preparing server.properties")
            val serverProperties = File(testServerDirectory, "server.properties")
            val props = Properties()
            if (serverProperties.exists()) {
                try {
                    FileReader(serverProperties, StandardCharsets.UTF_8).use { reader ->
                        props.load(reader)
                    }
                } catch (e: IOException) {
                    throw RuntimeException("Unable to read existing server.properties file", e)
                }
            }
            val properties = HashMap<String, String>(this.serverProperties.get())
            if (version.matches("1\\.(\\d|10|11)\\..*".toRegex())) {
                properties["use-native-transport"] = "false"
            }
            var needsToBeSaved = false
            for (it in properties.entries) {
                if (it.value != props.getProperty(it.key)) {
                    props.setProperty(it.key, it.value)
                    needsToBeSaved = true
                }
            }
            if (needsToBeSaved) {
                try {
                    FileWriter(serverProperties, StandardCharsets.UTF_8, false).use { writer ->
                        props.store(writer, null)
                    }
                } catch (e: IOException) {
                    throw RuntimeException("Unable to write to the server.properties file", e)
                }
            }
        }

        this.logger.info("Preparing plugin")
        val plugins = File(testServerDirectory, platform.pluginDirName)
        if (!plugins.exists()) {
            plugins.mkdirs()
        }

        if (!platform.supportsPluginAsParameter || version.matches("1\\.(([0-9]|1[0-5])(\\..*)?$|16(\\.[0-4])?$)".toRegex())) { // old versions
            try {
                Files.copy(
                    pluginJar,
                    testServerDirectory.toPath().resolve("${platform.pluginDirName}/debugPlugin.jar"),
                    StandardCopyOption.REPLACE_EXISTING
                )
            } catch (e: IOException) {
                throw RuntimeException("Unable to copy plugin jar to ${platform.pluginDirName} folder", e)
            }
        } else {
            // make sure plugins/debugPlugin.jar doesn't exist anymore
            if (Files.exists(testServerDirectory.toPath().resolve("${platform.pluginDirName}/debugPlugin.jar"))) {
                try {
                    Files.delete(testServerDirectory.toPath().resolve("${platform.pluginDirName}/debugPlugin.jar"))
                } catch (e: IOException) {
                    throw RuntimeException("Unable to remove existing debugPlugin.jar file", e)
                }
            }

            args("-add-plugin=${pluginJar.toAbsolutePath()}")
        }


        classpath(serverExecutable)
        workingDir = testServerDirectory

        super.exec()
    }
}
