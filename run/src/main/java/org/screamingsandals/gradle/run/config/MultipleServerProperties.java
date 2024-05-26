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

import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Data
public class MultipleServerProperties implements ServerProperties {
    private final @NotNull List<@NotNull Version> versions;

    public void property(@NotNull String key, @NotNull String value) {
        for (var version : versions) {
            version.getServerProperties().property(key, value);
        }
    }

    public void port(int port) {
        for (var version : versions) {
            version.getServerProperties().port(port);
        }
    }

    public void onlineMode(boolean onlineMode) {
        for (var version : versions) {
            version.getServerProperties().onlineMode(onlineMode);
        }
    }
}
