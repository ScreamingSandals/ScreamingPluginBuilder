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

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import dev.yumi.gradle.licenser.YumiLicenserGradleExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.attributes.DocsType
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.javadoc.Javadoc

fun Project.configureShadowPlugin(action: (ShadowJar.() -> Unit)? = null) =
    Utilities.configureShadowPlugin(this).let {
        if (action != null) {
            it.configure(action)
        }
    }

fun Project.configureLicenser(action: (YumiLicenserGradleExtension.() -> Unit)? = null) =
    Utilities.configureLicenser(this)?.let {
        if (action != null) {
            it.action()
        }
    }

fun Project.configureSourcesJar(predicate: ((SourceSet) -> Boolean)? = null, action: (Jar.() -> Unit)? = null, configureShadedSourcesInclude: Boolean = false): Utilities.JarPair =
    Utilities.configureSourcesJar(this, predicate).let {
        if (action != null) {
            it.task.configure(action)
        }
        if (configureShadedSourcesInclude && it.copySpecAction != null) {
            it.task.configure { t ->
                t.from(configurations.named("runtimeClasspath", Configuration::class.java).map { dep ->
                    dep.incoming.artifactView { view ->
                        view.withVariantReselection()
                        view.attributes { attr ->
                            attr.attribute(DocsType.DOCS_TYPE_ATTRIBUTE, objects.named(DocsType::class.java, DocsType.SOURCES))
                        }
                    }.files.map { zipTree(it) }
                }, it.copySpecAction)
            }
        }
        it
    }

fun Project.configureJavadocTasks(useSourcesJarAsInput: Boolean = false, action: (Javadoc.() -> Unit)? = null) =
    JavadocUtilities.configureJavadocTasks(this, useSourcesJarAsInput).let {
        if (action != null) {
            it.action()
        }
    }

fun Project.setupMavenPublishing(
    onlyPomArtifact: Boolean = false,
    addSourceJar: Boolean = false,
    addJavadocJar: Boolean = false,
    action: (MavenPublication.() -> Unit)? = null
) =
    MavenUtilities.setupPublishing(this, onlyPomArtifact, addSourceJar, addJavadocJar).let {
        if (action != null) {
            it.action()
        }
    }

fun Project.setupMavenRepositoriesFromProperties() = MavenUtilities.setupMavenRepositoriesFromProperties(this)

fun Project.configureJavac(javaVersion: JavaVersion) = Utilities.configureJavac(this, javaVersion)