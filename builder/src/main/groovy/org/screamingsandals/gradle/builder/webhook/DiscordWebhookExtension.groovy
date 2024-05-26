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

package org.screamingsandals.gradle.builder.webhook

import org.gradle.api.Project

class DiscordWebhookExtension {
    String url
    String title
    String content
    String buildInformationUrl
    List<String> allowedClassifiersAndExtensions = []

    static def registerTask(Project project) {
        if (System.getenv("OPTIMIZE_FOR_CI_CD") == "1") {
            project.tasks.create("discord", DiscordWebhookTask)
            project.tasks.getByName("screamCompile").dependsOn += "discord"
        }
    }
}
