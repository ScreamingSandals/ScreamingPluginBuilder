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

package org.screamingsandals.gradle.run

import org.gradle.api.Action
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.jetbrains.annotations.ApiStatus
import org.screamingsandals.gradle.run.config.MultipleVersions
import org.screamingsandals.gradle.run.config.Platform
import org.screamingsandals.gradle.run.config.Version
import java.io.File
import javax.inject.Inject

open class RunTestServerExtension @Inject constructor(private val providers: ProviderFactory) {
    var versions: MutableList<Version> = mutableListOf()
    var pluginJar: Provider<RegularFile>? = null
    var testingDirectory: String = "test-environment"

    fun version(version: Version): Version {
        this.versions.add(version)
        return version
    }

    fun version(version: Version, callback: Action<Version>): Version {
        this.versions.add(version)
        callback.execute(version)
        return version
    }

    fun version(platform: Platform, version: String): Version {
        return version(Version(platform, version, "${platform.name.lowercase()}/$version"))
    }

    fun version(platform: Platform, version: String, callback: Action<Version>): Version {
        val server = version(platform, version)
        callback.execute(server)
        return server
    }

    fun versions(platform: Platform, vararg versions: String): MultipleVersions {
        val list = mutableListOf<Version>()
        for (version in versions) {
            list.add(version(platform, version))
        }
        return MultipleVersions(list)
    }

    fun versions(platform: Platform, versions: Collection<String>): MultipleVersions {
        val list = mutableListOf<Version>()
        for (version in versions) {
            list.add(version(platform, version))
        }
        return MultipleVersions(list)
    }

    fun versions(
        platform: Platform,
        versions: Collection<String>,
        callback: Action<MultipleVersions>
    ): MultipleVersions {
        val multipleVersions = versions(platform, versions)
        callback.execute(multipleVersions)
        return multipleVersions
    }

    fun paper(version: String): Version {
        return version(Platform.PAPER, version)
    }

    fun paper(version: String, callback: Action<Version>): Version {
        return version(Platform.PAPER, version, callback)
    }

    fun paperVersions(vararg versions: String): MultipleVersions {
        return versions(Platform.PAPER, *versions)
    }

    fun paperVersions(versions: Collection<String>): MultipleVersions {
        return versions(Platform.PAPER, versions)
    }

    fun paperVersions(versions: Collection<String>, callback: Action<MultipleVersions>): MultipleVersions {
        return versions(Platform.PAPER, versions, callback)
    }

    fun pluginJar(pluginJar: Provider<RegularFile>) {
        this.pluginJar = pluginJar
    }

    @ApiStatus.Obsolete
    fun pluginJar(pluginJar: File) {
        this.pluginJar = providers.provider { RegularFile { pluginJar } }
    }

    fun testingDirectory(testingDirectory: String) {
        this.testingDirectory = testingDirectory
    }

    override fun toString(): String {
        return ("RunTestServerExtension(providers=" + this.providers
                + ", versions=" + this.versions
                + ", pluginJar=" + this.pluginJar
                + ", testingDirectory=" + this.testingDirectory + ")")
    }
}
