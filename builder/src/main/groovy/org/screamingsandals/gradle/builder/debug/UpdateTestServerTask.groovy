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

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

class UpdateTestServerTask extends DefaultTask {
    @Internal
    String version
    @Internal
    String subDirectory

    def setVersion(String version) {
        this.version = version
    }

    def setSubDirectory(String subDirectory) {
        this.subDirectory = subDirectory
    }

    @TaskAction
    def run() {
        def testServerDirectory = project.file("test-environment/$subDirectory/$version")

        TestServerUtils.prepareServer(testServerDirectory, version, true)
    }
}
