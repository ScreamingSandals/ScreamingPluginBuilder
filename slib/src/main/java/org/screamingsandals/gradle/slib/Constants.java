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
import org.jetbrains.annotations.NotNull;

@UtilityClass
public class Constants {
    public final @NotNull String SCREAMING_LIB_GROUP_ID = "org.screamingsandals.lib";
    public final @NotNull String SIMPLE_INVENTORIES_GROUP_ID = "org.screamingsandals.simpleinventories";
    public final @NotNull String SIMPLE_INVENTORIES_MODULE_NAME = "core";

    public final @NotNull String SANDALS_REPO_NAME = "sandals-repo";
    public final @NotNull String SANDALS_REPO_URL = "https://repo.screamingsandals.org/public/";

    public final @NotNull String IMPLEMENTATION_CONFIGURATION = "implementation";
    public final @NotNull String API_CONFIGURATION = "api";
    public final @NotNull String ANNOTATION_PROCESSOR = "annotationProcessor";
    public final @NotNull String KAPT = "kapt";

    public final @NotNull String PAPER_API_GROUP = "io.papermc.paper";
    public final @NotNull String PAPER_API = "paper-api";
    public final @NotNull String PAPER_DESTROYSTOKYO_API_GROUP = "com.destroystokyo.paper";
    public final @NotNull String PAPER_DESTROYSTOKYO_API = "paper-api";
    public final @NotNull String PAPERSPIGOT_API_GROUP = "org.github.paperspigot";
    public final @NotNull String PAPERSPIGOT_API = "paperspigot-api";
    public final @NotNull String SPIGOT_API_GROUP = "org.spigotmc";
    public final @NotNull String SPIGOT_API = "spigot-api";
    public final @NotNull String BUKKIT_GROUP = "org.bukkit";
    public final @NotNull String BUKKIT_API = "bukkit";
}
