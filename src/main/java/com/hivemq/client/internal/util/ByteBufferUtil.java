/*
 * Copyright 2018 The MQTT Bee project
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
 *
 */

package com.hivemq.client.internal.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.Optional;

/**
 * @author Silvio Giebl
 */
public class ByteBufferUtil {

    private ByteBufferUtil() {}

    public static @NotNull ByteBuffer allocate(final int capacity, final boolean direct) {
        return direct ? ByteBuffer.allocateDirect(capacity) : ByteBuffer.allocate(capacity);
    }

    public static @Nullable ByteBuffer wrap(final @Nullable byte[] binary) {
        return (binary == null) ? null : ByteBuffer.wrap(binary);
    }

    public static @Nullable ByteBuffer slice(final @Nullable ByteBuffer byteBuffer) {
        return (byteBuffer == null) ? null : byteBuffer.slice();
    }

    public static @NotNull Optional<ByteBuffer> optionalReadOnly(final @Nullable ByteBuffer byteBuffer) {
        if (byteBuffer == null) {
            return Optional.empty();
        }
        final ByteBuffer readOnlyBuffer = byteBuffer.asReadOnlyBuffer();
        readOnlyBuffer.clear();
        return Optional.of(readOnlyBuffer);
    }

    public static @NotNull byte[] getBytes(final @NotNull ByteBuffer byteBuffer) {
        final byte[] binary = new byte[byteBuffer.remaining()];
        byteBuffer.get(binary).position(0);
        return binary;
    }
}
