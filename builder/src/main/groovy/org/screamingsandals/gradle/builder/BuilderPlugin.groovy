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

package org.screamingsandals.gradle.builder

import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.JSch
import com.jcraft.jsch.SftpException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.api.tasks.bundling.Jar
import org.screamingsandals.gradle.builder.maven.NexusRepository
import org.screamingsandals.gradle.builder.webhook.DiscordWebhookExtension

class BuilderPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        def ciCdOptimized = System.getenv("OPTIMIZE_FOR_CI_CD") == "1"

        project.apply {
            plugin MavenPublishPlugin.class
            plugin JavaLibraryPlugin.class
        }

        Utilities.configureLombok(project)
        Utilities.configureLicenser(project)

        if (System.getenv("NEXUS_URL_SNAPSHOT") != null && System.getenv("NEXUS_URL_RELEASE") != null) {
            project.task('sourceJar', type: Jar) {
                it.archiveClassifier = 'sources'
                from project.sourceSets.main.allJava
            }
        }

        if (System.getenv('JAVADOC_HOST') != null) {
            project.javadoc {
                options.addBooleanOption('html5', true)
            }

            project.task('javadocJar', type: Jar, dependsOn: project.javadoc) {
                it.archiveClassifier = 'javadoc'
                from project.javadoc
            }

            if (System.getenv('JAVADOC_HOST') != null && System.getenv('JAVADOC_USER') != null && System.getenv('JAVADOC_SECRET') != null) {
                project.tasks.register("uploadJavadoc") {
                    doLast {
                        try {
                            def projectJavadocDirectories
                            def custom = System.getProperty("JavadocUploadCustomDirectoryPath")
                            if (custom != null && !custom.isEmpty()) {
                                if (project.getRootProject() == project) {
                                    projectJavadocDirectories = [custom]
                                } else {
                                    projectJavadocDirectories = [custom, project.getName()]
                                }
                            } else {
                                if (project.getRootProject() == project) {
                                    projectJavadocDirectories = [project.getName()]
                                } else {
                                    projectJavadocDirectories = [project.getRootProject().getName(), project.getName()]
                                }
                            }

                            def jsch = new JSch()
                            def jschSession = jsch.getSession(System.getenv('JAVADOC_USER'), System.getenv('JAVADOC_HOST'))
                            jschSession.setConfig("StrictHostKeyChecking", "no");
                            jschSession.setPassword(System.getenv('JAVADOC_SECRET'))
                            jschSession.connect()
                            def sftpChannel = jschSession.openChannel("sftp") as ChannelSftp
                            sftpChannel.connect()

                            sftpChannel.cd("www")

                            projectJavadocDirectories.forEach {
                                try {
                                    sftpChannel.cd(it)
                                } catch (SftpException ignored) {
                                    sftpChannel.mkdir(it)
                                    sftpChannel.cd(it)
                                }
                            }

                            recursiveClear(sftpChannel)

                            recursiveFolderUpload(sftpChannel, project.file('build/docs/javadoc'))

                            sftpChannel.disconnect()

                            jschSession.disconnect()
                        } catch (SftpException exception) {
                            exception.printStackTrace()
                        }
                    }
                    dependsOn('javadoc')
                }
            }
        }

        PublishingExtension publishing = project.extensions.getByName("publishing")
        def maven = Utilities.setupPublishing(project, false, System.getenv("NEXUS_URL_SNAPSHOT") != null && System.getenv("NEXUS_URL_RELEASE") != null, false).publication

        project.ext['enableShadowPlugin'] = {
            Utilities.enableShadowPlugin(project, maven)
        }

        List tasks = ["build"]

        if (!project.hasProperty('disablePublishingToMaven') || !project.property('disablePublishingToMaven')) {
            if (System.getenv("NEXUS_URL_SNAPSHOT") != null && System.getenv("NEXUS_URL_RELEASE") != null) {
                new NexusRepository().setup(project, publishing)
            }

            if (!ciCdOptimized) {
                tasks.add("publishToMavenLocal")
            }

            if (System.getenv("NEXUS_URL_SNAPSHOT") != null && System.getenv("NEXUS_URL_RELEASE") != null) {
                tasks.add("publish")
            }
        }

        project.getExtensions().create("discord", DiscordWebhookExtension)

        project.tasks.create("screamCompile").dependsOn = tasks

        project.tasks.create("allowJavadocUpload") {
            if (project.tasks.findByName("uploadJavadoc") != null) {
                dependsOn("uploadJavadoc")
            }
        }
    }

    def static recursiveClear(ChannelSftp sftpChannel) {
        sftpChannel.ls(".").forEach {
            if (it.filename == "." || it.filename == "..")
                return
            if (it.attrs.dir) {
                sftpChannel.cd(it.filename)
                recursiveClear(sftpChannel)
                sftpChannel.cd("..")
                sftpChannel.rmdir(it.filename)
            } else {
                sftpChannel.rm(it.filename)
            }
        }
    }

    def static recursiveFolderUpload(ChannelSftp channelSftp, File sourceFolder) {
        sourceFolder.listFiles().each {
            if (it.isDirectory()) {
                channelSftp.mkdir(it.getName())
                channelSftp.cd(it.getName())
                recursiveFolderUpload(channelSftp, it)
                channelSftp.cd("..")
            } else {
                channelSftp.put(it.getAbsolutePath(), it.getName())
            }
        }
    }

}