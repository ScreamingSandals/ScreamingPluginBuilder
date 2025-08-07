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

class Version(val platform: Platform, val version: String, var subDirectory: String) {
    internal val internalServerProperties: SingleServerProperties? = if (platform.supportsServerProperties) SingleServerProperties() else null

    val serverProperties: ServerProperties
        get() {
            if (internalServerProperties == null) {
                throw UnsupportedOperationException("Platform of type $platform does not support server.properties")
            }
            return internalServerProperties
        }

    var args: MutableList<String> = mutableListOf("nogui")
    var jvmArgs: MutableList<String> = mutableListOf()

    fun subDirectory(subDirectory: String) {
        this.subDirectory = subDirectory
    }

    fun serverProperties(callback: Action<ServerProperties>) {
        callback.execute(serverProperties)
    }

    fun args(vararg args: String) {
        this.args = mutableListOf(*args)
    }

    fun args(args: List<String>) {
        this.args = ArrayList(args)
    }

    fun jvmArgs(vararg jvmArgs: String) {
        this.jvmArgs = mutableListOf(*jvmArgs)
    }

    fun jvmArgs(jvmArgs: List<String>) {
        this.jvmArgs = ArrayList(jvmArgs)
    }

    override fun toString(): String {
        return ("Version(platform=" + this.platform
                + ", version=" + this.version
                + ", subDirectory=" + this.subDirectory
                + ", serverProperties=" + this.serverProperties
                + ", args=" + this.args
                + ", jvmArgs=" + this.jvmArgs + ")")
    }
}
