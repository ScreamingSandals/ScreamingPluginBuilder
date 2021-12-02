package org.screamingsandals.gradle.builder.repositories

import org.gradle.api.Project

import static java.lang.reflect.Modifier.isStatic

class Repositories {
    static final SCREAMING = "https://repo.screamingsandals.org/public/"
    static final SONATYPE = "https://oss.sonatype.org/content/repositories/snapshots/"
    static final JITPACK = "https://jitpack.io"
    static final VELOCITY = "https://nexus.velocitypowered.com/repository/maven-public/"
    static final SPIGOTMC = "https://hub.spigotmc.org/nexus/content/repositories/snapshots/"
    static final PROTOCOL_LIB = "https://repo.dmulloy2.net/nexus/repository/public/"
    static final ENGINEHUB = "https://maven.enginehub.org/repo/"
    static final CODEMC = "https://repo.codemc.org/repository/maven-public/"
    static final ENDER_ZONE = "https://ci.ender.zone/plugin/repository/everything/"
    static final FROSTCAST = "https://ci.frostcast.net/plugin/repository/everything"
    static final NUKKITX = "https://repo.nukkitx.com/maven-snapshots"
    static final PLACEHOLDER_API = "https://repo.extendedclip.com/content/repositories/placeholderapi/"
    static final MINECRAFT_LIBRARIES = "https://libraries.minecraft.net"
    static final SPONGE = "https://repo-new.spongepowered.org/repository/maven-public/"
    static final PURPURMC = "https://repo.pl3x.net/"
    static final PAPERMC = "https://papermc.io/repo/repository/maven-public/"

    static final BUNGEECORD = SONATYPE
    static final VAULT = JITPACK
    static final SPIGOT = SPIGOTMC
    static final PAPER = PAPERMC
    static final PURPUR = PURPURMC
    static final B_STATS = CODEMC
    static final ESSENTIALS_X = ENDER_ZONE

    def static registerRepositoriesMethods(Project project) {
        Repositories.class.declaredFields.findAll {
            isStatic(it.modifiers)
        }.each {field ->
            project.repositories.ext[toCamelCase(field.name)] = {
                project.repositories.maven { it.url Repositories[field.name] }
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
