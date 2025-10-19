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

import org.gradle.api.artifacts.dsl.DependencyHandler
import org.jetbrains.annotations.ApiStatus

class SLibSingleModule(
    /**
     * Artifact id of the optional internal module
     */
    val artifactId: String,
) : AdditionalContent {
    @ApiStatus.Internal
    override fun apply(
        configuration: String,
        dependencies: DependencyHandler,
        slibVersion: String,
        platforms: List<String>,
    ) {
        dependencies.add(configuration, "${SCREAMING_LIB_GROUP_ID}:$artifactId:$slibVersion")
    }

    @ApiStatus.Internal
    override fun applyMultiModule(
        configuration: String,
        dependencies: DependencyHandler,
        slibVersion: String,
        platformName: String,
    ) {
        if (platformName == "common") {
            dependencies.add(configuration, "${SCREAMING_LIB_GROUP_ID}:$artifactId:$slibVersion")
        }
    }

    override fun toString(): String {
        return "SLibSingleModule(artifactId=" + this.artifactId + ")"
    }
}
