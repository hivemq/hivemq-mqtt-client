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

import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.ArrayDeque;

/**
 * @author Silvio Giebl
 */
@NotThreadSafe
public class IntMap<E> {

    private final int capacity;
    private final int chunkShift;
    private final int chunkMask;
    private final int hashMask;

    private final Chunk<E>[] chunks;
    private final int chunkBatchCount;
    private ChunkBatch<E> chunkBatch;

    @SuppressWarnings("unchecked")
    public IntMap(final int maxKey) {
        final int pow2Bit = 32 - Integer.numberOfLeadingZeros(maxKey - 1);
        capacity = 1 << pow2Bit;
        chunkShift = (pow2Bit + 1) >> 1;
        hashMask = (1 << chunkShift) - 1;
        chunkMask = capacity - 1 - hashMask;

        chunks = new Chunk[(chunkMask >> chunkShift) + 1];
        chunkBatchCount = 1 << (pow2Bit >> 2);
        chunkBatch = new ChunkBatch<>(hashMask + 1, chunkBatchCount);
    }

    @Nullable
    public E put(final int key, @NotNull final E value) {
        checkKey(key);
        final int index = index(key);
        Chunk<E> chunk = chunks[index];
        if (chunk == null) {
            chunk = chunkBatch.getChunk();
            if (chunk == null) {
                chunkBatch = new ChunkBatch<>(hashMask + 1, chunkBatchCount);
                chunk = chunkBatch.getChunk();
                assert chunk != null;
            }
            chunks[index] = chunk;
        }
        return chunk.put(key, hashMask, value);
    }

    @Nullable
    public E get(final int key) {
        checkKey(key);
        final Chunk<E> chunk = chunks[index(key)];
        if (chunk == null) {
            return null;
        }
        return chunk.get(key, hashMask);
    }

    @Nullable
    public E remove(final int key) {
        checkKey(key);
        final int index = index(key);
        final Chunk<E> chunk = chunks[index];
        if (chunk == null) {
            return null;
        }
        final E value = chunk.remove(key, hashMask);
        if (chunk.size() == 0) {
            chunks[index] = null;
            chunk.returnToBatch();
        }
        return value;
    }

    private void checkKey(final int key) {
        if (key > capacity || key < 0) {
            throw new IllegalArgumentException();
        }
    }

    private int index(final int key) {
        return (key & chunkMask) >> chunkShift;
    }


    private static class Chunk<E> {

        private final ChunkBatch<E> chunkBatch;
        private final int offset;
        private int size;

        private Chunk(@NotNull final ChunkBatch<E> chunkBatch, final int offset) {
            this.chunkBatch = chunkBatch;
            this.offset = offset;
        }

        @Nullable
        private E put(final int key, final int hashMask, @NotNull final E value) {
            final int index = index(key, hashMask);
            final E[] values = chunkBatch.values;
            final E previousValue = values[index];
            values[index] = value;
            if (previousValue == null) {
                size++;
            }
            return previousValue;
        }

        @Nullable
        private E get(final int key, final int hashMask) {
            return chunkBatch.values[index(key, hashMask)];
        }

        @Nullable
        private E remove(final int key, final int hashMask) {
            final int index = index(key, hashMask);
            final E[] values = chunkBatch.values;
            final E previousValue = values[index];
            values[index] = null;
            if (previousValue != null) {
                size--;
            }
            return previousValue;
        }

        private int index(final int key, final int hashMask) {
            return offset + (key & hashMask);
        }

        private int size() {
            return size;
        }

        private void returnToBatch() {
            chunkBatch.returnChunk(this);
        }

    }


    private static class ChunkBatch<E> {

        private final E[] values;
        private final ArrayDeque<Chunk<E>> availableChunks;

        @SuppressWarnings("unchecked")
        private ChunkBatch(final int chunkSize, final int chunkCount) {
            values = (E[]) new Object[chunkCount * chunkSize];
            availableChunks = new ArrayDeque<>(chunkCount);
            for (int i = 0, offset = 0; i < chunkCount; i++, offset += chunkSize) {
                availableChunks.offer(new Chunk<>(this, offset));
            }
        }

        @Nullable
        private Chunk<E> getChunk() {
            return availableChunks.poll();
        }

        private void returnChunk(@NotNull final Chunk<E> chunk) {
            availableChunks.offer(chunk);
        }

    }

}
