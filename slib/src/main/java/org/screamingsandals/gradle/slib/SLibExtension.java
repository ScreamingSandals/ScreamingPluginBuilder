/*
 * Copyright 2024 ScreamingSandals
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.screamingsandals.gradle.slib;

import groovy.lang.Closure;
import lombok.Data;
import lombok.Setter;
import lombok.experimental.Tolerate;
import org.gradle.api.Action;
import org.gradle.api.provider.Provider;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@Data
public class SLibExtension {
    /**
     * ScreamingLib version
     */
    @Setter(onParam_ = @NotNull) // don't allow users to set it to null
    private String version;
    /**
     * Custom Relocate Path (default to the group id)
     */
    @Nullable
    private String customRelocatePath;
    /**
     * Name of the common subproject. If null, this is not a multi module project.
     */
    @Nullable
    private String multiModuleCommonSubproject;
    /**
     * Multi module configuration. If null, this is not a multi module project.
     * <p>
     * Key - Subproject name
     * Value - Platform name
     */
    @Nullable
    private Map<String, String> multiModuleConfiguration;
    /**
     * Name of the universal subproject (containing the final jar). If null, this is not a multi module project.
     */
    @Nullable
    private String multiModuleUniversalSubproject;
    /**
     * Name of the api subproject. This subproject is optional.
     */
    @Nullable
    private String multiModuleApiSubproject;
    /**
     * Package name the wrapper class will be relocated to.
     */
    @Nullable
    private String multiModuleApiSubprojectApiUtilsWrapperRelocation;
    /**
     * Whether `api` configuration should be used instead of `implementation` (sometimes it's better for gradle)
     */
    private boolean useApiConfigurationInsteadOfImplementation;
    /**
     * Supported platforms
     */
    private final List<String> platforms = new ArrayList<>();
    private final List<AdditionalContent> additionalContent = new ArrayList<>();

    /**
     * Disables automatic relocation.
     *
     * <p>
     *
     * Note: This can break some functionality.
     *
     */
    @ApiStatus.Experimental
    private boolean disableRelocate;
    /**
     * Disables compiler tricks.
     *
     * <p>
     *
     * Note: This can break some functionality.
     *
     */
    @ApiStatus.Experimental
    private boolean disableCompilerTricks;
    /**
     * Disables annotation processor.
     *
     * <p>
     *
     * Note: If this is disabled, your plugin won't work.
     *
     */
    @ApiStatus.Experimental
    private boolean disableAnnotationProcessor;
    /**
     * Disables kapt.
     *
     * <p>
     *
     * Note: If this is disabled, and the project use Kotlin,
     * you should apply the kapt plugin by yourself.
     * The plugin shouldn't be applied before this one.
     */
    @ApiStatus.Experimental
    private boolean disableAutoKaptApplicationForKotlin;
    /**
     * Disables kotlin-sam-with-receiver configuration.
     *
     * <p>
     *
     * Note: This can break some functionality.
     */
    @ApiStatus.Experimental
    private boolean disableAutoSAMWithReceiverConfigurationForKotlin;

    @Tolerate
    public void setVersion(@NotNull Provider<@NotNull String> version) {
        this.version = version.get();
    }

    /**
     * Sets the ScreamingLib version and version for all internal modules
     *
     * @param version ScreamingLib version
     */
    public void version(@NotNull String version) {
        this.version = version;
    }

    /**
     * Sets the ScreamingLib version and version for all internal modules
     *
     * @param version ScreamingLib version
     */
    public void version(@NotNull Provider<@NotNull String> version) {
        this.version = version.get();
    }


    /**
     * Sets the Custom relocate path. If the parameter is null, it defaults to your group id.
     *
     * @param customRelocatePath new custom relocate path or null
     */
    public void customRelocatePath(@Nullable String customRelocatePath) {
        this.customRelocatePath = customRelocatePath;
    }

    /**
     * Sets the supported platforms. Note that you can't mix proxy platforms with non-proxy platforms.
     *
     * @param platforms array of supported platforms
     */
    public void platforms(@NotNull String @NotNull... platforms) {
        this.platforms.addAll(Arrays.asList(platforms));
    }

    /**
     * Multi module configuration. If null, this is not a multi module project.
     * <p>
     * Key - Subproject name
     * Value - Platform name
     *
     * @param subprojectsDefinition map with subprojects
     */
    public void multiModuleConfiguration(@Nullable Map<@NotNull String, @NotNull String> subprojectsDefinition) {
        this.multiModuleConfiguration = subprojectsDefinition;
    }

    /**
     * Sets the name of the common subproject. If null, this is not a multi module project.
     *
     * @param multiModuleCommonSubproject common subproject name
     */
    public void multiModuleCommonSubproject(@Nullable String multiModuleCommonSubproject) {
        this.multiModuleCommonSubproject = multiModuleCommonSubproject;
    }

    /**
     * Sets the name of the universal subproject (containing the final jar). If null, this is not a multi module project.
     *
     * @param multiModuleUniversalSubproject universal subproject name
     */
    public void multiModuleUniversalSubproject(@Nullable String multiModuleUniversalSubproject) {
        this.multiModuleUniversalSubproject = multiModuleUniversalSubproject;
    }

    /**
     * Sets the supported platforms. Note that you can't mix proxy platforms with non-proxy platforms.
     * This will automatically set common subproject name (assuming that it has word `common` instead of platform in its name).
     * This will also automatically resolve all platforms subprojects based on the specified template.
     * And this will also automatically set universal subproject name (assuming that it has word `universal` instead of platform in its name).
     * Don't call {@link #multiModuleConfiguration(Map)} and/or {@link #multiModuleCommonSubproject(String)} if you use this method.
     *
     * @param template template which will be used to generate subproject names. %s means platform name (eg. plugin-%s). Note that subproject plugin-common should exist.
     * @param platforms array of supported platforms
     */
    public void multiModulePlatforms(@NotNull String template, @NotNull String @NotNull... platforms) {
        this.platforms.addAll(Arrays.asList(platforms));
        multiModuleConfiguration = new HashMap<>();
        for (var platform : platforms) {
            multiModuleConfiguration.put(String.format(template, platform), platform);
        }
        multiModuleCommonSubproject = String.format(template, "common");
        multiModuleUniversalSubproject = String.format(template, "universal");
    }

    /**
     * Sets the name of the api subproject. This subproject is optional.
     *
     * @param api api subproject name
     */
    public void multiModuleApiSubproject(@NotNull String api) {
        this.multiModuleApiSubproject = api;
    }

    /**
     * Sets the name of the api subproject. This subproject is optional. Also sets the package name Wrapper class and other slib api classes will be relocated to.
     *
     * @param api api subproject name
     * @param slibApiPackage the new slib api package
     */
    public void multiModuleApiSubproject(@NotNull String api, @Nullable String slibApiPackage) {
        this.multiModuleApiSubproject = api;
        this.multiModuleApiSubprojectApiUtilsWrapperRelocation = slibApiPackage;
    }

    /**
     * Whether `api` configuration should be used instead of `implementation` (sometimes it's better for gradle)
     *
     * @param useApiConfigurationInsteadOfImplementation true if `api` should be used
     */
    public void useApiConfigurationInsteadOfImplementation(boolean useApiConfigurationInsteadOfImplementation) {
        this.useApiConfigurationInsteadOfImplementation = useApiConfigurationInsteadOfImplementation;
    }

    /**
     * Allows you to add additional content, like optional module, simple inventories, or 3rd party library.
     *
     * @param consumer additional content builder
     */
    public void additionalContent(@NotNull Action<@NotNull AdditionalContentBuilder> consumer) {
        var builder = new AdditionalContentBuilder(additionalContent);
        consumer.execute(builder);
    }

    /**
     * Allows you to add additional content, like optional module, simple inventories, or 3rd party library.
     *
     * @param closure additional content builder
     */
    public void additionalContent(Closure<AdditionalContentBuilder> closure) {
        var builder = new AdditionalContentBuilder(additionalContent);
        closure.setDelegate(builder);
        closure.setResolveStrategy(Closure.DELEGATE_FIRST);
        closure.call(builder);
    }
}
