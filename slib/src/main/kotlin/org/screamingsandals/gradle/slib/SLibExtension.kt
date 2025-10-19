/*
 * Copyright 2025 ScreamingSandals
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
package org.screamingsandals.gradle.slib

import org.gradle.api.Action
import org.gradle.api.provider.Provider
import org.jetbrains.annotations.ApiStatus

open class SLibExtension {
    /**
     * ScreamingLib version
     */
    var version: String? = null
        private set

    /**
     * Custom Relocate Path (default to the group id)
     */
    var customRelocatePath: String? = null

    /**
     * Name of the common subproject. If null, this is not a multi module project.
     */
    var multiModuleCommonSubproject: String? = null

    /**
     * Multi module configuration. If null, this is not a multi module project.
     *
     * Key - Subproject name
     * Value - Platform name
     */
    var multiModuleConfiguration: MutableMap<String, String>? = null

    /**
     * Name of the universal subproject (containing the final jar). It is not required if you do not need
     */
    var multiModuleUniversalSubproject: String? = null

    /**
     * Name of the api subproject. This subproject is optional.
     */
    var multiModuleApiSubproject: String? = null

    /**
     * Package name the wrapper class will be relocated to.
     */
    var multiModuleApiSubprojectApiUtilsWrapperRelocation: String? = null

    /**
     * Whether `api` configuration should be used instead of `implementation` (sometimes it's better for gradle)
     */
    var isUseApiConfigurationInsteadOfImplementation: Boolean = false

    /**
     * Supported platforms
     */
    val platforms: MutableList<String> = mutableListOf()
    val additionalContent: MutableList<AdditionalContent> = mutableListOf()

    /**
     * Disables automatic relocation.
     *
     * Note: This can break some functionality.
     */
    @ApiStatus.Experimental
    var isDisableRelocate: Boolean = false

    /**
     * Disables compiler tricks.
     *
     * Note: This can break some functionality.
     */
    @ApiStatus.Experimental
    var isDisableCompilerTricks: Boolean = false

    /**
     * Disables annotation processor.
     *
     * Note: If this is disabled, your plugin won't work.
     */
    @ApiStatus.Experimental
    var isDisableAnnotationProcessor: Boolean = false

    /**
     * Disables kapt.
     *
     * Note: If this is disabled, and the project use Kotlin,
     * you should apply the kapt plugin by yourself.
     * The plugin shouldn't be applied before this one.
     */
    @ApiStatus.Experimental
    var isDisableAutoKaptApplicationForKotlin: Boolean = false

    /**
     * Disables kotlin-sam-with-receiver configuration.
     *
     * Note: This can break some functionality.
     */
    @ApiStatus.Experimental
    var isDisableAutoSAMWithReceiverConfigurationForKotlin: Boolean = false

    fun setVersion(version: String) {
        this.version = version
    }

    fun setVersion(version: Provider<String>) {
        this.version = version.get()
    }

    /**
     * Sets the ScreamingLib version and version for all internal modules
     *
     * @param version ScreamingLib version
     */
    fun version(version: String) {
        this.version = version
    }

    /**
     * Sets the ScreamingLib version and version for all internal modules
     *
     * @param version ScreamingLib version
     */
    fun version(version: Provider<String>) {
        this.version = version.get()
    }


    /**
     * Sets the Custom relocate path. If the parameter is null, it defaults to your group id.
     *
     * @param customRelocatePath new custom relocate path or null
     */
    fun customRelocatePath(customRelocatePath: String?) {
        this.customRelocatePath = customRelocatePath
    }

    /**
     * Sets the supported platforms. Note that you can't mix proxy platforms with non-proxy platforms.
     *
     * @param platforms array of supported platforms
     */
    fun platforms(vararg platforms: String) {
        this.platforms.addAll(listOf(*platforms))
    }

    /**
     * Multi module configuration. If null, this is not a multi module project.
     *
     * Key - Subproject name
     * Value - Platform name
     *
     * @param subprojectsDefinition map with subprojects
     */
    fun multiModuleConfiguration(subprojectsDefinition: MutableMap<String, String>?) {
        this.multiModuleConfiguration = subprojectsDefinition
    }

    /**
     * Sets the name of the common subproject. If null, this is not a multi module project.
     *
     * @param multiModuleCommonSubproject common subproject name
     */
    fun multiModuleCommonSubproject(multiModuleCommonSubproject: String?) {
        this.multiModuleCommonSubproject = multiModuleCommonSubproject
    }

    /**
     * Sets the name of the universal subproject (containing the final jar). If null, this is not a multi module project.
     *
     * @param multiModuleUniversalSubproject universal subproject name
     */
    fun multiModuleUniversalSubproject(multiModuleUniversalSubproject: String?) {
        this.multiModuleUniversalSubproject = multiModuleUniversalSubproject
    }

    /**
     * Sets the supported platforms. Note that you can't mix proxy platforms with non-proxy platforms.
     * This will automatically set common subproject name (assuming that it has word `common` instead of platform in its name).
     * This will also automatically resolve all platforms subprojects based on the specified template.
     * And this will also automatically set universal subproject name (assuming that it has word `universal` instead of platform in its name).
     * Don't call [.multiModuleConfiguration] and/or [.multiModuleCommonSubproject] if you use this method.
     *
     * @param template template which will be used to generate subproject names. %s means platform name (eg. plugin-%s). Note that subproject plugin-common should exist.
     * @param platforms array of supported platforms
     */
    fun multiModulePlatforms(template: String, vararg platforms: String) {
        this.platforms.addAll(listOf(*platforms))
        multiModuleConfiguration = mutableMapOf()
        for (platform in platforms) {
            multiModuleConfiguration!![String.format(template, platform)] = platform
        }
        multiModuleCommonSubproject = String.format(template, "common")
        multiModuleUniversalSubproject = String.format(template, "universal")
    }

    /**
     * Sets the name of the api subproject. This subproject is optional.
     *
     * @param api api subproject name
     */
    fun multiModuleApiSubproject(api: String) {
        this.multiModuleApiSubproject = api
    }

    /**
     * Sets the name of the api subproject. This subproject is optional. Also sets the package name Wrapper class and other slib api classes will be relocated to.
     *
     * @param api api subproject name
     * @param slibApiPackage the new slib api package
     */
    fun multiModuleApiSubproject(api: String, slibApiPackage: String? = null) {
        this.multiModuleApiSubproject = api
        this.multiModuleApiSubprojectApiUtilsWrapperRelocation = slibApiPackage
    }

    /**
     * Whether `api` configuration should be used instead of `implementation` (sometimes it's better for gradle)
     *
     * @param useApiConfigurationInsteadOfImplementation true if `api` should be used
     */
    fun useApiConfigurationInsteadOfImplementation(useApiConfigurationInsteadOfImplementation: Boolean) {
        this.isUseApiConfigurationInsteadOfImplementation = useApiConfigurationInsteadOfImplementation
    }

    /**
     * Allows you to add additional content, like optional module, simple inventories, or 3rd party library.
     *
     * @param consumer additional content builder
     */
    fun additionalContent(consumer: Action<AdditionalContentBuilder>) {
        val builder = AdditionalContentBuilder(additionalContent)
        consumer.execute(builder)
    }

    override fun toString(): String {
        return ("SLibExtension(version=" + this.version
                + ", customRelocatePath=" + this.customRelocatePath
                + ", multiModuleCommonSubproject=" + this.multiModuleCommonSubproject
                + ", multiModuleConfiguration=" + this.multiModuleConfiguration
                + ", multiModuleUniversalSubproject=" + this.multiModuleUniversalSubproject
                + ", multiModuleApiSubproject=" + this.multiModuleApiSubproject
                + ", multiModuleApiSubprojectApiUtilsWrapperRelocation=" + this.multiModuleApiSubprojectApiUtilsWrapperRelocation
                + ", useApiConfigurationInsteadOfImplementation=" + this.isUseApiConfigurationInsteadOfImplementation
                + ", platforms=" + this.platforms
                + ", additionalContent=" + this.additionalContent
                + ", disableRelocate=" + this.isDisableRelocate
                + ", disableCompilerTricks=" + this.isDisableCompilerTricks
                + ", disableAnnotationProcessor=" + this.isDisableAnnotationProcessor
                + ", disableAutoKaptApplicationForKotlin=" + this.isDisableAutoKaptApplicationForKotlin
                + ", disableAutoSAMWithReceiverConfigurationForKotlin=" + this.isDisableAutoSAMWithReceiverConfigurationForKotlin + ")")
    }
}
