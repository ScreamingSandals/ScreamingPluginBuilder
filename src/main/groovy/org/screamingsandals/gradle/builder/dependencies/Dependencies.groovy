package org.screamingsandals.gradle.builder.dependencies

import org.gradle.api.Project

import static java.lang.reflect.Modifier.isStatic

class Dependencies {
    static final SPIGOT = new Dependency().each {
        it.group = "org.spigotmc"
        it.name = "spigot-api"
        it.version = "1.16.5-R0.1-SNAPSHOT"
        it.versionModifier = VersionModifier.SPIGOT_MODIFIER
    }

    static final PAPER = new Dependency(SPIGOT).each {
        it.group = "com.destroystokyo.paper"
        it.name = "paper-api"
    }

    static final PROTOCOL_LIB = new Dependency().each {
        it.group = "com.comphenix.protocol"
        it.name = "ProtocolLib"
        it.version = "4.5.1"
    }

    static final VAULT = new Dependency().each {
        it.group = "com.github.MilkBowl"
        it.name = "VaultAPI"
        it.version = "1.7"
    }

    static final LUCK_PERMS = new Dependency().each {
        it.group = "net.luckperms"
        it.name = "api"
        it.version = "5.1"
    }

    static final WORLDEDIT = new Dependency().each {
        it.group = "com.sk89q.worldedit"
        it.name = "worldedit-bukkit"
        it.version = "7.1.0"
    }

    static final WORLDGUARD = new Dependency().each {
        it.group = "com.sk89q.worldguard"
        it.name = "worldguard-bukkit"
        it.version = "7.0.3"
    }

    static final ESSENTIALS_X = new Dependency().each {
        it.group = "net.ess3"
        it.name = "EssentialsX"
        it.version = "2.17.2"
    }

    static final B_STATS = new Dependency().each {
        it.group = "org.bstats"
        it.name = "bstats-bukkit"
        it.version = "1.7"
    }

    static final B_STATS_LITE = new Dependency(B_STATS).each {
        it.name = "bstats-bukkit-lite"
    }

    static final BUNGEECORD =  new Dependency().each {
        it.group = "net.md-5"
        it.name = "bungeecord-api"
        it.version = "1.16-R0.4-SNAPSHOT"
        it.versionModifier = VersionModifier.BUNGEECORD_R04_MODIFIER // change that later
    }

    static final NUKKIT = new Dependency().each {
        it.group = "cn.nukkit"
        it.name = "nukkit"
        it.version = "2.0-SNAPSHOT"
        it.versionModifier = VersionModifier.SNAPSHOT_MODIFIER
    }

    static final WATERFALL = new Dependency(BUNGEECORD).each {
        it.group = "io.github.waterfallmc"
        it.name = "waterfall-api"
    }

    static final VELOCITY = new Dependency().each {
        it.group = "com.velocitypowered"
        it.name = "velocity-api"
        it.version = "1.1.2-SNAPSHOT"
        it.versionModifier = VersionModifier.SNAPSHOT_MODIFIER
    }

    static final PAPERLIB =  new Dependency().each {
        it.group = "io.papermc"
        it.name = "paperlib"
        it.version = "1.0.6-SNAPSHOT"
        it.versionModifier = VersionModifier.SNAPSHOT_MODIFIER
    }

    static final PLACEHOLDERAPI = new Dependency().each {
        it.group = "me.clip"
        it.name = "placeholderapi"
        it.version = "2.10.9"
    }

    static final CLOUD = new Dependency().each {
        it.group = "cloud.commandframework"
        it.name = "cloud-core"
        it.version = "1.4.0"
    }

    static final CLOUD_PAPER = new Dependency(CLOUD).each {
        it.name = "cloud-paper"
    }

    static final CLOUD_BUNGEE = new Dependency(CLOUD).each {
        it.name = "cloud-bungee"
    }

    static final CLOUD_VELOCITY = new Dependency(CLOUD).each {
        it.name = "cloud-velocity"
    }

    static final MINESTOM = new Dependency().each {
        it.group = "com.github.Minestom"
        it.name = "Minestom"
        it.version = "-SNAPSHOT"
    }

    static final SPONGE = new Dependency().each {
        it.group = "org.spongepowered"
        it.name = "spongeapi"
        it.version = "8.0.0-SNAPSHOT"
    }

    static final NUKKIT_X = NUKKIT

    def static registerDependenciesMethods(Project project) {
        Dependencies.class.declaredFields.findAll {
            isStatic(it.modifiers)
        }.each {field ->
            project.dependencies.ext[toCamelCase(field.name)] = { String version = null ->
               Dependencies[field.name].format(version)
            }
        }
    }

    static String toCamelCase(String s){
        def parts = s.split("_")
        def camelCaseString = ""
        parts.each {
            if (camelCaseString.isEmpty()) {
                camelCaseString += it.toLowerCase()
            } else {
                camelCaseString += it.substring(0, 1).toUpperCase() + it.substring(1).toLowerCase()
            }
        }
        return camelCaseString
    }

}
