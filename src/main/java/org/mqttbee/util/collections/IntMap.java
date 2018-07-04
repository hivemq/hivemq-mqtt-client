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

package org.mqttbee.util.collections;

import javax.annotation.concurrent.NotThreadSafe;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;

/** @author Silvio Giebl */
@NotThreadSafe
public class IntMap<E> {

    private final int minKey;
    private final int capacity;
    private final int chunkShift;
    private final int chunkMask;
    private final int hashMask;

    private final Chunk[] chunks;

    private Chunk freeChunk;

    public IntMap(final int minKey, final int maxKey) {
        this.minKey = minKey;
        final int pow2Bit = 32 - Integer.numberOfLeadingZeros(maxKey - minKey);
        capacity = 1 << pow2Bit;
        chunkShift = (pow2Bit + 1) >> 1;
        hashMask = (1 << chunkShift) - 1;
        chunkMask = capacity - 1 - hashMask;

        chunks = new Chunk[1 << (pow2Bit - chunkShift)];
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public E put(final int key, @NotNull final E value) {
        final int actualKey = checkKey(key);
        final int index = index(actualKey);
        Chunk chunk = chunks[index];
        if (chunk == null) {
            if (freeChunk == null) {
                chunk = new Chunk(hashMask + 1);
            } else {
                chunk = freeChunk;
                freeChunk = null;
            }
            chunks[index] = chunk;
        }
        return (E) chunk.put(actualKey, hashMask, value);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public E get(final int key) {
        final int actualKey = checkKey(key);
        final Chunk chunk = chunks[index(actualKey)];
        if (chunk == null) {
            return null;
        }
        return (E) chunk.get(actualKey, hashMask);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public E remove(final int key) {
        final int actualKey = checkKey(key);
        final int index = index(actualKey);
        final Chunk chunk = chunks[index];
        if (chunk == null) {
            return null;
        }
        final E value = (E) chunk.remove(actualKey, hashMask);
        if (chunk.count == 0) {
            freeChunk = chunk;
            chunks[index] = null;
        }
        return value;
    }

    private int checkKey(final int key) {
        final int actualKey = key - minKey;
        if (actualKey > capacity - 1 || actualKey < 0) {
            throw new IllegalArgumentException();
        }
        return actualKey;
    }

    private int index(final int key) {
        return (key & chunkMask) >> chunkShift;
    }

    private static class Chunk {

        private final Object[] values;
        private int count;

        private Chunk(final int size) {
            this.values = new Object[size];
        }

        @Nullable
        Object put(final int key, final int hashMask, @NotNull final Object value) {
            final int index = index(key, hashMask);
            final Object previousValue = values[index];
            values[index] = value;
            if (previousValue == null) {
                count++;
            }
            return previousValue;
        }

        @Nullable
        Object get(final int key, final int hashMask) {
            return values[index(key, hashMask)];
        }

        @Nullable
        Object remove(final int key, final int hashMask) {
            final int index = index(key, hashMask);
            final Object previousValue = values[index];
            values[index] = null;
            if (previousValue != null) {
                count--;
            }
            return previousValue;
        }

        private int index(final int key, final int hashMask) {
            return key & hashMask;
        }
    }
}
