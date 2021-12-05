package org.screamingsandals.gradle.builder.webhook

import groovy.json.JsonSlurper
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
                if (System.getenv('REPOSILITE_BASE_URL')) {
                    var jsonSlurper = new JsonSlurper()
                    var shortRepositoryName = this.storage.repository.url.toString()
                    if (shortRepositoryName.endsWith('/')) {
                        shortRepositoryName = shortRepositoryName.substring(0, shortRepositoryName.length() - 1)
                    }
                    var split = shortRepositoryName.split("/")
                    shortRepositoryName = split[split.length - 1]
                    var result = jsonSlurper.parse("${System.getenv('REPOSILITE_BASE_URL')}/api/maven/details/${shortRepositoryName}/${this.storage.publication.groupId.replace('.', '/')}/${this.storage.publication.artifactId}/${this.storage.publication.version}".toURL())
                    var startingString = "${this.storage.publication.artifactId}-${this.storage.publication.version.replace('SNAPSHOT', snapshotReplace)}-"
                    var arti = it
                    var r = (result.files as List).find {
                        var map = it as Map
                        if (map.get("contentType") == "APPLICATION_JAR") {
                            return (map.get("name") as String).startsWith(startingString) && (map.get("name") as String).substring(startingString.length()).matches("\\d+${arti.classifier ? '\\-' + arti.classifier : ''}\\.${arti.extension}")
                        }
                        return false
                    }
                    if (r != null) {
                        uploadedUrl = baseUrl + r.get("name")
                    }
                } else if (System.getenv('NEXUS_BASE_URL')) {
                    var jsonSlurper = new JsonSlurper()
                    var result = jsonSlurper.parse("${System.getenv('NEXUS_BASE_URL')}service/rest/v1/search/assets?sort=&direction=desc&q=${snapshotReplace}&maven.groupId=${this.storage.publication.groupId}&maven.artifactId=${this.storage.publication.artifactId}&maven.baseVersion=${this.storage.publication.version}&maven.extension=${it.extension}".toURL())
                    var arti = it
                    var r = (result.items as List).find {
                        var map = (it.maven2 as Map)
                        if (map.containsKey('classifier')) {
                            return arti.classifier == map.classifier
                        } else if (!arti.classifier) {
                            return true
                        }
                        return false
                    }
                    if (r != null) {
                        uploadedUrl = r.downloadUrl
                    }
                }
                fieldValue += "[${realname}](${uploadedUrl})\\n"
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
