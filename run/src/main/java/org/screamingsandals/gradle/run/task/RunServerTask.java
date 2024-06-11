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

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.JavaExec;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.gradle.run.config.Platform;
import org.screamingsandals.gradle.run.utils.Constants;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public abstract class RunServerTask extends JavaExec {
    @Input
    public abstract @NotNull Property<Platform> getPlatform();

    @Input
    public abstract @NotNull Property<String> getVersion();

    @Input
    public abstract @NotNull Property<String> getSubDirectory();

    @InputFile
    public abstract @NotNull RegularFileProperty getPluginJar();

    @Input
    public abstract @NotNull MapProperty<String, String> getServerProperties();

    public RunServerTask() {
        setGroup(Constants.TASK_GROUP);
        getServerProperties().convention(Map.of());
        setStandardInput(System.in);
    }

    @Override
    public void exec() {
        var platform = this.getPlatform().get();
        var version = this.getVersion().get();
        var testServerDirectory = this.getProject().file(getSubDirectory().get());
        var pluginJar = this.getPluginJar().getAsFile().get().toPath();

        @NotNull File serverExecutable;
        try {
            serverExecutable = platform.obtainInstaller().install(version, testServerDirectory, false);
        } catch (Exception e) {
            throw new RuntimeException("Unable to install server " + platform + " version " + version, e);
        }

        if (platform.hasEula()) {
            var eulaTxt = new File(testServerDirectory, "eula.txt");
            if (!eulaTxt.exists()) {
                this.getLogger().info("By using this testing service, you agree to the EULA. Please refer to it at https://account.mojang.com/documents/minecraft_eula");
                try {
                    Files.writeString(eulaTxt.toPath(), "eula=true");
                } catch (IOException e) {
                    throw new RuntimeException("Unable to create file eula.txt", e);
                }
            }
        }

        if (platform.supportsServerProperties()) {
            this.getLogger().info("Preparing server.properties");
            var serverProperties = new File(testServerDirectory, "server.properties");
            var props = new Properties();
            if (serverProperties.exists()) {
                try (var reader = new FileReader(serverProperties, StandardCharsets.UTF_8)) {
                    props.load(reader);
                } catch (IOException e) {
                    throw new RuntimeException("Unable to read existing server.properties file", e);
                }
            }
            var properties = new HashMap<>(this.getServerProperties().get());
            if (version.matches("1\\.(\\d|10|11)\\..*")) {
                properties.put("use-native-transport", "false");
            }
            var needsToBeSaved = false;
            for (var it : properties.entrySet()) {
                if (!it.getValue().equals(props.getProperty(it.getKey()))) {
                    props.setProperty(it.getKey(), it.getValue());
                    needsToBeSaved = true;
                }
            }
            if (needsToBeSaved) {
                try (var writer = new FileWriter(serverProperties, StandardCharsets.UTF_8, false)) {
                    props.store(writer, null);
                } catch (IOException e) {
                    throw new RuntimeException("Unable to write to the server.properties file", e);
                }
            }
        }

        this.getLogger().info("Preparing plugin");
        var plugins = new File(testServerDirectory, platform.pluginDirName());
        if (!plugins.exists()) {
            plugins.mkdirs();
        }

        if (!platform.supportsPluginAsParameter() || version.matches("1\\.(([0-9]|1[0-5])(\\..*)?$|16(\\.[0-4])?$)")) { // old versions
            try {
                Files.copy(pluginJar, testServerDirectory.toPath().resolve(platform.pluginDirName() + "/debugPlugin.jar"), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new RuntimeException("Unable to copy plugin jar to " + platform.pluginDirName() + " folder", e);
            }
        } else {
            // make sure plugins/debugPlugin.jar doesn't exist anymore
            if (Files.exists(testServerDirectory.toPath().resolve(platform.pluginDirName() + "/debugPlugin.jar"))) {
                try {
                    Files.delete(testServerDirectory.toPath().resolve(platform.pluginDirName() + "/debugPlugin.jar"));
                } catch (IOException e) {
                    throw new RuntimeException("Unable to remove existing debugPlugin.jar file", e);
                }
            }

            args("-add-plugin=" + pluginJar.toAbsolutePath());
        }


        classpath(serverExecutable);
        setWorkingDir(testServerDirectory);

        super.exec();
    }
}
