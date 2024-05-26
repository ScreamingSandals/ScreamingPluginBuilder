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

package org.screamingsandals.gradle.builder.debug

import org.gradle.api.Project

import java.nio.file.Path

class TestTaskBuilder {
    private final Project project

    private List<String> versions
    private Path pluginJar
    private List<String> args = ['nogui']
    private List<String> jvmArgs = []
    private Map<String, String> serverProperties = [:]
    private String subdirectory = "paper"

    TestTaskBuilder(Project project) {
        this.project = project
    }

    def versions(String... versions) {
        this.versions = versions
        return this
    }

    def port(int port) {
        serverProperties['port'] = port as String
        return this
    }

    def onlineMode(boolean onlineMode) {
        serverProperties['online-mode'] = onlineMode as String
        return this
    }

    def pluginJar(Path pluginJar) {
        this.pluginJar = pluginJar
        return this
    }

    def args(String... args) {
        this.args = args
        return this
    }

    def jvmArgs(String... jvmArgs) {
        this.jvmArgs = jvmArgs
        return this
    }

    def addToServerProperties(String name, String value) {
        serverProperties[name] = value
        return this
    }

    def setSubdirectory(String subdirectory) {
        this.subdirectory = subdirectory
        return this
    }

    def build() {
        if (System.getenv("OPTIMIZE_FOR_CI_CD") != "1") {
            versions.each { version ->
                this.project.task("updatePaperServer$version", type: UpdateTestServerTask) {
                    it.version = version
                    it.subDirectory = this.subdirectory
                }
                this.project.task("runPaperServer$version", type: RunTestServerTask) {
                    args(this.args)
                    jvmArgs(this.jvmArgs)
                    it.version = version
                    it.pluginJar = this.pluginJar
                    it.properties = this.serverProperties
                    it.subDirectory = this.subdirectory
                }
            }
        }
    }
}
