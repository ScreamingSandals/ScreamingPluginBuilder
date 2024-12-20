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
import org.gradle.api.Action;
import org.gradle.api.provider.Provider;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SLibExtension {
    /**
     * ScreamingLib version
     */
    private @Nullable String version;
    /**
     * Custom Relocate Path (default to the group id)
     */
    private @Nullable String customRelocatePath;
    /**
     * Name of the common subproject. If null, this is not a multi module project.
     */
    private @Nullable String multiModuleCommonSubproject;
    /**
     * Multi module configuration. If null, this is not a multi module project.
     * <p>
     * Key - Subproject name
     * Value - Platform name
     */
    private @Nullable Map<@NotNull String, @NotNull String> multiModuleConfiguration;
    /**
     * Name of the universal subproject (containing the final jar). If null, this is not a multi module project.
     */
    private @Nullable String multiModuleUniversalSubproject;
    /**
     * Name of the api subproject. This subproject is optional.
     */
    private @Nullable String multiModuleApiSubproject;
    /**
     * Package name the wrapper class will be relocated to.
     */
    private @Nullable String multiModuleApiSubprojectApiUtilsWrapperRelocation;
    /**
     * Whether `api` configuration should be used instead of `implementation` (sometimes it's better for gradle)
     */
    private boolean useApiConfigurationInsteadOfImplementation;
    /**
     * Supported platforms
     */
    private final @NotNull List<@NotNull String> platforms = new ArrayList<>();
    private final @NotNull List<@NotNull AdditionalContent> additionalContent = new ArrayList<>();

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

    public @Nullable String getVersion() {
        return this.version;
    }

    public @Nullable String getCustomRelocatePath() {
        return this.customRelocatePath;
    }

    public @Nullable String getMultiModuleCommonSubproject() {
        return this.multiModuleCommonSubproject;
    }

    public @Nullable Map<String, String> getMultiModuleConfiguration() {
        return this.multiModuleConfiguration;
    }

    public @Nullable String getMultiModuleUniversalSubproject() {
        return this.multiModuleUniversalSubproject;
    }

    public @Nullable String getMultiModuleApiSubproject() {
        return this.multiModuleApiSubproject;
    }

    public @Nullable String getMultiModuleApiSubprojectApiUtilsWrapperRelocation() {
        return this.multiModuleApiSubprojectApiUtilsWrapperRelocation;
    }

    public boolean isUseApiConfigurationInsteadOfImplementation() {
        return this.useApiConfigurationInsteadOfImplementation;
    }

    public @NotNull List<@NotNull String> getPlatforms() {
        return this.platforms;
    }

    public @NotNull List<@NotNull AdditionalContent> getAdditionalContent() {
        return this.additionalContent;
    }

    public boolean isDisableRelocate() {
        return this.disableRelocate;
    }

    public boolean isDisableCompilerTricks() {
        return this.disableCompilerTricks;
    }

    public boolean isDisableAnnotationProcessor() {
        return this.disableAnnotationProcessor;
    }

    public boolean isDisableAutoKaptApplicationForKotlin() {
        return this.disableAutoKaptApplicationForKotlin;
    }

    public boolean isDisableAutoSAMWithReceiverConfigurationForKotlin() {
        return this.disableAutoSAMWithReceiverConfigurationForKotlin;
    }

    public void setCustomRelocatePath(@Nullable String customRelocatePath) {
        this.customRelocatePath = customRelocatePath;
    }

    public void setMultiModuleCommonSubproject(@Nullable String multiModuleCommonSubproject) {
        this.multiModuleCommonSubproject = multiModuleCommonSubproject;
    }

    public void setMultiModuleConfiguration(@Nullable Map<@NotNull String, @NotNull String> multiModuleConfiguration) {
        this.multiModuleConfiguration = multiModuleConfiguration;
    }

    public void setMultiModuleUniversalSubproject(@Nullable String multiModuleUniversalSubproject) {
        this.multiModuleUniversalSubproject = multiModuleUniversalSubproject;
    }

    public void setMultiModuleApiSubproject(@Nullable String multiModuleApiSubproject) {
        this.multiModuleApiSubproject = multiModuleApiSubproject;
    }

    public void setMultiModuleApiSubprojectApiUtilsWrapperRelocation(@Nullable String multiModuleApiSubprojectApiUtilsWrapperRelocation) {
        this.multiModuleApiSubprojectApiUtilsWrapperRelocation = multiModuleApiSubprojectApiUtilsWrapperRelocation;
    }

    public void setUseApiConfigurationInsteadOfImplementation(boolean useApiConfigurationInsteadOfImplementation) {
        this.useApiConfigurationInsteadOfImplementation = useApiConfigurationInsteadOfImplementation;
    }

    public void setDisableRelocate(boolean disableRelocate) {
        this.disableRelocate = disableRelocate;
    }

    public void setDisableCompilerTricks(boolean disableCompilerTricks) {
        this.disableCompilerTricks = disableCompilerTricks;
    }

    public void setDisableAnnotationProcessor(boolean disableAnnotationProcessor) {
        this.disableAnnotationProcessor = disableAnnotationProcessor;
    }

    public void setDisableAutoKaptApplicationForKotlin(boolean disableAutoKaptApplicationForKotlin) {
        this.disableAutoKaptApplicationForKotlin = disableAutoKaptApplicationForKotlin;
    }

    public void setDisableAutoSAMWithReceiverConfigurationForKotlin(boolean disableAutoSAMWithReceiverConfigurationForKotlin) {
        this.disableAutoSAMWithReceiverConfigurationForKotlin = disableAutoSAMWithReceiverConfigurationForKotlin;
    }

    public void setVersion(@NotNull String version) {
        this.version = version;
    }

    public @NotNull String toString() {
        return "SLibExtension(version=" + this.version
                + ", customRelocatePath=" + this.customRelocatePath
                + ", multiModuleCommonSubproject=" + this.multiModuleCommonSubproject
                + ", multiModuleConfiguration=" + this.multiModuleConfiguration
                + ", multiModuleUniversalSubproject=" + this.multiModuleUniversalSubproject
                + ", multiModuleApiSubproject=" + this.multiModuleApiSubproject
                + ", multiModuleApiSubprojectApiUtilsWrapperRelocation=" + this.multiModuleApiSubprojectApiUtilsWrapperRelocation
                + ", useApiConfigurationInsteadOfImplementation=" + this.useApiConfigurationInsteadOfImplementation
                + ", platforms=" + this.platforms
                + ", additionalContent=" + this.additionalContent
                + ", disableRelocate=" + this.disableRelocate
                + ", disableCompilerTricks=" + this.disableCompilerTricks
                + ", disableAnnotationProcessor=" + this.disableAnnotationProcessor
                + ", disableAutoKaptApplicationForKotlin=" + this.disableAutoKaptApplicationForKotlin
                + ", disableAutoSAMWithReceiverConfigurationForKotlin=" + this.disableAutoSAMWithReceiverConfigurationForKotlin + ")";
    }
}
