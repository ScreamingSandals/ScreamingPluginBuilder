package org.screamingsandals.gradle.builder.attributes

import kr.entree.spigradle.annotation.MappingObject

@MappingObject
class BungeePluginAttributes {
    String main
    String name
    String version
    String description
    String author
    List<String> depends = new ArrayList<>()
    List<String> softDepends = new ArrayList<>()
}
