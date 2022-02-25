package org.screamingsandals.gradle.builder.maven

import org.gradle.api.Project
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.publish.PublishingExtension

class NexusRepository implements MavenRepository {
    @Override
    void setup(Project project, PublishingExtension publishing) {
        project.afterEvaluate {
            publishing.repositories {
                it.maven({ MavenArtifactRepository repository ->
                    if (((String) project.version).contains("SNAPSHOT")) {
                        repository.url = System.getenv("NEXUS_URL_SNAPSHOT")
                    } else {
                        repository.url = System.getenv("NEXUS_URL_RELEASE")
                    }
                    repository.credentials.username = System.getenv("NEXUS_USERNAME")
                    repository.credentials.password = System.getenv("NEXUS_PASSWORD")
                })
            }
        }
    }
}
