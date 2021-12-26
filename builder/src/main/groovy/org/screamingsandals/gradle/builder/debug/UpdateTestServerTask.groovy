package org.screamingsandals.gradle.builder.debug

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

class UpdateTestServerTask extends DefaultTask {
    @Internal
    String version
    @Internal
    String subDirectory

    def setVersion(String version) {
        this.version = version
    }

    def setSubDirectory(String subDirectory) {
        this.subDirectory = subDirectory
    }

    @TaskAction
    def run() {
        def testServerDirectory = project.file("test-environment/$subDirectory/$version")

        TestServerUtils.prepareServer(testServerDirectory, version, true)
    }
}
