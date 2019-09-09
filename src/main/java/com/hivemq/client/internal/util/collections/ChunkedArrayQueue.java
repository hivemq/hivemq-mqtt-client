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

import java.util.NoSuchElementException;

/**
 * @author Silvio Giebl
 */
@NotThreadSafe
public class ChunkedArrayQueue<E> implements Iterable<E> {

    private final int chunkSize;
    private @Nullable E single;
    private @Nullable Object @Nullable [] producerChunk;
    private @Nullable Object @Nullable [] consumerChunk;
    private int producerIndex;
    private int consumerIndex;
    private int size;

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
                producerChunk = consumerChunk = new Object[chunkSize];
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
        Object[] producerChunk = this.producerChunk;
        assert producerChunk != null;
        int producerIndex = this.producerIndex;
        if ((producerIndex == chunkSize) ||
                ((producerChunk == consumerChunk) && (producerChunk[producerIndex] != null))) {
            if (size >= chunkSize) {
                final Object[] chunk = new Object[chunkSize];
                final Object o = producerChunk[producerIndex - 1];
                producerChunk[producerIndex - 1] = chunk;
                producerChunk = chunk;
                producerChunk[0] = o;
                this.producerChunk = chunk;
                producerIndex = 1;
            } else {
                producerIndex = 0;
            }
        }
        producerChunk[producerIndex] = e;
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
        final Object[] consumerChunk = this.consumerChunk;
        int consumerIndex = this.consumerIndex;
        final Object o = consumerChunk[consumerIndex];
        if (o == null) {
            return null;
        }
        consumerChunk[consumerIndex] = null;
        final E e;
        if (o.getClass() == Object[].class) {
            final Object[] nextChunk = (Object[]) o;
            this.consumerChunk = nextChunk;
            consumerIndex = 1;
            //noinspection unchecked
            e = (E) nextChunk[0];
        } else {
            //noinspection unchecked
            e = (E) o;
            consumerIndex++;
            if (consumerIndex == chunkSize) {
                consumerIndex = 0;
            }
        }
        size--;
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
        final Object o = consumerChunk[consumerIndex];
        if (o == null) {
            return null;
        }
        final E e;
        if (o.getClass() == Object[].class) {
            final Object[] nextChunk = (Object[]) o;
            //noinspection unchecked
            e = (E) nextChunk[0];
        } else {
            //noinspection unchecked
            e = (E) o;
        }
        return e;
    }

    public void clear() {
        //noinspection StatementWithEmptyBody
        while (poll() != null) {
        }
    }

    @Override
    public @NotNull Iterator iterator() {
        return new Iterator();
    }

    public class Iterator implements java.util.Iterator<E> {

        private @Nullable Object @Nullable [] iteratorChunk;
        private int iteratorIndex;
        private int iterated;

        Iterator() {
            reset();
        }

        public void reset() {
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
            final E iteratorSingle = single;
            if (iteratorSingle != null) {
                if (iterated > 0) {
                    throw new NoSuchElementException();
                }
                iterated = 1;
                return iteratorSingle;
            }
            if (iteratorChunk == null) {
                throw new NoSuchElementException();
            }
            final Object o = iteratorChunk[iteratorIndex];
            if (o == null) {
                throw new NoSuchElementException();
            }
            final E e;
            if (o.getClass() == Object[].class) {
                final Object[] nextChunk = (Object[]) o;
                this.iteratorChunk = nextChunk;
                iteratorIndex = 1;
                //noinspection unchecked
                e = (E) nextChunk[0];
            } else {
                //noinspection unchecked
                e = (E) o;
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
            for (int i = 0; i < iterated; i++) {
                poll();
            }
            iterated = 0;
        }
    }
}
