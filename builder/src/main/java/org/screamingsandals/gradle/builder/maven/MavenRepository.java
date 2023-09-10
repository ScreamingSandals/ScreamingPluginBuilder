package org.screamingsandals.gradle.builder.maven;

import org.gradle.api.Project;
import org.gradle.api.publish.PublishingExtension;
import org.jetbrains.annotations.NotNull;

public interface MavenRepository {
    void setup(@NotNull Project project, @NotNull PublishingExtension publishing);
}