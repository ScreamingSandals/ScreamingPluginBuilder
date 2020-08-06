package org.screamingsandals.gradle.builder

import com.github.jengelman.gradle.plugins.shadow.ShadowExtension
import io.franzbecker.gradle.lombok.LombokPlugin
import kr.entree.spigradle.data.BungeeDependencies
import kr.entree.spigradle.data.Dependency
import kr.entree.spigradle.data.Repositories
import kr.entree.spigradle.data.SpigotRepositories
import kr.entree.spigradle.data.VersionModifier
import kr.entree.spigradle.module.bungee.BungeePlugin
import kr.entree.spigradle.module.common.SpigradlePlugin
import kr.entree.spigradle.module.spigot.SpigotPlugin
import net.fabricmc.loom.LoomGradlePlugin
import net.fabricmc.loom.task.RemapJarTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.SelfResolvingDependency
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import io.franzbecker.gradle.lombok.task.DelombokTask

import com.github.jengelman.gradle.plugins.shadow.ShadowPlugin
import org.gradle.api.tasks.bundling.Jar

class BuilderPlugin implements Plugin<Project> {

    public static Dependency WATERFALL = new Dependency(
            "io.github.waterfallmc",
            "waterfall-api",
            BungeeDependencies.BUNGEE_CORD.getVersion(),
            BungeeDependencies.BUNGEE_CORD.getVersionModifier()
    )

    public static Dependency VELOCITY = new Dependency(
            "com.velocitypowered",
            "velocity-api",
            "1.1.0-SNAPSHOT",
            VersionModifier.SNAPSHOT_APPENDER
    )

    private Project project

    @Override
    void apply(Project project) {
        this.project = project

        project.apply {
            plugin ShadowPlugin.class
            plugin SpigradlePlugin.class
            plugin MavenPublishPlugin.class
            plugin LombokPlugin.class
        }

        project.repositories {
            jcenter()
            mavenCentral()
            //mavenLocal()

            maven { url Repositories.SONATYPE}
            maven { url SpigotRepositories.PAPER_MC}
            maven { url SpigotRepositories.SPIGOT_MC}
            maven { url 'https://repo.screamingsandals.org/repository/maven-public/' }
            maven { url 'https://repo.velocitypowered.com/snapshots/' }
            maven { url 'https://maven.fabricmc.net/' }
        }

        project.dependencies {
            compileOnly 'org.jetbrains:annotations:19.0.0'
        }

        project.dependencies.ext['waterfall'] = { String version = null ->
            WATERFALL.format(version)
        }

        project.dependencies.ext['velocity'] = { String version = null ->
            VELOCITY.format(version)
        }

        project.dependencies.ext['screaming'] = { String lib, String version ->
            return "org.screamingsandals.lib:$lib:$version"
        }

        project.configurations {
            def shade = it.maybeCreate("shade")
            project.configurations.runtimeClasspath.extendsFrom(shade)
            project.configurations.compileClasspath.extendsFrom(shade)
            project.configurations.runtimeElements.extendsFrom(shade)
        }

        project.shadowJar {
            configurations = [project.configurations.shade]
        }

        project.ext['prepareFabric'] = { String minecraftVersion, String mappingsVersion, String loaderVersion, String apiVersion = null ->
            project.apply {
                plugin LoomGradlePlugin
            }

            project.dependencies {
                minecraft "com.mojang:minecraft:${minecraftVersion}"
                mappings "net.fabricmc:yarn:${mappingsVersion}"
                modImplementation "net.fabricmc:fabric-loader:${loaderVersion}"

                if (apiVersion != null) {
                    modImplementation "net.fabricmc:fabric-api:${apiVersion}"
                }
            }

            project.processResources {
                inputs.property "version", project.version

                from(project.sourceSets.main.resources.srcDirs) {
                    include "fabric.mod.json"
                    expand "version": project.version
                }

                from(project.sourceSets.main.resources.srcDirs) {
                    exclude "fabric.mod.json"
                }
            }

            project.jar {
                from "LICENSE"
            }

            project.shadowJar {
                classifier = "dev"
            }

            project.tasks.register("remapShadowJar", RemapJarTask) {
                def shadowJar = project.tasks.getByName("shadowJar")
                dependsOn(shadowJar)
                input.set(shadowJar.archiveFile)
                archiveFileName.set(shadowJar.archiveFileName.get().replaceAll('-dev\\.jar$', '-all.jar'))
                addNestedDependencies.set(true)
                remapAccessWidener.set(true)
            }

            def remapShadowJar = project.tasks.getByName("remapShadowJar")
            project.tasks.getByName("screamCompile").dependsOn(remapShadowJar);
            project.tasks.getByName("publishToMavenLocal").dependsOn(remapShadowJar);
            if (project.hasProperty("nexus")) {
                project.tasks.getByName("publish").dependsOn(remapShadowJar);
            }
            if (project.hasProperty("screamingDocs")) {
                project.tasks.getByName("javadoc").dependsOn(remapShadowJar);
            }

        }

        project.ext['enableSpigradleSpigot'] = {
            project.apply {
                plugin SpigotPlugin.class
            }
        }

        project.ext['enableSpigradleBungee'] = {
            project.apply {
                plugin BungeePlugin.class
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

        if (project.hasProperty("nexus")) {
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
        }

        PublishingExtension publishing = project.extensions.getByName("publishing")
        publishing.publications.create("maven", MavenPublication) {
            ShadowExtension shadow = project.extensions.getByName("shadow")
            shadow.component(it)

            it.artifact(project.tasks.sourceJar)

            /*it.artifacts.every {
                it.classifier = ""
            }*/

            it.pom.withXml {
                if (asNode().get("dependencies") != null) {
                    asNode().remove(asNode().get("dependencies"))
                }

                def dependenciesNode = asNode().appendNode("dependencies")
                project.configurations.compileOnly.dependencies.each {
                    if (!(it instanceof SelfResolvingDependency) && it.name != "spigradle") {
                        def dependencyNode = dependenciesNode.appendNode('dependency')
                        dependencyNode.appendNode('groupId', it.group)
                        dependencyNode.appendNode('artifactId', it.name)
                        dependencyNode.appendNode('version', it.version)
                        dependencyNode.appendNode('scope', 'provided')
                    }
                }
            }
        }

        if (project.hasProperty("nexus")) {
            publishing.repositories {
                it.maven({ MavenArtifactRepository repository ->
                    if (((String) project.version).contains("SNAPSHOT")) {
                        repository.url = System.getenv("NEXUS_URL_SNAPSHOT")
                    } else {
                        repository.url = System.getenv("NEXUS_URL_RELEASE")
                    }
                    repository.credentials.username = System.getenv("NEXUS_USERNAME")
                    repository.credentials.password = System.getenv("NEXUS_PASSWORD")
                })
            }
        }

        List tasks = ["shadowJar", "publishToMavenLocal"]

        if (project.hasProperty("nexus")) {
            tasks.add("publish")
            tasks.add("javadoc")
        }

        project.tasks.create("screamCompile").dependsOn = tasks
    }

}