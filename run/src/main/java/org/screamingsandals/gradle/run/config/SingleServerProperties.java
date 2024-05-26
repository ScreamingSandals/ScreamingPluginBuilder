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
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

@Getter
@ApiStatus.Internal
public class SingleServerProperties implements ServerProperties {
    private final @NotNull Map<@NotNull String, String> serverProperties = new HashMap<>();

    public void property(@NotNull String key, @NotNull String value) {
        serverProperties.put(key, value);
    }

    public void port(int port) {
        serverProperties.put("port", Integer.toString(port));
    }

    public void onlineMode(boolean onlineMode) {
        serverProperties.put("online-mode", Boolean.toString(onlineMode));
    }
}
