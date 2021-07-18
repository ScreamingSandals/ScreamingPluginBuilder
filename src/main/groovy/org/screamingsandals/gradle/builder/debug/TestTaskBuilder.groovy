package org.screamingsandals.gradle.builder.debug

import org.gradle.api.Project

import java.nio.file.Path

class TestTaskBuilder {
    private final Project project

    private List<String> versions
    private Path pluginJar
    private List<String> args = ['nogui']
    private List<String> jvmArgs = []
    private Map<String, String> serverProperties = [:]
    private String subdirectory = "paper"

    TestTaskBuilder(Project project) {
        this.project = project
    }

    def versions(String... versions) {
        this.versions = versions
        return this
    }

    def port(int port) {
        serverProperties['port'] = port as String
        return this
    }

    def onlineMode(boolean onlineMode) {
        serverProperties['online-mode'] = onlineMode as String
        return this
    }

    def pluginJar(Path pluginJar) {
        this.pluginJar = pluginJar
        return this
    }

    def args(String... args) {
        this.args = args
        return this
    }

    def jvmArgs(String... jvmArgs) {
        this.jvmArgs = jvmArgs
        return this
    }

    def addToServerProperties(String name, String value) {
        serverProperties[name] = value
        return this
    }

    def setSubdirectory(String subdirectory) {
        this.subdirectory = subdirectory
        return this
    }

    def build() {
        if (System.getenv("OPTIMIZE_FOR_CI_CD") != "1") {
            versions.each { version ->
                this.project.task("runPaperServer$version", type: RunTestServerTask) {
                    args(this.args)
                    jvmArgs(this.jvmArgs)
                    it.version = version
                    it.pluginJar = this.pluginJar
                    it.properties = this.serverProperties
                    it.subDirectory = this.subdirectory
                }
            }
        }
    }
}
