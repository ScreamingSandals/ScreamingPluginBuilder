package org.screamingsandals.gradle.builder.utils

import org.gradle.api.Project

// TODO: get rid of this shit and use the new one for internal projects too
@Deprecated
class ScreamingLibBuilder {
    private final Project project

    private List<String> platforms = []
    private List<String> modules = []
    private List<String> universalModules = []
    private String version
    private String simpleInventories = null
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

    ScreamingLibBuilder universalModules(String...newModules) {
        newModules.each {
            if (!universalModules.contains(it)) {
                universalModules += it
            }
        }
        return this
    }

    ScreamingLibBuilder simpleInventories(String version) {
        simpleInventories = version
        return this
    }

    void build() {
        if (version == null) {
            throw new RuntimeException("Version of ScreamingLib can't be null")
        }

        project.dependencies {
            api "org.screamingsandals.lib:utils-common:$version"

            if (annotationProcessor) {
                it.annotationProcessor "org.screamingsandals.lib:annotation:$version"
            }
        }

        if (simpleInventories) {
            if (!modules.contains("core")) {
                modules += "core"
            }

            platforms.each { platform ->
                project.dependencies {
                    api "org.screamingsandals.simpleinventories:core-${platform.toLowerCase()}:$simpleInventories", {
                        exclude group: 'org.screamingsandals.lib' // causes problems
                    }
                }
            }
        }

        universalModules.each {module ->
            project.dependencies {
                api "org.screamingsandals.lib:${module.toLowerCase()}:$version"
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
