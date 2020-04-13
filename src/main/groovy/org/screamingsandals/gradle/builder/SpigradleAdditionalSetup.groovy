package org.screamingsandals.gradle.builder

import kr.entree.spigradle.SpigradleProject
import kr.entree.spigradle.project.Dependencies
import org.gradle.api.Project

import static kr.entree.spigradle.project.Dependency.*

class SpigradleAdditionalSetup {

    SpigradleProject project;

    SpigradleAdditionalSetup(Project project) {
        this.project = new SpigradleProject(project);
        setupAdditionalRepositories();
        setupAdditionalDependencies();
    }

    def setupAdditionalRepositories() {
        // not needed now
    }

    def setupAdditionalDependencies() {
        this.project.setupDependencies([
                'waterfall': dependency(Dependencies.BUNGEECORD) {
                    groupId = 'io.github.waterfallmc'
                    artifactId = 'waterfall-api'
                }
        ]);
    }
}
