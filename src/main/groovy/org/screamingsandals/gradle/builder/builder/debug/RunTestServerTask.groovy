package org.screamingsandals.gradle.builder.builder.debug

import com.google.gson.Gson
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
        if (!testServerDirectory.exists()) {
            testServerDirectory.mkdirs()
        }

        println 'Preparing server.jar'
        def serverJar = new File(testServerDirectory, "server.jar")
        if (!serverJar.exists()) {
            def latestBuild = 0
            new URL("https://papermc.io/api/v2/projects/paper/versions/$version").newInputStream().withReader {
                def map = new Gson().fromJson(it, Map.class)
                latestBuild = Collections.max(map.get("builds") as List) as int
            }

            if (latestBuild == 0) {
                throw new RuntimeException("Can't obtain build number for version $version")
            }

            def downloadName = ""
            new URL("https://papermc.io/api/v2/projects/paper/versions/$version/builds/$latestBuild").newInputStream().withReader {
                def map = new Gson().fromJson(it, Map.class)
                downloadName = ((map.get("downloads") as Map).get("application") as Map).get("name") as String
            }

            if (downloadName == "") {
                throw new RuntimeException("Can't obtain download for version $version build $latestBuild")
            }

            serverJar.withOutputStream { it << new URL("https://papermc.io/api/v2/projects/paper/versions/$version/builds/$latestBuild/downloads/$downloadName").newInputStream() }
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

        Files.copy(pluginJar, testServerDirectory.toPath().resolve("plugins/debugPlugin.jar"), StandardCopyOption.REPLACE_EXISTING)

        classpath(serverJar)
        setWorkingDir(testServerDirectory)

        super.exec()
    }
}
