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

class MultipleServerProperties(private val versions: MutableList<Version>) : ServerProperties {
    override fun property(key: String, value: String) {
        for (version in versions) {
            version.serverProperties.property(key, value)
        }
    }

    override fun port(port: Int) {
        for (version in versions) {
            version.serverProperties.port(port)
        }
    }

    override fun onlineMode(onlineMode: Boolean) {
        for (version in versions) {
            version.serverProperties.onlineMode(onlineMode)
        }
    }

    override fun toString(): String {
        return "MultipleServerProperties(versions=" + this.versions + ")"
    }
}
