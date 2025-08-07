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

package org.screamingsandals.gradle.run.task

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.screamingsandals.gradle.run.config.Platform
import java.io.File

abstract class UpdateVersionTask : DefaultTask() {
    @get:Input
    abstract val platform: Property<Platform>

    @get:Input
    abstract val version: Property<String>

    @get:Input
    abstract val directory: Property<String>

    init {
        group = TASK_GROUP
    }

    @TaskAction
    fun run() {
        val testServerDirectory = File(this.directory.get())

        try {
            this.platform.get().obtainInstaller().install(this.version.get(), testServerDirectory, true)
        } catch (e: Exception) {
            throw RuntimeException(
                "Unable to update server " + this.platform.get() + " version " + this.version.get(),
                e
            )
        }
    }
}
