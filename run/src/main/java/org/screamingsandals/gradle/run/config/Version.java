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

package org.screamingsandals.gradle.run.config;

import org.gradle.api.Action;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Version {
    private final @NotNull Platform platform;
    private final @NotNull String version;
    private @NotNull String subDirectory;
    private final @Nullable SingleServerProperties serverProperties;
    private @NotNull List<@NotNull String> args = new ArrayList<>(List.of("nogui"));
    private @NotNull List<@NotNull String> jvmArgs = new ArrayList<>();

    public Version(@NotNull Platform platform, @NotNull String version, @NotNull String subDirectory) {
        this.platform = platform;
        this.version = version;
        this.subDirectory = subDirectory;
        this.serverProperties = platform.supportsServerProperties() ? new SingleServerProperties() : null;
    }

    public @NotNull SingleServerProperties getServerProperties() {
        if (serverProperties == null) {
            throw new UnsupportedOperationException("Platform of type " + platform + " does not support server.properties");
        }
        return serverProperties;
    }

    public void subDirectory(@NotNull String subDirectory) {
        this.subDirectory = subDirectory;
    }

    public void serverProperties(@NotNull Action<@NotNull ServerProperties> callback) {
        if (serverProperties == null) {
            throw new UnsupportedOperationException("Platform of type " + platform + " does not support server.properties");
        }
        callback.execute(serverProperties);
    }

    public void args(@NotNull String @NotNull ... args) {
        this.args = Arrays.asList(args);
    }

    public void args(@NotNull List<@NotNull String> args) {
        this.args = new ArrayList<>(args);
    }

    public void jvmArgs(@NotNull String @NotNull ... jvmArgs) {
        this.jvmArgs = Arrays.asList(jvmArgs);
    }

    public void jvmArgs(@NotNull List<@NotNull String> jvmArgs) {
        this.jvmArgs = new ArrayList<>(jvmArgs);
    }

    public @NotNull Platform getPlatform() {
        return this.platform;
    }

    public @NotNull String getVersion() {
        return this.version;
    }

    public @NotNull String getSubDirectory() {
        return this.subDirectory;
    }

    public @NotNull List<@NotNull String> getArgs() {
        return this.args;
    }

    public @NotNull List<@NotNull String> getJvmArgs() {
        return this.jvmArgs;
    }

    public void setSubDirectory(@NotNull String subDirectory) {
        this.subDirectory = subDirectory;
    }

    public void setArgs(@NotNull List<@NotNull String> args) {
        this.args = args;
    }

    public void setJvmArgs(@NotNull List<@NotNull String> jvmArgs) {
        this.jvmArgs = jvmArgs;
    }

    public @NotNull String toString() {
        return "Version(platform=" + this.platform
                + ", version=" + this.version
                + ", subDirectory=" + this.subDirectory
                + ", serverProperties=" + this.serverProperties
                + ", args=" + this.args
                + ", jvmArgs=" + this.jvmArgs + ")";
    }
}
