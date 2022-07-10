package org.screamingsandals.gradle.slib;

import com.github.jengelman.gradle.plugins.shadow.ShadowPlugin;
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.compile.JavaCompile;
import org.jetbrains.kotlin.samWithReceiver.gradle.SamWithReceiverExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

public class SLibPlugin implements Plugin<Project> {
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
                return; // Not configured
            }

            if (project.getPlugins().hasPlugin("org.jetbrains.kotlin.jvm")) {
                if (!extension.isDisableAutoKaptApplicationForKotlin() && !project.getPlugins().hasPlugin("org.jetbrains.kotlin.kapt")){
                    project.getPlugins().apply("org.jetbrains.kotlin.kapt");
                    System.out.println("Kapt was automatically added to your classpath. You may now see some warnings about version mismatch, to fix that, add kapt plugin yourself (the plugin must be applied after the slib plugin)");
                }
                if (!extension.isDisableAutoSAMWithReceiverConfigurationForKotlin() && project.getPlugins().hasPlugin("kotlin-sam-with-receiver")) {
                    project.getExtensions().getByType(SamWithReceiverExtension.class).annotation("org.screamingsandals.lib.utils.annotations.ImplicitReceiver");
                }
            }
            var multiModuleProject = extension.getMultiModuleConfiguration() != null && extension.getMultiModuleCommonSubproject() != null && extension.getMultiModuleUniversalSubproject() != null;
            var implConfig = extension.isUseApiConfigurationInsteadOfImplementation() ? Constants.API_CONFIGURATION : Constants.IMPLEMENTATION_CONFIGURATION;

            var dependencies = project1.getDependencies();
            if (multiModuleProject && project1.getName().equals(extension.getMultiModuleApiSubproject())) {
                dependencies.add(implConfig, Constants.SCREAMING_LIB_GROUP_ID + ":api-utils:" + extension.getVersion());
                return;
            }
            if (multiModuleProject && extension.getMultiModuleUniversalSubproject().equals(project1.getName())) {
                dependencies.add(implConfig, project1.project(":" + extension.getMultiModuleCommonSubproject()));
                for (var pr : extension.getMultiModuleConfiguration().keySet()) {
                    dependencies.add(implConfig, project1.project(":" + pr));
                }
                relocate(project1, extension);
                return;
            }

            if (extension.getPlatforms().stream().allMatch(s -> s.equals("bungee") || s.equals("velocity"))) {
                // Proxy
                if (multiModuleProject) {
                    if (extension.getMultiModuleCommonSubproject().equals(project1.getName())) {
                        dependencies.add(implConfig, Constants.SCREAMING_LIB_GROUP_ID + ":proxy-common:" + extension.getVersion());
                        if (extension.getMultiModuleApiSubproject() != null) {
                            dependencies.add(implConfig, project1.project(":" + extension.getMultiModuleApiSubproject()));
                        }
                    } else if (extension.getMultiModuleConfiguration().containsKey(project1.getName())) {
                        var platform = extension.getMultiModuleConfiguration().get(project1.getName());
                        if (!extension.getPlatforms().contains(platform)) {
                            throw new UnsupportedOperationException("Malformed multi module project configuration: Platform " + platform + " is not configured, but is in multiModuleConfiguration map!");
                        }
                        dependencies.add(implConfig, project1.project(":" + extension.getMultiModuleCommonSubproject()));
                        dependencies.add(implConfig, Constants.SCREAMING_LIB_GROUP_ID + ":proxy-" + platform + ":" + extension.getVersion());
                    } else {
                        throw new UnsupportedOperationException("Can't determine what is this subproject for: " + project1.getName());
                    }
                } else {
                    dependencies.add(implConfig, Constants.SCREAMING_LIB_GROUP_ID + ":proxy-common:" + extension.getVersion());
                    extension.getPlatforms().forEach(s -> {
                        dependencies.add(implConfig, Constants.SCREAMING_LIB_GROUP_ID + ":proxy-" + s + ":" + extension.getVersion());
                    });
                }
            } else if (extension.getPlatforms().stream().noneMatch(s -> s.equals("bungee") || s.equals("velocity"))) {
                // Core
                if (multiModuleProject) {
                    if (extension.getMultiModuleCommonSubproject().equals(project1.getName())) {
                        dependencies.add(implConfig, Constants.SCREAMING_LIB_GROUP_ID + ":core-common:" + extension.getVersion());
                        if (extension.getMultiModuleApiSubproject() != null) {
                            dependencies.add(implConfig, project1.project(":" + extension.getMultiModuleApiSubproject()));
                        }
                    } else if (extension.getMultiModuleConfiguration().containsKey(project1.getName())) {
                        var platform = extension.getMultiModuleConfiguration().get(project1.getName());
                        if (!extension.getPlatforms().contains(platform)) {
                            throw new UnsupportedOperationException("Malformed multi module project configuration: Platform " + platform + " is not configured, but is in multiModuleConfiguration map!");
                        }
                        dependencies.add(implConfig, project1.project(":" + extension.getMultiModuleCommonSubproject()));
                        dependencies.add(implConfig, Constants.SCREAMING_LIB_GROUP_ID + ":core-" + platform + ":" + extension.getVersion());
                    } else {
                        throw new UnsupportedOperationException("Can't determine what is this subproject for: " + project1.getName());
                    }
                } else {
                    dependencies.add(implConfig, Constants.SCREAMING_LIB_GROUP_ID + ":core-common:" + extension.getVersion());
                    extension.getPlatforms().forEach(s -> {
                        dependencies.add(implConfig, Constants.SCREAMING_LIB_GROUP_ID + ":core-" + s + ":" + extension.getVersion());
                    });
                }
            } else {
                throw new UnsupportedOperationException("Can't mix Proxy and Core modules together! Please create separated projects or subprojects for proxy and for core!");
            }

            if (multiModuleProject) {
                if (extension.getMultiModuleCommonSubproject().equals(project1.getName())) {
                    extension.getAdditionalContent().forEach(additionalContent ->
                            additionalContent.applyMultiModule(implConfig, dependencies, extension.getVersion(), "common")
                    );
                } else if (extension.getMultiModuleConfiguration().containsKey(project1.getName())) {
                    var platform = extension.getMultiModuleConfiguration().get(project1.getName());
                    if (!extension.getPlatforms().contains(platform)) {
                        throw new UnsupportedOperationException("Malformed multi module project configuration: Platform " + platform + " is not configured, but is in multiModuleConfiguration map!");
                    }
                    extension.getAdditionalContent().forEach(additionalContent ->
                            additionalContent.applyMultiModule(implConfig, dependencies, extension.getVersion(), platform)
                    );
                } else {
                    throw new UnsupportedOperationException("Can't determine what is this subproject for: " + project1.getName());
                }
            } else {
                extension.getAdditionalContent().forEach(additionalContent ->
                        additionalContent.apply(implConfig, dependencies, extension.getVersion(), extension.getPlatforms())
                );
            }
            if (!extension.isDisableAnnotationProcessor()) {
                if (project.getPlugins().hasPlugin("kotlin-kapt")) {
                    dependencies.add(Constants.KAPT, Constants.SCREAMING_LIB_GROUP_ID + ":annotation:" + extension.getVersion());
                } else {
                    dependencies.add(Constants.ANNOTATION_PROCESSOR, Constants.SCREAMING_LIB_GROUP_ID + ":annotation:" + extension.getVersion());
                }
                if (multiModuleProject) {
                    var compileJava = project1.getTasks().withType(JavaCompile.class).getByName("compileJava");
                    var file = project.project(":" + extension.getMultiModuleCommonSubproject()).getBuildDir().toPath().resolve("slib/pluginName.txt").toAbsolutePath().toString();
                    if (extension.getMultiModuleCommonSubproject().equals(project1.getName())) {
                        compileJava.getOptions().getCompilerArgs().add("-AlookForPluginAndSaveFullClassNameTo=" + file);
                    } else {
                        compileJava.getOptions().getCompilerArgs().add("-AusePluginClassFrom=" + file);
                    }
                }
            }

            /**
             * This allows us to build the final product without depending on Bukkit api.
             */
            if (!extension.isDisableCompilerTricks()
                    && (multiModuleProject ? "bukkit".equals(extension.getMultiModuleConfiguration().get(project1.getName())) : extension.getPlatforms().contains("bukkit"))) {
                // TODO: check if there's no bukkit in classpath
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

            if (!extension.isDisableRelocate() && !multiModuleProject) {
                relocate(project1, extension);
            }
        });
    }

    private void relocate(Project project1, SLibExtension extension) {
        var path = extension.getCustomRelocatePath() != null ? extension.getCustomRelocatePath() : (project1.getGroup() + ".lib");
        var shadowJar = project1.getTasks().withType(ShadowJar.class).getByName("shadowJar");
        if (extension.getMultiModuleApiSubprojectApiUtilsWrapperRelocation() != null) {
            shadowJar.relocate("org.screamingsandals.lib.utils.Wrapper", extension.getMultiModuleApiSubprojectApiUtilsWrapperRelocation());
        }
        shadowJar.relocate("org.screamingsandals.lib", path);
        if (extension.getAdditionalContent().stream().anyMatch(additionalContent -> additionalContent instanceof ThirdPartyModule && ((ThirdPartyModule) additionalContent).getGroupId().equals(Constants.SIMPLE_INVENTORIES_GROUP_ID))) {
            shadowJar.relocate("org.screamingsandals.simpleinventories", path + ".inventories");
        }
    }
}