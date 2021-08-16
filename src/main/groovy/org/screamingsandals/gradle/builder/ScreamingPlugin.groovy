package org.screamingsandals.gradle.builder

import io.freefair.gradle.plugins.lombok.LombokPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.screamingsandals.gradle.builder.builder.BuilderSelector
import org.screamingsandals.gradle.builder.builder.ScreamingLibBuilder
import org.screamingsandals.gradle.builder.builder.debug.TestTaskBuilder
import org.screamingsandals.gradle.builder.dependencies.Dependencies
import org.screamingsandals.gradle.builder.repositories.Repositories

/**
 * @author Frantisek Novosad (fnovosad@monetplus.cz)
 */
class ScreamingPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.apply {
            plugin MavenPublishPlugin.class
            plugin LombokPlugin.class
            plugin JavaLibraryPlugin.class
        }

        Repositories.registerRepositoriesMethods(project)
        Dependencies.registerDependenciesMethods(project)

        project.dependencies.ext['screaming'] = { String lib, String version ->
            return "org.screamingsandals.lib:$lib:$version"
        }

        project.extensions.create("screamingBuilder", BuilderSelector, project)
        project.extensions.create("screamingTest", TestTaskBuilder, project)
        project.extensions.create("screamingLib", ScreamingLibBuilder, project)
    }
}
