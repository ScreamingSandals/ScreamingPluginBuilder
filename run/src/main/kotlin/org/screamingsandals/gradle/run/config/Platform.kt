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

package org.screamingsandals.gradle.run.config

import org.screamingsandals.gradle.run.installer.FillInstaller
import org.screamingsandals.gradle.run.installer.BuildToolsInstaller
import org.screamingsandals.gradle.run.installer.Installer

enum class Platform(
    val supportsServerProperties: Boolean,
    val hasEula: Boolean,
    val pluginDirName: String,
    val supportsPluginAsParameter: Boolean
) {
    SPIGOT(true, true, "plugins", false) {
        override fun obtainInstaller(): Installer = BuildToolsInstaller("https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar")
    },
    PAPER(true, true, "plugins", true) {
        override fun obtainInstaller(): Installer = FillInstaller("https://fill.papermc.io", "paper")
    },
    FOLIA(true, true, "plugins", true) {
        override fun obtainInstaller(): Installer = FillInstaller("https://fill.papermc.io", "folia")
    };

    abstract fun obtainInstaller(): Installer
}
