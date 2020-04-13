package org.screamingsandals.gradle.builder.attributes

class BungeePluginAttributes {
	String main
	String name
	String version
	String description
	String author
	List<String> depends = new ArrayList<>()
	List<String> softDepends = new ArrayList<>()
}
