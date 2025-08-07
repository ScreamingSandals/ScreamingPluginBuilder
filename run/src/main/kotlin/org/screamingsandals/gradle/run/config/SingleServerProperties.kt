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

import org.jetbrains.annotations.ApiStatus

@ApiStatus.Internal
class SingleServerProperties : ServerProperties {
    val serverProperties: MutableMap<String, String> = mutableMapOf()

    override fun property(key: String, value: String) {
        serverProperties[key] = value
    }

    override fun port(port: Int) {
        serverProperties["port"] = port.toString()
    }

    override fun onlineMode(onlineMode: Boolean) {
        serverProperties["online-mode"] = onlineMode.toString()
    }
}
