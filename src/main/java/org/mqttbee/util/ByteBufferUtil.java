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

package org.mqttbee.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.Optional;

/**
 * @author Silvio Giebl
 */
public class ByteBufferUtil {

    @NotNull
    public static ByteBuffer allocate(final int capacity, final boolean direct) {
        return direct ? ByteBuffer.allocateDirect(capacity) : ByteBuffer.allocate(capacity);
    }

    @Nullable
    public static ByteBuffer wrap(@Nullable final byte[] binary) {
        return (binary == null) ? null : ByteBuffer.wrap(binary);
    }

    @Nullable
    public static ByteBuffer slice(@Nullable final ByteBuffer byteBuffer) {
        return (byteBuffer == null) ? null : byteBuffer.slice();
    }

    @NotNull
    public static Optional<ByteBuffer> optionalReadOnly(@Nullable final ByteBuffer byteBuffer) {
        if (byteBuffer == null) {
            return Optional.empty();
        }
        final ByteBuffer readOnlyBuffer = byteBuffer.asReadOnlyBuffer();
        readOnlyBuffer.clear();
        return Optional.of(readOnlyBuffer);
    }

    @NotNull
    public static byte[] getBytes(@NotNull final ByteBuffer byteBuffer) {
        final byte[] binary = new byte[byteBuffer.remaining()];
        byteBuffer.get(binary).position(0);
        return binary;
    }

}
