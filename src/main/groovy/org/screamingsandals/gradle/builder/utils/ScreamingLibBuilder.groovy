package org.screamingsandals.gradle.builder.utils

import org.gradle.api.Project

class ScreamingLibBuilder {
    private final Project project

    private List<String> platforms = []
    private List<String> modules = ["plugin"]
    private String version
    private boolean annotationProcessor = false

    ScreamingLibBuilder(Project project) {
        this.project = project
    }

    ScreamingLibBuilder version(String version) {
        this.version = version
        return this
    }

    ScreamingLibBuilder enableAnnotationProcessor() {
        this.annotationProcessor = true
        return this
    }

    ScreamingLibBuilder platforms(String...newPlatforms) {
        newPlatforms.each {
            if (!platforms.contains(it)) {
                platforms += it
            }
        }
        return this
    }

    ScreamingLibBuilder modules(String...newModules) {
        newModules.each {
            if (!modules.contains(it)) {
                modules += it
            }
        }
        return this
    }

    void build() {
        if (version == null) {
            throw new RuntimeException("Version of ScreamingLib can't be null")
        }

        project.dependencies {
            api "org.screamingsandals.lib:utils-common:$version"

            if (annotationProcessor) {
                it.annotationProcessor "org.screamingsandals.lib:screaming-annotation:$version"
            }
        }

        modules.each {module ->
            platforms.each {platform ->
                project.dependencies {
                    api "org.screamingsandals.lib:${module.toLowerCase()}-${platform.toLowerCase()}:$version"
                }
            }
        }
    }
}
