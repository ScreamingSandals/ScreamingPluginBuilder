/*
 * Copyright 2024 ScreamingSandals
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
            new URL("https://api.papermc.io/v2/projects/paper/versions/$version").newInputStream().withReader {
                def map = new Gson().fromJson(it, Map.class)
                latestBuild = Collections.max(map.get("builds") as List) as int
            }

            if (latestBuild == 0) {
                throw new RuntimeException("Can't obtain build number for version $version")
            }

            def downloadName = ""
            new URL("https://api.papermc.io/v2/projects/paper/versions/$version/builds/$latestBuild").newInputStream().withReader {
                def map = new Gson().fromJson(it, Map.class)
                downloadName = ((map.get("downloads") as Map).get("application") as Map).get("name") as String
            }

            if (downloadName == "") {
                throw new RuntimeException("Can't obtain download for version $version build $latestBuild")
            }

            serverJar.withOutputStream { it << new URL("https://api.papermc.io/v2/projects/paper/versions/$version/builds/$latestBuild/downloads/$downloadName").newInputStream() }
        }

        return serverJar
    }
}
