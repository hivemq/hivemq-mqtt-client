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
import org.jetbrains.annotations.Nullable;

/**
 * @author Silvio Giebl
 */
public class ByteArray {

    protected final byte @NotNull [] array;

    public ByteArray(final byte @NotNull [] array) {
        this.array = array;
    }

    public int length() {
        return getEnd() - getStart();
    }

    protected int getStart() {
        return 0;
    }

    protected int getEnd() {
        return array.length;
    }

    @Override
    public final boolean equals(final @Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ByteArray)) {
            return false;
        }
        final ByteArray that = (ByteArray) o;
        return ByteArrayUtil.equals(array, getStart(), getEnd(), that.array, that.getStart(), that.getEnd());
    }

    @Override
    public final int hashCode() {
        return ByteArrayUtil.hashCode(array, getStart(), getEnd());
    }
}
