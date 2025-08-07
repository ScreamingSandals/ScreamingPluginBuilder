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

import org.gradle.api.Action
import java.util.Collections

class MultipleVersions(private val versions: MutableList<Version>) : Iterable<Version> {
    fun args(vararg args: String) {
        for (version in versions) {
            version.args(*args)
        }
    }

    fun args(args: List<String>) {
        for (version in versions) {
            version.args(args)
        }
    }

    fun jvmArgs(vararg jvmArgs: String) {
        for (version in versions) {
            version.jvmArgs(*jvmArgs)
        }
    }

    fun jvmArgs(jvmArgs: List<String>) {
        for (version in versions) {
            version.jvmArgs(jvmArgs)
        }
    }

    fun serverProperties(callback: Action<ServerProperties>) {
        for (version in versions) {
            if (!version.platform.supportsServerProperties) {
                throw UnsupportedOperationException("Platform of type " + version.platform + " does not support server.properties")
            }
        }
        callback.execute(MultipleServerProperties(versions))
    }

    override fun iterator(): MutableIterator<Version> {
        return Collections.unmodifiableCollection(versions).iterator()
    }
}
