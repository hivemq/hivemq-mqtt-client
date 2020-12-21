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

import java.nio.ByteBuffer;
import java.util.Optional;

/**
 * @author Silvio Giebl
 */
public final class ByteBufferUtil {

    private static final byte @NotNull [] EMPTY_BYTE_ARRAY = new byte[0];

    public static @NotNull ByteBuffer allocate(final int capacity, final boolean direct) {
        return direct ? ByteBuffer.allocateDirect(capacity) : ByteBuffer.allocate(capacity);
    }

    public static @Nullable ByteBuffer wrap(final byte @Nullable [] binary) {
        return (binary == null) ? null : ByteBuffer.wrap(binary);
    }

    public static @Nullable ByteBuffer slice(final @Nullable ByteBuffer byteBuffer) {
        return (byteBuffer == null) ? null : byteBuffer.slice();
    }

    public static @NotNull Optional<ByteBuffer> optionalReadOnly(final @Nullable ByteBuffer byteBuffer) {
        return (byteBuffer == null) ? Optional.empty() : Optional.of(byteBuffer.asReadOnlyBuffer());
    }

    public static byte @NotNull [] getBytes(final @NotNull ByteBuffer byteBuffer) {
        if (byteBuffer.hasArray()) {
            final byte[] array = byteBuffer.array();
            if ((byteBuffer.arrayOffset() == 0) && (array.length == byteBuffer.remaining())) {
                return array;
            }
        }
        return copyBytes(byteBuffer);
    }

    public static byte @NotNull [] copyBytes(final @Nullable ByteBuffer byteBuffer) {
        if (byteBuffer == null) {
            return EMPTY_BYTE_ARRAY;
        }
        final byte[] binary = new byte[byteBuffer.remaining()];
        byteBuffer.duplicate().get(binary);
        return binary;
    }

    private ByteBufferUtil() {}
}
