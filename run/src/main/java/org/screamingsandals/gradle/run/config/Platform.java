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

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.gradle.run.installer.Installer;
import org.screamingsandals.gradle.run.installer.BibliothekInstaller;

@RequiredArgsConstructor
@Getter
@Accessors(fluent = true)
public enum Platform {
    PAPER(true, true, "plugins", true) {
        @Override
        public @NotNull Installer obtainInstaller() {
            return new BibliothekInstaller("https://api.papermc.io", "paper");
        }
    },
    FOLIA(true, true, "plugins", true) {
        @Override
        public @NotNull Installer obtainInstaller() {
            return new BibliothekInstaller("https://api.papermc.io", "folia");
        }
    };

    private final boolean supportsServerProperties;
    private final boolean hasEula;
    private final @NotNull String pluginDirName;
    private final boolean supportsPluginAsParameter;

    public abstract @NotNull Installer obtainInstaller();
}
