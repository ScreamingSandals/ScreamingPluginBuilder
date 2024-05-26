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

package org.screamingsandals.gradle.slib;

import lombok.Data;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Data
public class SLibModule implements AdditionalContent {
    /**
     * Name of the optional internal module
     */
    @NotNull
    private final String name;

    @Override
    @ApiStatus.Internal
    @ApiStatus.OverrideOnly
    public void apply(String configuration, DependencyHandler dependencies, String slibVersion, List<String> platforms) {
        dependencies.add(configuration, Constants.SCREAMING_LIB_GROUP_ID + ":" + name + "-common:" + slibVersion);
        platforms.forEach(s ->
                dependencies.add(configuration, Constants.SCREAMING_LIB_GROUP_ID + ":" + name + "-" + s + ":" + slibVersion)
        );
    }

    @Override
    public void applyMultiModule(String configuration, DependencyHandler dependencies, String slibVersion, String platformName) {
        dependencies.add(configuration, Constants.SCREAMING_LIB_GROUP_ID + ":" + name + "-" + platformName + ":" + slibVersion);
    }
}
