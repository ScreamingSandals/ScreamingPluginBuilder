package org.screamingsandals.gradle.builder.debug

import org.gradle.api.Project

import java.nio.file.Path

class TestTaskBuilder {
    private final Project project

    private List<String> versions
    private int port = 25565
    private boolean onlineMode = true
    private Path pluginJar
    private List<String> args = ['nogui']
    private List<String> jvmArgs = []

    TestTaskBuilder(Project project) {
        this.project = project
    }

    def versions(String... versions) {
        this.versions = versions
        return this
    }

    def port(int port) {
        this.port = port
        return this
    }

    def onlineMode(boolean onlineMode) {
        this.onlineMode = onlineMode
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

    def build() {
        if (System.getenv("OPTIMIZE_FOR_CI_CD") != "1") {
            versions.each { version ->
                this.project.task("runPaperServer$version", type: RunTestServerTask) {
                    args(this.args)
                    jvmArgs(this.jvmArgs)
                    it.version = version
                    it.port = this.port
                    it.pluginJar = this.pluginJar
                    it.onlineMode = this.onlineMode
                }
            }
        }
    }
}
