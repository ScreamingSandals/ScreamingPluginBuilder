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

package org.screamingsandals.gradle.builder;

import org.jetbrains.annotations.NotNull;

public final class Constants {
    private Constants() {
    }

    public static final @NotNull String SOURCES_JAR_TASK_NAME = "sourcesJar";
    public static final @NotNull String JAVADOC_JAR_TASK_NAME = "javadocJar";
    public static final @NotNull String JAVADOC_TASK_NAME = "javadoc";

    public static final @NotNull String NEXUS_URL_SNAPSHOT_PROPERTY = "NEXUS_URL_SNAPSHOT";
    public static final @NotNull String NEXUS_URL_RELEASE_PROPERTY = "NEXUS_URL_RELEASE";
    public static final @NotNull String NEXUS_USERNAME_PROPERTY = "NEXUS_USERNAME";
    public static final @NotNull String NEXUS_PASSWORD_PROPERTY = "NEXUS_PASSWORD";
}
