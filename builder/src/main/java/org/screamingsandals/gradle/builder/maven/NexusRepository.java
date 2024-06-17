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

package org.screamingsandals.gradle.builder.maven;

import org.gradle.api.Project;
import org.gradle.api.publish.PublishingExtension;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.gradle.builder.Constants;

public class NexusRepository implements MavenRepository {
    @Override
    public void setup(@NotNull Project project, @NotNull PublishingExtension publishing) {
        project.afterEvaluate(p1 ->
                publishing.repositories(it ->
                        it.maven(repository -> {
                            if (((String) project.getVersion()).contains("SNAPSHOT")) {
                                repository.setUrl(System.getenv(Constants.NEXUS_URL_SNAPSHOT_PROPERTY));
                            } else {
                                repository.setUrl(System.getenv(Constants.NEXUS_URL_RELEASE_PROPERTY));
                            }
                            repository.getCredentials().setUsername(System.getenv(Constants.NEXUS_USERNAME_PROPERTY));
                            repository.getCredentials().setPassword(System.getenv(Constants.NEXUS_PASSWORD_PROPERTY));
                        })
                )
        );
    }
}
