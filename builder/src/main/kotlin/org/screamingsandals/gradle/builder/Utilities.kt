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

package org.screamingsandals.gradle.builder

import com.github.jengelman.gradle.plugins.shadow.ShadowPlugin
import com.github.jengelman.gradle.plugins.shadow.relocation.RelocatePathContext
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.cadixdev.gradle.licenser.LicenseExtension
import org.cadixdev.gradle.licenser.Licenser
import org.gradle.api.Action
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.file.CopySpec
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.compile.JavaCompile
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.screamingsandals.gradle.builder.shadow.RelocateFilterReader
import java.util.Calendar
import java.util.function.Predicate

object Utilities {
    fun configureShadowPlugin(project: Project): TaskProvider<ShadowJar> {
        project.apply { it.plugin(ShadowPlugin::class.java) }

        val jarTask = project.tasks.withType(Jar::class.java).getByName("jar")
        val oldClassifier = jarTask.archiveClassifier.get()
        jarTask.archiveClassifier.set("unshaded")
        val shadowJarTask = project.tasks.named(ShadowJar.SHADOW_JAR_TASK_NAME, ShadowJar::class.java) { shadowJar ->
            shadowJar.archiveClassifier.set(oldClassifier)
        }
        project.tasks.named("build") { build ->
            build.dependsOn(ShadowJar.SHADOW_JAR_TASK_NAME)
        }
        return shadowJarTask
    }

    fun configureLicenser(project: Project): LicenseExtension? {
        val headerFile = project.rootProject.file("license_header.txt")

        if (!headerFile.exists()) {
            return null
        }

        project.apply { it.plugin(Licenser::class.java) }

        val extension = project.extensions.getByType(LicenseExtension::class.java)
        extension.setHeader(headerFile)
        extension.ignoreFailures(true)
        extension.properties {
            it.set("year", Calendar.getInstance().get(Calendar.YEAR))
        }
        return extension
    }

    fun configureSourcesJar(project: Project, sourceSetSelector: Predicate<SourceSet>? = null): JarPair {
        val shadowJar = project.tasks.withType(ShadowJar::class.java).findByName(ShadowJar.SHADOW_JAR_TASK_NAME)
        val specAction: Action<CopySpec>?
        if (shadowJar != null) {
            specAction = Action { spec ->
                // Use our FilterReader with relocators from shadow
                spec.filter(
                    mapOf("relocators" to shadowJar.relocators),
                    RelocateFilterReader::class.java
                )
                spec.eachFile { fileCopyDetails ->
                    val path = fileCopyDetails.path
                    for (relocator in shadowJar.relocators.get()) {
                        if (relocator.canRelocatePath(path)) {
                            val relocatedPath = relocator.relocatePath(RelocatePathContext(path))
                            fileCopyDetails.path = relocatedPath
                            break
                        }
                    }
                }
            }
        } else {
            specAction = null
        }

        return JarPair(
            project.tasks.register(SOURCES_JAR_TASK_NAME, Jar::class.java) {
                it.duplicatesStrategy = DuplicatesStrategy.EXCLUDE
                it.archiveClassifier.set("sources")

                val sourceSets: SourceSetContainer =
                    project.extensions.getByType(JavaPluginExtension::class.java).sourceSets
                if (sourceSetSelector != null) {
                    sourceSets.forEach { sourceSet ->
                        if (sourceSetSelector.test(sourceSet)) {
                            if (specAction != null) {
                                it.from(sourceSet.allJava, specAction)
                            } else {
                                it.from(sourceSet.allJava)
                            }
                        }
                    }
                } else {
                    if (specAction != null) {
                        it.from(sourceSets.getByName("main").allJava, specAction)
                    } else {
                        it.from(sourceSets.getByName("main").allJava)
                    }
                }
            },
            specAction,
        )
    }

    fun configureJavac(project: Project, javaVersion: JavaVersion) {
        project.extensions.configure(JavaPluginExtension::class.java) {
            it.sourceCompatibility = javaVersion
        }
        project.tasks.withType(JavaCompile::class.java) {
            it.options.compilerArgs.add("-Xlint:deprecation")
            it.options.release.set(javaVersion.majorVersion.toInt())
        }
        // Automatically configure Kotlin if present
        if (project.plugins.hasPlugin("org.jetbrains.kotlin.jvm")) {
            project.extensions.getByType(KotlinJvmProjectExtension::class.java).compilerOptions.jvmTarget.set(
                JvmTarget.fromTarget(javaVersion.toString())
            )
        }
    }

    class JarPair(val task: TaskProvider<Jar>, val copySpecAction: Action<CopySpec>?)
}