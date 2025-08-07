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

package org.screamingsandals.gradle.builder.shadow

import com.github.jengelman.gradle.plugins.shadow.relocation.Relocator
import org.gradle.api.provider.SetProperty
import org.jetbrains.annotations.ApiStatus
import java.io.BufferedReader
import java.io.FilterReader
import java.io.IOException
import java.io.Reader
import java.util.stream.Collectors
import kotlin.math.min

@ApiStatus.Internal
class RelocateFilterReader(input: Reader) : FilterReader(input) {
    var relocators: SetProperty<Relocator>? = null
    private val bufferedReader: BufferedReader = BufferedReader(input)
    private var relocatedContent: String? = null
    private var nextChar = 0

    private fun relocateIfNeeded() {
        if (relocatedContent != null) {
            return
        }

        // Read entire source file
        val original = bufferedReader.lines().collect(Collectors.joining("\n"))
        var modified = original
        for (relocator in relocators!!.get()) {
            modified = relocator.applyToSourceContent(modified)
        }
        relocatedContent = modified
    }

    @Throws(IOException::class)
    override fun read(cbuf: CharArray, off: Int, len: Int): Int {
        relocateIfNeeded()
        if (nextChar >= relocatedContent!!.length) {
            return -1
        }

        val count = min(len, relocatedContent!!.length - nextChar)
        relocatedContent!!.toCharArray(cbuf, off, nextChar, nextChar + count)
        nextChar += count
        return count
    }
}
