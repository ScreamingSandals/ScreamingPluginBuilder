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

import lombok.experimental.UtilityClass;

@UtilityClass
public class Constants {
    public final String SCREAMING_LIB_GROUP_ID = "org.screamingsandals.lib";
    public final String SIMPLE_INVENTORIES_GROUP_ID = "org.screamingsandals.simpleinventories";
    public final String SIMPLE_INVENTORIES_MODULE_NAME = "core";

    public final String SANDALS_REPO_NAME = "sandals-repo";
    public final String SANDALS_REPO_URL = "https://repo.screamingsandals.org/public/";

    public final String PAPER_REPO_NAME = "paper-repo";
    public final String PAPER_REPO_URL = "https://repo.papermc.io/repository/maven-public/";

    public final String IMPLEMENTATION_CONFIGURATION = "implementation";
    public final String API_CONFIGURATION = "api";
    public final String ANNOTATION_PROCESSOR = "annotationProcessor";
    public final String KAPT = "kapt";
}
