package org.screamingsandals.gradle.builder.webhook

import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.tasks.AbstractPublishToMaven
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

import java.text.SimpleDateFormat

class DiscordWebhookTask extends DefaultTask {

    // gradle can sometimes be pain
    @Internal
    private final InternalStorage storage = new InternalStorage()

    InternalStorage getStorage() {
        return this.storage
    }

    class InternalStorage {
        private String snapshotTime
        private MavenPublication publication
        private MavenArtifactRepository repository
    }

    {
        dependsOn("publish")

        getProject().tasks.withType(PublishToMavenRepository).all { task ->
            task.doLast {
                this.storage.publication = task.publication
                var m = AbstractPublishToMaven.class.getDeclaredMethod("getMavenPublishers")
                m.setAccessible(true)
                var publishers = m.invoke(task)
                var f = publishers.getClass().getDeclaredField("timeProvider")
                f.setAccessible(true)
                var timeProvider = f.get(publishers)
                var utcDateFormatter = new SimpleDateFormat("yyyyMMdd.HHmmss")
                utcDateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"))
                this.storage.snapshotTime = utcDateFormatter.format(new Date(timeProvider.getCurrentTime()))
                this.storage.repository = task.repository
            }
        }
    }

    @TaskAction
    void run() {
        def extension = getProject().getExtensions().getByType(DiscordWebhookExtension)

        String baseUrl = ""
        if (this.storage.repository != null) {
            baseUrl += this.storage.repository.url.toString() + '/'
        }
        baseUrl += this.storage.publication.groupId.replace('.', '/') + '/'
        baseUrl += this.storage.publication.artifactId + '/' + this.storage.publication.version + '/'
        var snapshotReplace = 'SNAPSHOT'
        if (this.storage.publication.version.contains('SNAPSHOT')) {
            snapshotReplace = this.storage.snapshotTime
        }

        var webhook = new DiscordWebhook(extension.url)

        var embed = new DiscordWebhook.EmbedObject()
                .setTitle(extension.title)
                .setDescription(extension.content)

        if (extension.buildInformationUrl) {
            embed.setUrl(extension.buildInformationUrl)
        }

        var fieldValue = ""

        this.storage.publication.artifacts.each {
            if (extension.allowedClassifiersAndExtensions.contains((it.classifier?:'') + '.' + it.extension)) {
                var realname = it.file.getName()
                var uploadedUrl = baseUrl + realname.replace('SNAPSHOT', snapshotReplace)
                fieldValue += "[${realname}](${uploadedUrl}) "
            }
        }

        if (!fieldValue.isEmpty()) {
            embed.addField("Artifacts", fieldValue, false)
        }

        embed.setFooter("Screaming Plugin Builder", null)
        var tz = TimeZone.getTimeZone("UTC")
        var df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        df.setTimeZone(tz)
        embed.setTimestamp(df.format(new Date()))

        webhook.addEmbed(embed)

        webhook.execute()
    }
}
