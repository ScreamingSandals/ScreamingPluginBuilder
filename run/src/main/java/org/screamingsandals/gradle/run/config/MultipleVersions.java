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

package org.screamingsandals.gradle.run.config;

import lombok.RequiredArgsConstructor;
import org.gradle.api.Action;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@RequiredArgsConstructor
public class MultipleVersions implements Iterable<@NotNull Version> {
    private final @NotNull List<@NotNull Version> versions;

    public void args(@NotNull String @NotNull... args) {
        for (var version : versions) {
            version.args(args);
        }
    }

    public void args(@NotNull List<@NotNull String> args) {
        for (var version : versions) {
            version.args(args);
        }
    }

    public void jvmArgs(@NotNull String @NotNull... jvmArgs) {
        for (var version : versions) {
            version.jvmArgs(jvmArgs);
        }
    }

    public void jvmArgs(@NotNull List<@NotNull String> jvmArgs) {
        for (var version : versions) {
            version.jvmArgs(jvmArgs);
        }
    }

    public void serverProperties(@NotNull Action<@NotNull ServerProperties> callback) {
        callback.execute(new MultipleServerProperties(versions));
    }

    @Override
    public @NotNull Iterator<@NotNull Version> iterator() {
        return Collections.unmodifiableCollection(versions).iterator();
    }
}
