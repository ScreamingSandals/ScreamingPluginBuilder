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
import org.jetbrains.annotations.ApiStatus

class AdditionalContentBuilder(
    @field:ApiStatus.Internal val additionalContent: MutableList<AdditionalContent>,
) {
    /**
     * Adds optional modules of ScreamingLib.
     *
     * @param modules slib optional modules
     */
    fun module(vararg modules: String) {
        for (module in modules) {
            additionalContent.add(SLibModule(module))
        }
    }

    /**
     * Adds ScreamingLib lang module
     */
    fun lang() {
        additionalContent.add(SLibSingleModule("lang"))
    }

    /**
     * Adds optional single modules of ScreamingLib.
     * Unlike normal modules, these modules consist of just one artifact.
     *
     * Note: Except `lang`, there are currently no modules that can be added using this method.
     * For adding lang use its own method [.lang]
     *
     * @param modules slib optional single modules
     * @see .module
     * @see .lang
     */
    fun singleModule(vararg modules: String) {
        for (module in modules) {
            additionalContent.add(SLibSingleModule(module))
        }
    }

    /**
     * Adds third party module to screaming lib.
     *
     * @param thirdPartyModuleConsumer third party module builder
     */
    fun thirdParty(thirdPartyModuleConsumer: Action<ThirdPartyModule>) {
        val module = ThirdPartyModule()
        thirdPartyModuleConsumer.execute(module)
        additionalContent.add(module)
    }

    /**
     * Adds Simple Inventories to your project.
     * Despite this method requires third party module consumer, group id and module are already set.
     * The only property you should specify is the version.
     *
     * @param thirdPartyModuleConsumer third party module builder
     */
    fun simpleInventories(thirdPartyModuleConsumer: Action<ThirdPartyModule>) {
        val module = ThirdPartyModule()
        module.groupId = SIMPLE_INVENTORIES_GROUP_ID
        module.module = SIMPLE_INVENTORIES_MODULE_NAME
        thirdPartyModuleConsumer.execute(module)
        additionalContent.add(module)
    }

    override fun toString(): String {
        return "AdditionalContentBuilder(additionalContent=" + this.additionalContent + ")"
    }
}
