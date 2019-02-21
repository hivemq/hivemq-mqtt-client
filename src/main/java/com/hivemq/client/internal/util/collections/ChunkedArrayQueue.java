/*
 * Copyright 2018 dc-square and the HiveMQ MQTT Client Project
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

package com.hivemq.client.internal.util.collections;

import com.hivemq.client.internal.annotations.NotThreadSafe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author Silvio Giebl
 */
@NotThreadSafe
public class ChunkedArrayQueue<E> implements Iterable<E> {

    private final int chunkSize;
    private @Nullable E single;
    private @Nullable Chunk<E> producerChunk;
    private @Nullable Chunk<E> consumerChunk;
    private int producerIndex;
    private int consumerIndex;
    private int size;
    private final @NotNull ChunkedArrayQueueIterator iterator = new ChunkedArrayQueueIterator();

    public ChunkedArrayQueue(final int chunkSize) {
        this.chunkSize = chunkSize;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void offer(final @NotNull E e) {
        if (size == 0) {
            size = 1;
            single = e;
            return;
        }
        if (size == 1) {
            if (producerChunk == null) {
                producerChunk = consumerChunk = new Chunk<>(chunkSize);
            }
            final E single = this.single;
            if (single != null) {
                size = 0;
                this.single = null;
                offerQueue(single);
            }
        }
        offerQueue(e);
    }

    private void offerQueue(final @NotNull E e) {
        Chunk<E> producerChunk = this.producerChunk;
        assert producerChunk != null;
        int producerIndex = this.producerIndex;
        if ((producerIndex == chunkSize) ||
                ((producerChunk == consumerChunk) && (producerChunk.values[producerIndex] != null))) {
            if (size >= chunkSize) {
                final Chunk<E> chunk = new Chunk<>(chunkSize);
                producerChunk.jumpIndex = producerIndex - 1;
                producerChunk.next = chunk;
                producerChunk = chunk;
                this.producerChunk = chunk;
            }
            producerIndex = 0;
        }
        producerChunk.values[producerIndex] = e;
        this.producerIndex = producerIndex + 1;
        size++;
    }

    public @Nullable E poll() {
        final E single = this.single;
        if (single != null) {
            size = 0;
            this.single = null;
            return single;
        }
        if (consumerChunk == null) {
            return null;
        }
        final Chunk<E> consumerChunk = this.consumerChunk;
        int consumerIndex = this.consumerIndex;
        final E e = consumerChunk.values[consumerIndex];
        if (e == null) {
            return null;
        }
        consumerChunk.values[consumerIndex] = null;
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
        return e;
    }

    public @Nullable E peek() {
        if (single != null) {
            return single;
        }
        if (consumerChunk == null) {
            return null;
        }
        return consumerChunk.values[consumerIndex];
    }

    public void clear() {
        //noinspection StatementWithEmptyBody
        while (poll() != null) {
        }
    }

    @Override
    public @NotNull Iterator<E> iterator() {
        iterator.clear();
        return iterator;
    }

    private static class Chunk<E> {

        final @Nullable E @NotNull [] values;
        int jumpIndex = -1;
        @Nullable Chunk<E> next;

        Chunk(final int chunkSize) {
            //noinspection unchecked
            values = (E[]) new Object[chunkSize];
        }
    }

    private class ChunkedArrayQueueIterator implements Iterator<E> {

        private @Nullable E iteratorSingle;
        private @Nullable Chunk<E> iteratorChunk;
        private int iteratorIndex;
        private int iterated;

        void clear() {
            iteratorSingle = single;
            iteratorChunk = consumerChunk;
            iteratorIndex = consumerIndex;
            iterated = 0;
        }

        @Override
        public boolean hasNext() {
            return iterated < size;
        }

        @Override
        public @NotNull E next() {
            final E iteratorSingle = this.iteratorSingle;
            if (iteratorSingle != null) {
                iterated = 1;
                this.iteratorSingle = null;
                return iteratorSingle;
            }
            if (iteratorChunk == null) {
                throw new NoSuchElementException();
            }
            final E e = iteratorChunk.values[iteratorIndex];
            if (e == null) {
                throw new NoSuchElementException();
            }
            if (iteratorIndex == iteratorChunk.jumpIndex) {
                iteratorIndex = 0;
                iteratorChunk = iteratorChunk.next;
            } else {
                iteratorIndex++;
                if (iteratorIndex == chunkSize) {
                    iteratorIndex = 0;
                }
            }
            iterated++;
            return e;
        }

        @Override
        public void remove() {
            if (iterated != 1) {
                throw new IllegalStateException();
            }
            iterated = 0;
            poll();
        }
    }
}
