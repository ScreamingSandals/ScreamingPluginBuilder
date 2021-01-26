package org.screamingsandals.gradle.builder.dependencies

import java.util.function.Function

class VersionModifier {
    static final Function<String, String> SNAPSHOT_MODIFIER = { String version ->
        modifyVersion(["SNAPSHOT"], version)
    }

    static final Function<String, String> SPIGOT_MODIFIER = { String version ->
        modifyVersion(["R0.1", "SNAPSHOT"], version)
    }

    static final Function<String, String> BUNGEECORD_R04_MODIFIER = { String version ->
        modifyVersion(["R0.4", "SNAPSHOT"], version)
    }

    static String modifyVersion(List<String> tags, String version) {
        def pieces = version.split("-")
        def finalVersion = version
        tags.drop(pieces.length - 1).each {
            finalVersion += "-" + it
        }
        return finalVersion
    }
}
