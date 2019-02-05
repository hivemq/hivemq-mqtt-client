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

package org.mqttbee.internal.util.collections;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.internal.annotations.NotThreadSafe;

/**
 * @author Silvio Giebl
 */
@NotThreadSafe
public class ChunkedIntArrayQueue {

    private final int chunkSize;
    private @NotNull IntChunk producerChunk;
    private @NotNull IntChunk consumerChunk;
    private int producerIndex;
    private int consumerIndex;
    private int size;

    public ChunkedIntArrayQueue(final int chunkSize) {
        this.chunkSize = chunkSize;
        producerChunk = consumerChunk = new IntChunk(chunkSize);
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void offer(final int value) {
        IntChunk producerChunk = this.producerChunk;
        final int producerIndex = this.producerIndex;
        final int newProducerIndex = (producerIndex == chunkSize) ? 0 : producerIndex;
        if ((size > 0) && (((producerIndex == chunkSize) && (producerChunk != consumerChunk)) ||
                ((producerChunk == consumerChunk) && (newProducerIndex == consumerIndex)))) {
            final IntChunk chunk = new IntChunk(chunkSize);
            producerChunk.jumpIndex = producerIndex - 1;
            producerChunk.next = chunk;
            producerChunk = chunk;
            this.producerChunk = chunk;
        }
        producerChunk.values[newProducerIndex] = value;
        this.producerIndex = newProducerIndex + 1;
        size++;
    }

    public int poll(final int nullValue) {
        if (size == 0) {
            return nullValue;
        }
        final IntChunk consumerChunk = this.consumerChunk;
        int consumerIndex = this.consumerIndex;
        final int value = consumerChunk.values[consumerIndex];
        size--;
        if (consumerIndex == consumerChunk.jumpIndex) {
            assert consumerChunk.next != null;
            consumerIndex = 0;
            this.consumerChunk = consumerChunk.next;
        } else {
            consumerIndex++;
            if (consumerIndex == chunkSize) {
                consumerIndex = 0;
            }
        }
        this.consumerIndex = consumerIndex;
        return value;
    }

    public int peek(final int nullValue) {
        return (size == 0) ? nullValue : consumerChunk.values[consumerIndex];
    }

    public void clear() {
        consumerChunk = producerChunk;
        consumerIndex = producerIndex = 0;
    }

    public boolean removeFirst(final int value) {
        IntChunk chunk = this.consumerChunk;
        int index = this.consumerIndex;
        for (int i = 0; i < size; i++) {
            final int currentValue = chunk.values[index];
            if (currentValue == value) {
                remove(chunk, index);
                return true;
            }
            if (index == chunk.jumpIndex) {
                assert chunk.next != null;
                index = 0;
                chunk = chunk.next;
            } else {
                index++;
                if (index == chunkSize) {
                    index = 0;
                }
            }
        }
        return false;
    }

    private void remove(final @NotNull IntChunk chunk, final int index) {
        IntChunk currentChunk = this.consumerChunk;
        int currentIndex = this.consumerIndex;
        int lastValue = currentChunk.values[currentIndex];
        while ((currentChunk != chunk) || (currentIndex != index)) {
            if (currentIndex == currentChunk.jumpIndex) {
                assert currentChunk.next != null;
                currentIndex = 0;
                currentChunk = currentChunk.next;
            } else {
                currentIndex++;
                if (currentIndex == chunkSize) {
                    currentIndex = 0;
                }
            }
            final int currentValue = currentChunk.values[currentIndex];
            currentChunk.values[currentIndex] = lastValue;
            lastValue = currentValue;
        }
        poll(-1);
    }

    private static class IntChunk {

        final @NotNull int[] values;
        int jumpIndex = -1;
        @Nullable IntChunk next;

        IntChunk(final int chunkSize) {
            values = new int[chunkSize];
        }
    }
}
