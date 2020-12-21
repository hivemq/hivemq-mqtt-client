/*
 * Copyright 2018-present HiveMQ and the HiveMQ Community
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hivemq.client2.internal.util;

import org.jetbrains.annotations.NotNull;

/**
 * @author Silvio Giebl
 */
public final class ByteArrayUtil {

    public static boolean equals(
            final byte @NotNull [] array1,
            final int start1,
            final int end1,
            final byte @NotNull [] array2,
            final int start2,
            final int end2) {

        final int length1 = end1 - start1;
        final int length2 = end2 - start2;
        if (length1 != length2) {
            return false;
        }

        if ((array1 == array2) && (start1 == start2)) {
            return true;
        }

        for (int i1 = start1, i2 = start2; i1 < end1; i1++, i2++) {
            if (array1[i1] != array2[i2]) {
                return false;
            }
        }
        return true;
    }

    public static int hashCode(final byte @NotNull [] array, final int start, final int end) {
        int result = 1;
        for (int i = start; i < end; i++) {
            result = 31 * result + array[i];
        }
        return result;
    }

    public static int indexOf(final byte @NotNull [] array, final int start, final byte search) {
        for (int i = start; i < array.length; i++) {
            if (array[i] == search) {
                return i;
            }
        }
        return array.length;
    }

    private ByteArrayUtil() {}
}
