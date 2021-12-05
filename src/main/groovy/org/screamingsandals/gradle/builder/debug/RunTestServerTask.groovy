package org.screamingsandals.gradle.builder.debug

import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.JavaExec

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

class RunTestServerTask extends JavaExec {
    @Internal
    String version
    @Internal
    Path pluginJar
    @Internal
    Map<String, String> properties
    @Internal
    String subDirectory

    RunTestServerTask() {
        super()
        dependsOn('screamCompile')
        standardInput = System.in
    }

    def setVersion(String version) {
        this.version = version
    }

    def setPluginJar(Path pluginJar) {
        this.pluginJar = pluginJar
    }

    def setProperties(Map<String, String> map) {
        this.properties = map
    }

    def setSubDirectory(String subDirectory) {
        this.subDirectory = subDirectory
    }

    @Override
    void exec() {
        def testServerDirectory = project.file("test-environment/$subDirectory/$version")

        def serverJar = TestServerUtils.prepareServer(testServerDirectory, version, false)

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
        if (version.matches("1\\.(\\d|10|11)\\..*")) {
            properties['use-native-transport'] = "false"
        }
        def needsToBeSaved = false
        properties.each {
            if (props.hasProperty(it.key) || props.getProperty(it.key) != it.value) {
                props.setProperty(it.key, it.value)
                needsToBeSaved = true
            }
        }
        if (needsToBeSaved) {
            props.store(serverProperties.newDataOutputStream(), null)
        }

        println 'Preparing plugin'
        def plugins = new File(testServerDirectory, "plugins")
        if (!plugins.exists()) {
            plugins.mkdirs()
        }

        if (version.matches(/1\.(([0-9]|1[0-5])(\..*)?$|16(\.[0-4])?$)/)) { // old versions
            Files.copy(pluginJar, testServerDirectory.toPath().resolve("plugins/debugPlugin.jar"), StandardCopyOption.REPLACE_EXISTING)
        } else {
            // make sure plugins/debugPlugin.jar doesn't exist anymore
            if (Files.exists(testServerDirectory.toPath().resolve("plugins/debugPlugin.jar"))) {
                Files.delete(testServerDirectory.toPath().resolve("plugins/debugPlugin.jar"))
            }

            args("-add-plugin=${pluginJar.toAbsolutePath().toString()}")
        }

        classpath(serverJar)
        setWorkingDir(testServerDirectory)

        super.exec()
    }
}
