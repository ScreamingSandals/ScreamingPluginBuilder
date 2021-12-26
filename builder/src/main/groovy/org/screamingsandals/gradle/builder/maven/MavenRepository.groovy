package org.screamingsandals.gradle.builder.maven

import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension

interface MavenRepository {
    void setup(Project project, PublishingExtension publishing)
}