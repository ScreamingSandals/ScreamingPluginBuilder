package org.screamingsandals.gradle.builder

import com.github.jengelman.gradle.plugins.shadow.ShadowPlugin
import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.JSch
import com.jcraft.jsch.SftpException
import org.cadixdev.gradle.licenser.Licenser
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.SelfResolvingDependency
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.api.tasks.bundling.Jar
import org.screamingsandals.gradle.builder.debug.TestTaskBuilder
import org.screamingsandals.gradle.builder.dependencies.Dependencies
import org.screamingsandals.gradle.builder.maven.GitlabRepository
import org.screamingsandals.gradle.builder.maven.NexusRepository
import org.screamingsandals.gradle.builder.repositories.Repositories
import org.screamingsandals.gradle.builder.utils.ScreamingLibBuilder
import org.screamingsandals.gradle.builder.webhook.DiscordWebhookExtension
import io.freefair.gradle.plugins.lombok.LombokPlugin

class BuilderPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        def ciCdOptimized = System.getenv("OPTIMIZE_FOR_CI_CD") == "1"

        project.apply {
            plugin MavenPublishPlugin.class
            plugin LombokPlugin.class
            plugin JavaLibraryPlugin.class
        }

        Repositories.registerRepositoriesMethods(project)
        Dependencies.registerDependenciesMethods(project)

        project.repositories {
            mavenCentral()
            //mavenLocal()

            screaming()
            sonatype()
            paper()
            spigotmc()
        }

        project.dependencies {
            compileOnly 'org.jetbrains:annotations:21.0.1'
        }

        project.dependencies.ext['screaming'] = { String lib, String version ->
            return "org.screamingsandals.lib:$lib:$version"
        }

        project.ext['ScreamingLibBuilder'] = {
            return new ScreamingLibBuilder(project)
        }

        project.ext['prepareTestTask'] = {
            return new TestTaskBuilder(project)
        }

        var headerFile = project.getRootProject().file("license_header.txt")

        if (headerFile.exists()) {
            project.apply {
                plugin Licenser.class
            }

            project.license {
                header = headerFile
                ignoreFailures = true
                properties {
                    year = Calendar.getInstance().get(Calendar.YEAR)
                }
            }
        }

        project.getExtensions().create("discord", DiscordWebhookExtension)

        project.tasks.create("screamCompile").dependsOn = tasks

        project.tasks.create("allowJavadocUpload") {
            if (project.tasks.findByName("uploadJavadoc") != null) {
                dependsOn("uploadJavadoc")
            }
        }

        project.afterEvaluate {
            if (System.getenv("GITLAB_REPO") != null || (System.getenv("NEXUS_URL_SNAPSHOT") != null && System.getenv("NEXUS_URL_RELEASE") != null)) {
                project.task('sourceJar', type: Jar) {
                    it.classifier 'sources'
                    from project.sourceSets.main.allJava
                }
            }

            if (System.getenv('JAVADOC_HOST') != null) {
                def srcmain = project.file("src/main");

                project.javadoc {
                    options.addBooleanOption('html5', true)
                }

                project.task('javadocJar', type: Jar, dependsOn: project.javadoc) {
                    it.classifier = 'javadoc'
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
            def maven = publishing.publications.create("maven", MavenPublication) {
                it.artifact(project.tasks.jar)

                it.artifacts.every {
                    it.classifier = ""
                }

                if (System.getenv("GITLAB_REPO") != null || (System.getenv("NEXUS_URL_SNAPSHOT") != null && System.getenv("NEXUS_URL_RELEASE") != null)) {
                    it.artifact(project.tasks.sourceJar)
                }

                it.pom.withXml {
                    def dependenciesNode = asNode().appendNode("dependencies")
                    project.configurations.compileOnly.dependencies.each {
                        if (!(it instanceof SelfResolvingDependency)) {
                            def dependencyNode = dependenciesNode.appendNode('dependency')
                            dependencyNode.appendNode('groupId', it.group)
                            dependencyNode.appendNode('artifactId', it.name)
                            dependencyNode.appendNode('version', it.version)
                            dependencyNode.appendNode('scope', 'provided')
                        }
                    }
                    if (!project.tasks.findByName("shadowJar")) {
                        project.configurations.api.dependencies.each {
                            def dependencyNode = dependenciesNode.appendNode('dependency')
                            dependencyNode.appendNode('groupId', it.group)
                            dependencyNode.appendNode('artifactId', it.name)
                            dependencyNode.appendNode('version', it.version)
                            dependencyNode.appendNode('scope', 'compile')
                        }
                    }
                }
            }

            project.ext['enableShadowPlugin'] = {
                project.apply {
                    plugin ShadowPlugin.class
                }

                project.tasks.getByName("screamCompile").dependsOn -= "build"
                project.tasks.getByName("screamCompile").dependsOn += "shadowJar"

                maven.each {
                    it.getArtifacts().removeIf {
                        it.buildDependencies.getDependencies().contains(project.tasks.jar)
                    }
                    it.artifact(project.tasks.shadowJar) {
                        it.classifier = ""
                    }
                }
            }

            if (System.getenv("GITLAB_REPO") != null) {
                new GitlabRepository().setup(project, publishing)
            }

            if (System.getenv("NEXUS_URL_SNAPSHOT") != null && System.getenv("NEXUS_URL_RELEASE") != null) {
                new NexusRepository().setup(project, publishing)
            }

            List tasks = ["build"]

            if (!ciCdOptimized) {
                tasks.add("publishToMavenLocal")
            }

            if (System.getenv("GITLAB_REPO") != null || (System.getenv("NEXUS_URL_SNAPSHOT") != null && System.getenv("NEXUS_URL_RELEASE") != null)) {
                tasks.add("publish")
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