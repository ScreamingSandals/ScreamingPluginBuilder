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

        return serverJar
    }
}
