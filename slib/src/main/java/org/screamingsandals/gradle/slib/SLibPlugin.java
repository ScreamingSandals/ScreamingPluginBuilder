package org.screamingsandals.gradle.slib;

import com.github.jengelman.gradle.plugins.shadow.ShadowPlugin;
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

public class SLibPlugin implements Plugin<Project> {
    // TODO: multi-module projects (now this supports just single-module projects)
    @Override
    public void apply(Project project) {
        project.apply(it -> {
           it.plugin(ShadowPlugin.class);
        });

        project.getRepositories().add(project.getRepositories().mavenCentral());

        if (project.getRepositories().findByName(Constants.SANDALS_REPO_NAME) == null) {
            project.getRepositories().add(
                    project.getRepositories().maven(it -> {
                        it.setName(Constants.SANDALS_REPO_NAME);
                        it.setUrl(Constants.SANDALS_REPO_URL);
                    })
            );
        }

        if (project.getRepositories().findByName(Constants.PAPER_REPO_NAME) == null) {
            project.getRepositories().add(
                    project.getRepositories().maven(it -> {
                        it.setName(Constants.PAPER_REPO_NAME);
                        it.setUrl(Constants.PAPER_REPO_URL);
                    })
            );
        }

        var extension = project.getExtensions().create("slib", SLibExtension.class);

        project.afterEvaluate(project1 -> {
            if (extension.getVersion() == null) {
                throw new UnsupportedOperationException("ScreamingLib version can't be null!");
            }

            var dependencies = project1.getDependencies();
            if (extension.getPlatforms().stream().allMatch(s -> s.equals("bungee") || s.equals("velocity"))) {
                // Proxy
                dependencies.add(Constants.IMPLEMENTATION_CONFIGURATION, Constants.SCREAMING_LIB_GROUP_ID + ":proxy-common:" + extension.getVersion());
                extension.getPlatforms().forEach(s -> {
                    dependencies.add(Constants.IMPLEMENTATION_CONFIGURATION, Constants.SCREAMING_LIB_GROUP_ID + ":proxy-" + s + ":" + extension.getVersion());
                });
            } else if (extension.getPlatforms().stream().noneMatch(s -> s.equals("bungee") || s.equals("velocity"))) {
                // Core
                dependencies.add(Constants.IMPLEMENTATION_CONFIGURATION, Constants.SCREAMING_LIB_GROUP_ID + ":core-common:" + extension.getVersion());
                extension.getPlatforms().forEach(s -> {
                    dependencies.add(Constants.IMPLEMENTATION_CONFIGURATION, Constants.SCREAMING_LIB_GROUP_ID + ":core-" + s + ":" + extension.getVersion());
                });
            } else {
                throw new UnsupportedOperationException("Can't mix Proxy and Core modules together! Please create separated projects or subprojects for proxy and for core!");
            }

            extension.getAdditionalContent().forEach(additionalContent -> {
                additionalContent.apply(dependencies, extension.getVersion(), extension.getPlatforms());
            });
            if (project.getPlugins().hasPlugin("kotlin-kapt")) {
                dependencies.add(Constants.KAPT, Constants.SCREAMING_LIB_GROUP_ID + ":annotation:" + extension.getVersion());
            } else {
                dependencies.add(Constants.ANNOTATION_PROCESSOR, Constants.SCREAMING_LIB_GROUP_ID + ":annotation:" + extension.getVersion());
            }

            /**
             * This allows us to build the final product without depending on Bukkit api.
             */
            if (!extension.isDontTryToTrickTheCompiler() && extension.getPlatforms().contains("bukkit")) { // TODO: check if there's no bukkit in classpath
                try {
                    var slibCompilationTricks = Files.createTempDirectory("slibCompilationTricks").toFile().getAbsoluteFile();

                    var fakesMap = Map.of(
                        "org/bukkit/plugin/java/JavaPlugin.class", "/fakes/JavaPlugin.class",
                        "org/bukkit/plugin/Plugin.class", "/fakes/Plugin.class",
                        "org/bukkit/plugin/PluginBase.class", "/fakes/PluginBase.class",
                        "org/slf4j/Logger.class", "/fakes/Logger.class"
                    );

                    for (var entry : fakesMap.entrySet()) {
                        var className = entry.getKey();
                        var savedFake = entry.getValue();

                        var stream = SLibPlugin.class.getResourceAsStream(savedFake);
                        var trick = new File(slibCompilationTricks, className);
                        trick.getParentFile().mkdirs();
                        Files.copy(stream, trick.toPath());
                    }

                    dependencies.add("compileOnly", project1.files(slibCompilationTricks));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            // TODO: bungee
            // TODO: velocity
            // TODO: minestom
            // TODO: sponge

            if (!extension.isDontRelocate()) {
                var path = extension.getCustomRelocatePath() != null ? extension.getCustomRelocatePath() : (project1.getGroup() + ".lib");
                var shadowJar = project1.getTasks().withType(ShadowJar.class).getByName("shadowJar");
                shadowJar.relocate("org.screamingsandals.lib", path);
                if (extension.getAdditionalContent().stream().anyMatch(additionalContent -> additionalContent instanceof ThirdPartyModule && ((ThirdPartyModule) additionalContent).getGroupId().equals(Constants.SIMPLE_INVENTORIES_GROUP_ID))) {
                    shadowJar.relocate("org.screamingsandals.simpleinventories", path + ".inventories");
                }
            }
        });
    }
}