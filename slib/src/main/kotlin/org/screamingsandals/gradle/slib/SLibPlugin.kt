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
package org.screamingsandals.gradle.slib

import com.github.jengelman.gradle.plugins.shadow.ShadowPlugin
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.compile.JavaCompile
import org.jetbrains.kotlin.samWithReceiver.gradle.SamWithReceiverExtension

class SLibPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create("slib", SLibExtension::class.java)

        project.afterEvaluate {
            if (extension.version == null) {
                return@afterEvaluate  // Not configured
            }
            try {
                Class.forName("com.github.jengelman.gradle.plugins.shadow.ShadowPlugin")
            } catch (e: ClassNotFoundException) {
                throw RuntimeException("ScreamingLib plugin requires Shadow plugin to be present! Please add it", e)
            }

            if (!project.plugins.hasPlugin(ShadowPlugin::class.java)) {
                throw RuntimeException("Shadow plugin is loaded on the classpath, but ScreamingLib plugin requires it to be applied!")
            }

            project.repositories.add(project.repositories.mavenCentral()) // TODO: why are we adding this?

            if (project.repositories.findByName(SANDALS_REPO_NAME) == null) {
                project.repositories.add(
                    project.repositories.maven {
                        it.name = SANDALS_REPO_NAME
                        it.setUrl(SANDALS_REPO_URL)
                    }
                )
            }

            if (project.plugins.hasPlugin("org.jetbrains.kotlin.jvm")) {
                if (!extension.isDisableAutoKaptApplicationForKotlin && !project.plugins.hasPlugin("org.jetbrains.kotlin.kapt")) {
                    project.plugins.apply("org.jetbrains.kotlin.kapt")
                    println("Kapt was automatically added to your classpath. You may now see some warnings about version mismatch, to fix that, add kapt plugin yourself (the plugin must be applied after the slib plugin)")
                }
                if (!extension.isDisableAutoSAMWithReceiverConfigurationForKotlin && project.plugins.hasPlugin("kotlin-sam-with-receiver")) {
                    project.extensions.getByType(SamWithReceiverExtension::class.java)
                        .annotation("org.screamingsandals.lib.utils.annotations.ImplicitReceiver")
                }
            }
            val multiModuleProject = extension.multiModuleConfiguration != null && extension.multiModuleCommonSubproject != null
            val implConfig = if (extension.isUseApiConfigurationInsteadOfImplementation) API_CONFIGURATION else IMPLEMENTATION_CONFIGURATION

            val dependencies = project.dependencies
            if (multiModuleProject && project.name == extension.multiModuleApiSubproject) {
                dependencies.add(implConfig, "${SCREAMING_LIB_GROUP_ID}:api-utils:${extension.version}")
                if (extension.multiModuleApiSubprojectApiUtilsWrapperRelocation != null) {
                    val shadowJar = project.tasks.withType(ShadowJar::class.java).getByName("shadowJar")
                    shadowJar.relocate(
                        "org.screamingsandals.lib.api",
                        extension.multiModuleApiSubprojectApiUtilsWrapperRelocation!!
                    )
                }
                return@afterEvaluate
            }
            if (multiModuleProject && extension.multiModuleUniversalSubproject == project.name) {
                dependencies.add(implConfig, project.project(":${extension.multiModuleCommonSubproject}"))
                for (pr in extension.multiModuleConfiguration!!.keys) {
                    dependencies.add(implConfig, project.project(":$pr"))
                }
                if (extension.platforms.contains("bukkit")) {
                    project.tasks.withType(ShadowJar::class.java).getByName("shadowJar")!!
                        .getManifest().attributes(
                            mapOf("paperweight-mappings-namespace" to "mojang")
                        )
                }
                relocate(project, extension)
                return@afterEvaluate
            }

            if (extension.platforms.stream().allMatch { it == "bungee" || it == "velocity" }) {
                // Proxy
                if (multiModuleProject) {
                    if (extension.multiModuleCommonSubproject == project.name) {
                        dependencies.add(
                            implConfig,
                            "$SCREAMING_LIB_GROUP_ID:proxy-common:${extension.version}"
                        )
                        if (extension.multiModuleApiSubproject != null) {
                            dependencies.add(implConfig, project.project(":${extension.multiModuleApiSubproject}"))
                        }
                    } else if (extension.multiModuleConfiguration!!.containsKey(project.name)) {
                        val platform = extension.multiModuleConfiguration!![project.name]
                        if (!extension.platforms.contains(platform)) {
                            throw UnsupportedOperationException("Malformed multi module project configuration: Platform $platform is not configured, but is in multiModuleConfiguration map!")
                        }
                        dependencies.add(implConfig, project.project(":${extension.multiModuleCommonSubproject}"))
                        dependencies.add(
                            implConfig,
                            "${SCREAMING_LIB_GROUP_ID}:proxy-$platform:${extension.version}"
                        )
                        relocate(project, extension)
                    } else {
                        throw UnsupportedOperationException("Can't determine what is this subproject for: ${project.name}")
                    }
                } else {
                    dependencies.add(
                        implConfig,
                        "${SCREAMING_LIB_GROUP_ID}:proxy-common:${extension.version}"
                    )
                    extension.platforms.forEach { s ->
                        dependencies.add(
                            implConfig,
                            "${SCREAMING_LIB_GROUP_ID}:proxy-$s:${extension.version}"
                        )
                    }
                }
            } else if (extension.platforms.stream().noneMatch { it == "bungee" || it == "velocity" }) {
                // Core
                if (multiModuleProject) {
                    if (extension.multiModuleCommonSubproject == project.name) {
                        dependencies.add(
                            implConfig,
                            "${SCREAMING_LIB_GROUP_ID}:core-common:${extension.version}"
                        )
                        if (extension.multiModuleApiSubproject != null) {
                            dependencies.add(implConfig, project.project(":${extension.multiModuleApiSubproject}"))
                        }
                    } else if (extension.multiModuleConfiguration!!.containsKey(project.name)) {
                        val platform = extension.multiModuleConfiguration!![project.name]
                        if (!extension.platforms.contains(platform)) {
                            throw UnsupportedOperationException("Malformed multi module project configuration: Platform $platform is not configured, but is in multiModuleConfiguration map!")
                        }
                        dependencies.add(implConfig, project.project(":${extension.multiModuleCommonSubproject}"))
                        dependencies.add(
                            implConfig,
                            "${SCREAMING_LIB_GROUP_ID}:core-$platform:${extension.version}"
                        )
                        if (platform == "bukkit") {
                            project.tasks.withType(ShadowJar::class.java).getByName("shadowJar")!!
                                .getManifest().attributes(
                                    mapOf("paperweight-mappings-namespace" to "mojang")
                                )
                        }
                        relocate(project, extension)
                    } else {
                        throw UnsupportedOperationException("Can't determine what is this subproject for: ${project.name}")
                    }
                } else {
                    dependencies.add(implConfig, "${SCREAMING_LIB_GROUP_ID}:core-common:${extension.version}")
                    extension.platforms.forEach { s ->
                        dependencies.add(
                            implConfig,
                            "${SCREAMING_LIB_GROUP_ID}:core-$s:${extension.version}"
                        )
                        if (s == "bukkit") {
                            project.tasks.withType(ShadowJar::class.java).getByName("shadowJar")!!
                                .getManifest().attributes(
                                    mapOf("paperweight-mappings-namespace" to "mojang")
                                )
                        }
                    }
                }
            } else {
                throw UnsupportedOperationException("Can't mix Proxy and Core modules together! Please create separated projects or subprojects for proxy and for core!")
            }

            if (multiModuleProject) {
                if (extension.multiModuleCommonSubproject == project.name) {
                    extension.additionalContent.forEach {
                        it.applyMultiModule(
                            implConfig,
                            dependencies,
                            extension.version!!,
                            "common"
                        )
                    }
                } else if (extension.multiModuleConfiguration!!.containsKey(project.name)) {
                    val platform: String = extension.multiModuleConfiguration!![project.name]!!
                    if (!extension.platforms.contains(platform)) {
                        throw UnsupportedOperationException("Malformed multi module project configuration: Platform $platform is not configured, but is in multiModuleConfiguration map!")
                    }
                    extension.additionalContent.forEach {
                        it.applyMultiModule(
                            implConfig,
                            dependencies,
                            extension.version!!,
                            platform
                        )
                    }
                } else {
                    throw UnsupportedOperationException("Can't determine what is this subproject for: ${project.name}")
                }
            } else {
                extension.additionalContent.forEach {
                    it.apply(
                        implConfig,
                        dependencies,
                        extension.version!!,
                        extension.platforms
                    )
                }
            }
            if (!extension.isDisableAnnotationProcessor) {
                if (project.plugins.hasPlugin("kotlin-kapt")) {
                    dependencies.add(KAPT, "${SCREAMING_LIB_GROUP_ID}:annotation:${extension.version}")
                } else {
                    dependencies.add(ANNOTATION_PROCESSOR, "${SCREAMING_LIB_GROUP_ID}:annotation:${extension.version}")
                }
                if (multiModuleProject) {
                    val compileJava =
                        project.tasks.withType(JavaCompile::class.java).getByName("compileJava")
                    val file: Provider<RegularFile> =
                        project.project(":${extension.multiModuleCommonSubproject}").layout.buildDirectory.file("slib/pluginName.txt")
                    if (extension.multiModuleCommonSubproject == project.name) {
                        compileJava.options.compilerArgs.add("-AlookForPluginAndSaveFullClassNameTo=${file.get().asFile.absolutePath}")
                        compileJava.outputs.file(file)
                    } else {
                        compileJava.options.compilerArgs.add("-AusePluginClassFrom=${file.get().asFile.absolutePath}")
                        compileJava.inputs.file(file)
                    }
                }
            }

            /**
             * This allows us to build the final product without depending on Bukkit api.
             */
            if (!extension.isDisableCompilerTricks
                && (if (multiModuleProject) (extension.multiModuleConfiguration!![project.name] == "bukkit") else extension.platforms.contains(
                    "bukkit"
                ))
            ) {
                if (project.configurations.stream().noneMatch { files ->
                        files.dependencies.stream().anyMatch { dependency ->
                            (PAPER_API_GROUP == dependency.group && PAPER_API == dependency.name)
                                    || (PAPER_DESTROYSTOKYO_API_GROUP == dependency.group && PAPER_DESTROYSTOKYO_API == dependency.name)
                                    || (PAPERSPIGOT_API_GROUP == dependency.group && PAPERSPIGOT_API == dependency.name)
                                    || (SPIGOT_API_GROUP == dependency.group && SPIGOT_API == dependency.name)
                                    || (BUKKIT_GROUP == dependency.group && BUKKIT_API == dependency.name)
                        }
                    }) {
                    dependencies.add(
                        "compileOnly",
                        "${STUB_GROUP_ID}:${STUB_BUKKIT}:${STUB_VERSION}"
                    )
                }
            }

            // TODO: bungee
            // TODO: velocity
            // TODO: minestom
            // TODO: sponge
            if (!extension.isDisableRelocate && !multiModuleProject) {
                relocate(project, extension)
            }
        }
    }

    private fun relocate(project1: Project, extension: SLibExtension) {
        val path: String = extension.customRelocatePath ?: "${project1.group}.lib"
        val shadowJar = project1.tasks.withType(ShadowJar::class.java).getByName("shadowJar")
        extension.multiModuleApiSubprojectApiUtilsWrapperRelocation?.let {
            shadowJar.relocate("org.screamingsandals.lib.api", it)
        }
        shadowJar.relocate("org.screamingsandals.lib", path)
        if (extension.additionalContent.stream().anyMatch { it is ThirdPartyModule && it.groupId == SIMPLE_INVENTORIES_GROUP_ID }) {
            shadowJar.relocate("org.screamingsandals.simpleinventories", "$path.inventories")
        }
    }
}