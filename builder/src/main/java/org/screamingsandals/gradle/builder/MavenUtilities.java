package org.screamingsandals.gradle.builder;

import org.gradle.api.Project;
import org.gradle.api.artifacts.SelfResolvingDependency;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.gradle.builder.maven.NexusRepository;

public final class MavenUtilities {
    private MavenUtilities() {
    }

    public static @NotNull MavenConfiguration setupPublishing(@NotNull Project project) {
        return setupPublishing(project, false, false, false);
    }

    public static @NotNull MavenConfiguration setupPublishing(@NotNull Project project, boolean onlyPomArtifact, boolean addSourceJar, boolean addJavadocJar) {
        var publishing = (PublishingExtension) project.getExtensions().getByName("publishing");
        var publication = publishing.getPublications().create("maven", MavenPublication.class, it -> {
            if (!onlyPomArtifact) {
                it.artifact(project.getTasks().getByName("jar"));
            }

            if (addSourceJar) {
                it.artifact(project.getTasks().getByName("sourceJar"));
            }

            if (addJavadocJar) {
                it.artifact(project.getTasks().getByName("javadocJar"));
            }

            it.getArtifacts().forEach(a -> a.setClassifier(""));

            it.getPom().withXml(xml -> {
                var dependenciesNode = xml.asNode().appendNode("dependencies");
                project.getConfigurations().getByName("compileOnly").getDependencies().forEach(dep -> {
                    if (!(dep instanceof SelfResolvingDependency)) {
                        var dependencyNode = dependenciesNode.appendNode("dependency");
                        dependencyNode.appendNode("groupId", dep.getGroup());
                        dependencyNode.appendNode("artifactId", dep.getName());
                        dependencyNode.appendNode("version", dep.getVersion());
                        dependencyNode.appendNode("scope", "provided");
                    }
                });
                if (project.getTasks().findByName("shadowJar") == null) {
                    project.getConfigurations().getByName("api").getDependencies().forEach(dep -> {
                        var dependencyNode = dependenciesNode.appendNode("dependency");
                        dependencyNode.appendNode("groupId", dep.getGroup());
                        dependencyNode.appendNode("artifactId", dep.getName());
                        dependencyNode.appendNode("version", dep.getVersion());
                        dependencyNode.appendNode("scope", "compile");
                    });
                }
            });
        });
        return new MavenConfiguration(publishing, publication);
    }

    public static void setupMavenRepositoriesFromProperties(@NotNull Project project) {
        var publishing = (PublishingExtension) project.getExtensions().getByName("publishing");
        if (System.getenv(Constants.NEXUS_URL_RELEASE_PROPERTY) != null
                && System.getenv(Constants.NEXUS_URL_SNAPSHOT_PROPERTY) != null
                && System.getenv(Constants.NEXUS_USERNAME_PROPERTY) != null
                && System.getenv(Constants.NEXUS_PASSWORD_PROPERTY) != null
        ) {
            new NexusRepository().setup(project, publishing);
        }
    }

    public static final class MavenConfiguration {
        private final @NotNull PublishingExtension extension;
        private final @NotNull MavenPublication publication;

        public MavenConfiguration(@NotNull PublishingExtension extension, @NotNull MavenPublication publication) {
            this.extension = extension;
            this.publication = publication;
        }

        public @NotNull PublishingExtension getExtension() {
            return extension;
        }

        public @NotNull MavenPublication getPublication() {
            return publication;
        }
    }
}
