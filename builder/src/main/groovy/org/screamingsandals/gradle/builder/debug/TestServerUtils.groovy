package org.screamingsandals.gradle.builder.debug

import com.google.gson.Gson

class TestServerUtils {
    def static prepareServer(File testServerDirectory, String version, boolean forceUpdate) {
        if (!testServerDirectory.exists()) {
            testServerDirectory.mkdirs()
        }

        println 'Preparing server.jar'
        def serverJar = new File(testServerDirectory, "server.jar")
        if (!serverJar.exists() || forceUpdate) {
            def downloadUrl = ""
            def connection = new URL("https://fill.papermc.io/v3/projects/paper/versions/$version/builds/latest").openConnection()
            connection.setRequestProperty("User-Agent", "screaming-gradle/1.0.88 (https://github.com/ScreamingSandals/ScreamingPluginBuilder)")

            connection.getInputStream().withReader {
                def map = new Gson().fromJson(it, Map.class)
                downloadUrl = ((map.get("downloads") as Map).get("server:default") as Map).get("url") as String
            }

            if (downloadUrl == "") {
                throw new RuntimeException("Can't obtain download for version $version")
            }

            serverJar.withOutputStream { it << new URL(downloadUrl).newInputStream() }
        }

        return serverJar
    }
}
