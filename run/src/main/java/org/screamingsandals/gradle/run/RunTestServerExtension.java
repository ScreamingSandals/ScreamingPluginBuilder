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

package org.screamingsandals.gradle.run;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.gradle.api.Action;
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.screamingsandals.gradle.run.config.MultipleVersions;
import org.screamingsandals.gradle.run.config.Platform;
import org.screamingsandals.gradle.run.config.Version;

import javax.inject.Inject;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Data
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class RunTestServerExtension {
    @Getter(AccessLevel.NONE)
    private final @NotNull ProviderFactory providers;
    private @NotNull List<@NotNull Version> versions = new ArrayList<>();
    private @Nullable Provider<RegularFile> pluginJar;
    private @NotNull String testingDirectory = "test-environment";

    public @NotNull Version version(@NotNull Version version) {
        this.versions.add(version);
        return version;
    }

    public @NotNull Version version(@NotNull Version version, @NotNull Action<@NotNull Version> callback) {
        this.versions.add(version);
        callback.execute(version);
        return version;
    }

    public @NotNull Version version(@NotNull Platform platform, @NotNull String version) {
        return version(new Version(platform, version, platform.name().toLowerCase(Locale.ROOT) + "/" + version));
    }

    public @NotNull Version version(@NotNull Platform platform, @NotNull String version, @NotNull Action<@NotNull Version> callback) {
        var server = version(platform, version);
        callback.execute(server);
        return server;
    }

    public @NotNull MultipleVersions versions(@NotNull Platform platform, @NotNull String @NotNull... versions) {
        var list = new ArrayList<Version>();
        for (var version : versions) {
            list.add(version(platform, version));
        }
        return new MultipleVersions(list);
    }

    public @NotNull MultipleVersions versions(@NotNull Platform platform, @NotNull List<@NotNull String> versions) {
        var list = new ArrayList<Version>();
        for (var version : versions) {
            list.add(version(platform, version));
        }
        return new MultipleVersions(list);
    }

    public @NotNull MultipleVersions versions(@NotNull Platform platform, @NotNull List<@NotNull String> versions, @NotNull Action<@NotNull MultipleVersions> callback) {
        var multipleVersions = versions(platform, versions);
        callback.execute(multipleVersions);
        return multipleVersions;
    }

    public @NotNull Version paper(@NotNull String version) {
        return version(Platform.PAPER, version);
    }

    public @NotNull Version paper(@NotNull String version, @NotNull Action<@NotNull Version> callback) {
        return version(Platform.PAPER, version, callback);
    }

    public @NotNull MultipleVersions paperVersions(@NotNull String @NotNull... versions) {
        return versions(Platform.PAPER, versions);
    }

    public @NotNull MultipleVersions paperVersions(@NotNull List<@NotNull String> versions) {
        return versions(Platform.PAPER, versions);
    }

    public @NotNull MultipleVersions paperVersions(@NotNull List<@NotNull String> versions, @NotNull Action<@NotNull MultipleVersions> callback) {
        return versions(Platform.PAPER, versions, callback);
    }

    public void pluginJar(@NotNull Provider<RegularFile> pluginJar) {
        this.pluginJar = pluginJar;
    }

    @ApiStatus.Obsolete
    public void pluginJar(@NotNull File pluginJar) {
        this.pluginJar = providers.provider(() -> () -> pluginJar);
    }

    public void testingDirectory(@NotNull String testingDirectory) {
        this.testingDirectory = testingDirectory;
    }
}
