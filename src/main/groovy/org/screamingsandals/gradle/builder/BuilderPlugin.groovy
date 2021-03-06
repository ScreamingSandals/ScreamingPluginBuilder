package org.screamingsandals.gradle.builder

import com.github.jengelman.gradle.plugins.shadow.ShadowPlugin
import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.JSch
import com.jcraft.jsch.SftpException
import io.franzbecker.gradle.lombok.LombokPlugin
import io.franzbecker.gradle.lombok.task.DelombokTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.SelfResolvingDependency
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.credentials.HttpHeaderCredentials
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.api.tasks.bundling.Jar
import org.gradle.authentication.http.HttpHeaderAuthentication
import org.screamingsandals.gradle.builder.dependencies.Dependencies
import org.screamingsandals.gradle.builder.repositories.Repositories
import org.screamingsandals.gradle.builder.utils.ScreamingLibBuilder

class BuilderPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.apply {
            plugin MavenPublishPlugin.class
            plugin LombokPlugin.class
            plugin JavaLibraryPlugin.class
        }

        Repositories.registerRepositoriesMethods(project)
        Dependencies.registerDependenciesMethods(project)

        project.repositories {
            jcenter()
            mavenCentral()
            //mavenLocal()

            screaming()
            sonatype()
            papermc()
            spigotmc()
            velocity()
            placeholderApi()
        }

        project.dependencies {
            compileOnly 'org.jetbrains:annotations:20.1.0'
        }

        project.dependencies.ext['screaming'] = { String lib, String version ->
            return "org.screamingsandals.lib:$lib:$version"
        }

        project.ext['ScreamingLibBuilder'] = {
            return new ScreamingLibBuilder(project)
        }

        if (System.getenv("GITLAB_REPO") != null || System.getenv('JAVADOC_HOST') != null) {
            def srcmain = project.file("src/main");
            def processDelombok = srcmain.exists() && srcmain.listFiles().length > 0
            if (processDelombok) {
                project.task('delombokForJavadoc', type: DelombokTask, dependsOn: 'compileJava') {
                    ext.outputDir = project.file("$project.buildDir/delombok")
                    outputs.dir(outputDir)
                    project.sourceSets.main.java.srcDirs.each {
                        inputs.dir(it)
                        args(it, "-d", outputDir)
                    }
                    doFirst {
                        outputDir.deleteDir()
                    }
                }
            }

            project.task('sourceJar', type: Jar) {
                it.classifier 'sources'
                from project.sourceSets.main.allJava
            }

            project.javadoc {
                if (processDelombok) {
                    dependsOn 'delombokForJavadoc'
                    source = project.tasks.getByName('delombokForJavadoc').outputDir
                }
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
                            if (project.getRootProject() == project) {
                                projectJavadocDirectories = [project.getName()]
                            } else {
                                projectJavadocDirectories = [project.getRootProject().getName(), project.getName()]
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


            if (System.getenv("GITLAB_REPO") != null) {
                it.artifact(project.tasks.sourceJar)
                it.artifact(project.tasks.javadocJar)
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
            publishing.repositories {
                it.maven { MavenArtifactRepository repository ->
                    repository.url System.getenv("GITLAB_REPO")
                    repository.name "GitLab"
                    repository.credentials(HttpHeaderCredentials) {
                        name = 'Private-Token'
                        value = System.getenv("GITLAB_TOKEN")
                    }
                    repository.authentication {
                        header(HttpHeaderAuthentication)
                    }
                }
            }
        }

        List tasks = ["build", "publishToMavenLocal"]

        if (System.getenv("GITLAB_REPO") != null) {
            tasks.add("publish")
        }

        if (System.getenv("GITLAB_REPO") != null) {
            tasks.add("javadoc")
        }

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