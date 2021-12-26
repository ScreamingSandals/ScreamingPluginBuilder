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
