package org.screamingsandals.gradle.builder.builder

import org.gradle.api.Project

/**
 * @author Frantisek Novosad (fnovosad@monetplus.cz)
 */
class BuilderSelector {
    private final Project project;

    ScreamingBuilder builder() {
        return new ScreamingBuilder(project);
    }

    LiteScreamingBuilder liteBuilder() {
        return new LiteScreamingBuilder(project);
    }
}
