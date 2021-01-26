package org.screamingsandals.gradle.builder.dependencies

import java.util.function.Function

class Dependency {
    String group
    String name
    String version
    Function<String, String> versionModifier = { it }

    Dependency(Dependency origin = null) {
        origin?.each {
            this.group = it.group
            this.name = it.name
            this.version = it.version
            this.versionModifier = it.versionModifier
        }
    }

    String format(String specificVersion = null) {
        return "${group}:${name}:${versionModifier.apply(specificVersion ?: version)}"
    }
}
