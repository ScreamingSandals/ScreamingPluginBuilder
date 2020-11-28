package org.screamingsandals.gradle.builder

import com.github.jengelman.gradle.plugins.shadow.ShadowExtension
import com.github.jengelman.gradle.plugins.shadow.ShadowPlugin
import io.franzbecker.gradle.lombok.LombokPlugin
import io.franzbecker.gradle.lombok.task.DelombokTask
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
import org.gradle.api.credentials.HttpHeaderCredentials
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.api.tasks.bundling.Jar
import org.gradle.authentication.http.HttpHeaderAuthentication

class BuilderPlugin implements Plugin<Project> {

    public static Dependency WATERFALL = new Dependency(
            "io.github.waterfallmc",
            "waterfall-api",
            "1.16-R0.4-SNAPSHOT",
            false,
            VersionModifier.INSTANCE.createAdjuster("R0.4", "SNAPSHOT")
    )

    public static Dependency VELOCITY = new Dependency(
            "com.velocitypowered",
            "velocity-api",
            "1.1.2-SNAPSHOT",
            false,
            VersionModifier.INSTANCE.getSNAPSHOT_APPENDER()
    )

    public static Dependency PAPERLIB = new Dependency(
            "io.papermc",
            "paperlib",
            "1.0.6-SNAPSHOT",
            false,
            VersionModifier.INSTANCE.getSNAPSHOT_APPENDER()
    )

    public static Dependency PLACEHOLDERAPI = new Dependency(
            "me.clip",
            "placeholderapi",
            "2.10.9",
            false,
            VersionModifier.INSTANCE.createAdjuster("")
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

            maven { url Repositories.SONATYPE }
            maven { url SpigotRepositories.PAPER_MC }
            maven { url SpigotRepositories.SPIGOT_MC }
            maven { url 'https://repo.screamingsandals.org/public/' }
            maven { url 'https://repo.hoz.network/repository/maven-public/' }
            maven { url 'https://repo.velocitypowered.com/snapshots/' }
            maven { url 'https://maven.fabricmc.net/' }
            maven { url 'https://repo.extendedclip.com/content/repositories/placeholderapi/' }
        }

        project.dependencies {
            compileOnly 'org.jetbrains:annotations:20.1.0'
        }

        project.dependencies.ext['waterfall'] = { String version = null ->
            WATERFALL.format(version)
        }

        project.dependencies.ext['velocity'] = { String version = null ->
            VELOCITY.format(version)
        }

        project.dependencies.ext['paperlib'] = { String version = null ->
            PAPERLIB.format(version)
        }

        project.dependencies.ext['placeholder_api'] = { String version = null ->
            PLACEHOLDERAPI.format(version)
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
            if (System.getenv("GITLAB_REPO") != null) {
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
        //still need TODO this.
/*
        project.javadoc {
            def srcmain = project.file("src/main");
            def processDelombok = srcmain.exists() && srcmain.listFiles().length > 0
            if (processDelombok) {
                dependsOn 'delombokForJavadoc'
                source = project.tasks.getByName('delombokForJavadoc').outputDir
            }
            options.addBooleanOption('html5', true)
        } // Ceph's assigned this job to himself. Good luck boi
*/
        project.task('javadocJar', type: Jar, dependsOn: project.javadoc) {
            it.classifier = 'javadoc'
            from project.javadoc
        }

        if (System.getenv("GITLAB_REPO") != null) {
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

            it.artifacts.every {
                it.classifier = ""
            }

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

        List tasks = ["shadowJar", "publishToMavenLocal"]

        if (System.getenv("GITLAB_REPO") != null) {
            tasks.add("publish")
            tasks.add("javadoc")
        }

        project.tasks.create("screamCompile").dependsOn = tasks
    }

}