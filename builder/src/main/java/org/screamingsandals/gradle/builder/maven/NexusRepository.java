package org.screamingsandals.gradle.builder.maven;

import org.gradle.api.Project;
import org.gradle.api.publish.PublishingExtension;
import org.jetbrains.annotations.NotNull;

public class NexusRepository implements MavenRepository {
    @Override
    public void setup(@NotNull Project project, @NotNull PublishingExtension publishing) {
        project.afterEvaluate(p1 ->
                publishing.repositories(it ->
                        it.maven(repository -> {
                            if (((String) project.getVersion()).contains("SNAPSHOT")) {
                                repository.setUrl(System.getenv("NEXUS_URL_SNAPSHOT"));
                            } else {
                                repository.setUrl(System.getenv("NEXUS_URL_RELEASE"));
                            }
                            repository.getCredentials().setUsername(System.getenv("NEXUS_USERNAME"));
                            repository.getCredentials().setPassword(System.getenv("NEXUS_PASSWORD"));
                        })
                )
        );
    }
}
