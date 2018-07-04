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

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceArray;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;

/**
 * Special queue for the following use case:
 *
 * <ul>
 *   <li>Single producer which calls {@link #offer(Object)} or {@link #canOffer()}.
 *   <li>Single consumer which calls {@link #poll()}, {@link #peek()} or iterates over all elements
 *       between consumer and producer index without polling.
 * </ul>
 *
 * <p>The queue grows in chunks to a specified maximum capacity.
 *
 * <p>The returned {@link Iterator} is always the same, as it must be used by a single consumer
 * only. Calling {@link Iterator#remove()} will poll the first element and so only works at the
 * head.
 *
 * @param <E> the type of the elements.
 * @author Silvio Giebl
 */
public class SpscIterableChunkedArrayQueue<E> implements Iterable<E> {

    private final int capacity;
    private final int chunkSize;
    private Chunk<E> producerChunk;
    private Chunk<E> consumerChunk;
    private int producerIndex;
    private int consumerIndex;
    private long produced;
    private long consumed;
    private final AtomicLong consumedAtomic = new AtomicLong();
    private long producerSize;
    private final SpscChunkedArrayQueueIterator iterator = new SpscChunkedArrayQueueIterator();

    /**
     * Creates a new queue which grows in chunks with the given size to the given maximum capacity.
     *
     * @param capacity the maximum capacity.
     * @param chunkSize the size of a single chunk.
     */
    public SpscIterableChunkedArrayQueue(final int capacity, final int chunkSize) {
        this.capacity = capacity;
        this.chunkSize = chunkSize;
        producerChunk = consumerChunk = new Chunk<>(chunkSize);
    }

    /**
     * @return <code>true</code> if an element can be added, <code>false</code> if the maximum
     *     capacity is reached.
     */
    public boolean canOffer() {
        if (producerSize == capacity) {
            producerSize = produced - consumedAtomic.get();
            return producerSize != capacity;
        }
        return true;
    }

    /**
     * Adds the given element to the tail of this queue.
     *
     * <p>This method does not check if the maximum capacity is exceeded. Use {@link #canOffer()}
     * before to ensure this. This allows the data structure to be used in an unbounded manner.
     *
     * @param e the element to add.
     */
    public void offer(@NotNull final E e) {
        producerSize++;
        produced++;
        final int producerIndex = this.producerIndex;
        final Chunk<E> producerChunk = this.producerChunk;
        final int nextProducerIndex = producerIndex + 1;
        if (nextProducerIndex == chunkSize) {
            final Chunk<E> chunk = new Chunk<>(chunkSize);
            producerChunk.next.lazySet(chunk);
            this.producerChunk = chunk;
            this.producerIndex = 0;
        } else {
            this.producerIndex = nextProducerIndex;
        }
        producerChunk.lazySet(producerIndex, e);
    }

    /**
     * Removes the element at the head of this queue if existent.
     *
     * @return the removed or element or <code>null</code> if no element exists.
     */
    @Nullable
    public E poll() {
        final int consumerIndex = this.consumerIndex;
        final Chunk<E> consumerChunk = this.consumerChunk;
        final E e = consumerChunk.get(consumerIndex);
        if (e == null) {
            return null;
        }
        consumerChunk.set(consumerIndex, null);
        consumed++;
        consumedAtomic.lazySet(consumed);
        final int nextConsumerIndex = consumerIndex + 1;
        if (nextConsumerIndex == chunkSize) {
            this.consumerChunk = consumerChunk.next.get();
            this.consumerIndex = 0;
        } else {
            this.consumerIndex = nextConsumerIndex;
        }
        return e;
    }

    /**
     * Returns the element at the head of this queue if existent.
     *
     * @return the head of this queue or <code>null</code> if no element exists.
     */
    @Nullable
    public E peek() {
        return consumerChunk.get(consumerIndex);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Returns always the same iterator, as it must be used by a single consumer only.
     *
     * @return {@inheritDoc}
     */
    @NotNull
    @Override
    public Iterator<E> iterator() {
        iterator.clear();
        return iterator;
    }

    /**
     * Chunk which is an array and has a pointer to the next chunk.
     *
     * @param <E> the type of the elements
     */
    private static class Chunk<E> extends AtomicReferenceArray<E> {

        private final AtomicReference<Chunk<E>> next = new AtomicReference<>();

        private Chunk(final int chunkSize) {
            super(chunkSize);
        }
    }

    /** {@link Iterator} for a {@link SpscIterableChunkedArrayQueue}. */
    private class SpscChunkedArrayQueueIterator implements Iterator<E> {

        private Chunk<E> iteratorChunk;
        private int iteratorIndex;
        private E iteratorElement;

        private void clear() {
            iteratorChunk = consumerChunk;
            iteratorIndex = consumerIndex;
            iteratorElement = null;
        }

        @Override
        public boolean hasNext() {
            final Chunk<E> iteratorChunk = this.iteratorChunk;
            final int iteratorIndex = this.iteratorIndex;
            iteratorElement = iteratorChunk.get(iteratorIndex);
            final int nextIteratorIndex = iteratorIndex + 1;
            if (nextIteratorIndex == chunkSize) {
                this.iteratorChunk = iteratorChunk.next.get();
                this.iteratorIndex = 0;
            } else {
                this.iteratorIndex = nextIteratorIndex;
            }
            return iteratorElement != null;
        }

        /**
         * {@inheritDoc}
         *
         * <p>Does not throw if no more elements exist but returns <code>null</code> instead.
         *
         * @return {@inheritDoc}
         */
        @Nullable
        @Override
        public E next() {
            return iteratorElement;
        }

        /**
         * {@inheritDoc}
         *
         * @throws {@inheritDoc}. Also thrown if the last element was not at the head of the queue.
         */
        @Override
        public void remove() {
            if ((iteratorElement == null) || (iteratorElement != peek())) {
                throw new IllegalStateException();
            }
            iteratorElement = null;
            consumerChunk.set(consumerIndex, null);
            consumed++;
            consumedAtomic.lazySet(consumed);
            consumerChunk = iteratorChunk;
            consumerIndex = iteratorIndex;
        }
    }
}
