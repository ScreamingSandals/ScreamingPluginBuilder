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
package org.screamingsandals.gradle.slib

import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.provider.Provider
import org.jetbrains.annotations.ApiStatus

class ThirdPartyModule : AdditionalContent {
    /**
     * Group id of the third party module.
     */
    lateinit var groupId: String

    /**
     * Prefix of the artifact id of the third party module.
     *
     * The final artifact id looks like `module-platform` or `module-common`
     */
    lateinit var module: String

    /**
     * Version of the third party module.
     */
    lateinit var version: String

    fun setVersion(version: Provider<String>) {
        this.version = version.get()
    }

    /**
     * Group id of the third party module.
     *
     * @param groupId new group id
     */
    fun groupId(groupId: String) {
        this.groupId = groupId
    }

    /**
     * Prefix of the artifact id of the third party module.
     *
     * The final artifact id looks like `module-platform` or `module-common`
     *
     * @param module new artifact id prefix
     */
    fun module(module: String) {
        this.module = module
    }

    /**
     * Version of the third party module.
     *
     * @param version new version
     */
    fun version(version: String) {
        this.version = version
    }

    /**
     * Version of the third party module.
     *
     * @param version new version
     */
    fun version(version: Provider<String>) {
        this.version = version.get()
    }

    @ApiStatus.Internal
    override fun apply(
        configuration: String,
        dependencies: DependencyHandler,
        slibVersion: String,
        platforms: List<String>,
    ) {
        val dependency = dependencies.add(configuration, "$groupId:$module-common:$version")
        if (dependency is ModuleDependency) {
            dependency.exclude(mapOf("group" to SCREAMING_LIB_GROUP_ID))
        }
        platforms.forEach { s ->
            val dependency = dependencies.add(IMPLEMENTATION_CONFIGURATION, "$groupId:$module-$s:$version")
            if (dependency is ModuleDependency) {
                dependency.exclude(mapOf("group" to SCREAMING_LIB_GROUP_ID))
            }
        }
    }

    @ApiStatus.Internal
    override fun applyMultiModule(
        configuration: String,
        dependencies: DependencyHandler,
        slibVersion: String,
        platformName: String,
    ) {
        val dependency = dependencies.add(configuration, "$groupId:$module-$platformName:$version")
        if (dependency is ModuleDependency) {
            dependency.exclude(mapOf("group" to SCREAMING_LIB_GROUP_ID))
        }
    }

    override fun toString(): String {
        return "ThirdPartyModule(groupId=" + this.groupId + ", module=" + this.module + ", version=" + this.version + ")"
    }
}
