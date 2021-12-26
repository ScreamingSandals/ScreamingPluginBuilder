package org.screamingsandals.gradle.builder.maven

import org.gradle.api.Project
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.credentials.HttpHeaderCredentials
import org.gradle.api.publish.PublishingExtension
import org.gradle.authentication.http.HttpHeaderAuthentication

class GitlabRepository implements MavenRepository {
    @Override
    void setup(Project project, PublishingExtension publishing) {
        publishing.repositories {
            it.maven { MavenArtifactRepository repository ->
                repository.url System.getenv("GITLAB_REPO")
                repository.name "GitLab"
                repository.credentials(HttpHeaderCredentials) {
                    name = 'Private-Token'
                    value = System.getenv("GITLAB_TOKEN")
                }
                repository.authentication {
                    header(HttpHeaderAuthentication)
                }
            }
        }
    }
}
