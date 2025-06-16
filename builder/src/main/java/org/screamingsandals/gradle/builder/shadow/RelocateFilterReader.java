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

package org.screamingsandals.gradle.builder.shadow;

import com.github.jengelman.gradle.plugins.shadow.relocation.Relocator;
import org.gradle.api.provider.SetProperty;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;
import java.util.stream.Collectors;

@ApiStatus.Internal
public class RelocateFilterReader extends FilterReader {
    public SetProperty<Relocator> relocators;
    private final @NotNull BufferedReader bufferedReader;
    private @Nullable String relocatedContent = null;
    private int nextChar = 0;

    public RelocateFilterReader(@NotNull Reader in) {
        super(in);
        this.bufferedReader = new BufferedReader(in);
    }

    private void relocateIfNeeded() {
        if (relocatedContent != null) {
            return;
        }

        // Read entire source file
        String original = bufferedReader.lines().collect(Collectors.joining("\n"));
        String modified = original;
        for (Relocator relocator : relocators.get()) {
            modified = relocator.applyToSourceContent(modified);
        }
        relocatedContent = modified;
    }

    @Override
    public int read(char @NotNull [] cbuf, int off, int len) throws IOException {
        relocateIfNeeded();
        if (nextChar >= relocatedContent.length()) return -1;

        int count = Math.min(len, relocatedContent.length() - nextChar);
        relocatedContent.getChars(nextChar, nextChar + count, cbuf, off);
        nextChar += count;
        return count;
    }
}
