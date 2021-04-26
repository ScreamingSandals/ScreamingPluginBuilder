package org.screamingsandals.gradle.builder.debug

import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.JavaExec

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

class RunTestServerTask extends JavaExec {
    @Internal
    int port = 25565
    @Internal
    String version
    @Internal
    boolean onlineMode = true
    @Internal
    Path pluginJar

    RunTestServerTask() {
        super()
        dependsOn('screamCompile')
        standardInput = System.in
    }

    def setPort(int port) {
        this.port = port
    }

    def setVersion(String version) {
        this.version = version
    }

    def setOnlineMode(boolean onlineMode) {
        this.onlineMode = onlineMode
    }

    def setPluginJar(Path pluginJar) {
        this.pluginJar = pluginJar
    }

    @Override
    void exec() {
        def testServerDirectory = project.file("test-environment/paper/$version")
        if (!testServerDirectory.exists()) {
            testServerDirectory.mkdirs()
        }

        println 'Preparing server.jar'
        def serverJar = new File(testServerDirectory, "server.jar")
        if (!serverJar.exists()) {
            serverJar.withOutputStream { it << new URL("https://papermc.io/api/v1/paper/$version/latest/download").newInputStream() }
        }

        def eulaTxt = new File(testServerDirectory, "eula.txt")
        if (!eulaTxt.exists()) {
            println 'By using this test service you agree to the EULA (https://account.mojang.com/documents/minecraft_eula)'
            eulaTxt.withOutputStream {it << "eula=true" }
        }

        println 'Preparing server.properties'
        def serverProperties = new File(testServerDirectory, "server.properties")
        def props = new Properties()
        if (serverProperties.exists()) {
            props.load(serverProperties.newDataInputStream())
        }
        def needsToBeSaved = false
        if (props.hasProperty('port') || props.getProperty('port') != String.valueOf(port)) {
            props.setProperty('port', String.valueOf(port))
            needsToBeSaved = true
        }
        if (props.hasProperty('onlineMode') || props.getProperty('onlineMode') != String.valueOf(onlineMode)) {
            props.setProperty('onlineMode', String.valueOf(onlineMode))
            needsToBeSaved = true
        }
        if (needsToBeSaved) {
            props.store(serverProperties.newDataOutputStream(), null)
        }

        println 'Preparing plugin'
        def plugins = new File(testServerDirectory, "plugins")
        if (!plugins.exists()) {
            plugins.mkdirs()
        }

        Files.copy(pluginJar, testServerDirectory.toPath().resolve("plugins/debugPlugin.jar"), StandardCopyOption.REPLACE_EXISTING)

        classpath(serverJar)
        setWorkingDir(testServerDirectory)

        super.exec()
    }
}
