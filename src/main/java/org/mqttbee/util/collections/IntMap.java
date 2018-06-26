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

/**
 * @author Silvio Giebl
 */
@NotThreadSafe
public interface IntMap<E> {

    @NotNull
    static <E> IntMap<E> range(final int minKey, final int maxKey) {
        final int capacity = maxKey - minKey + 1;
        final int capacityBits = 32 - Integer.numberOfLeadingZeros(capacity - 1);
        final IntMap<E> intMap;
        if (capacityBits < 7) {
            intMap = new IntMapArray<>(capacity);
        } else if (capacityBits < 15) {
            intMap = new IntMapAllocator<E>(capacityBits, 2).alloc();
        } else if (capacityBits < 22) {
            intMap = new IntMapAllocator<E>(capacityBits, 3).alloc();
        } else {
            intMap = new IntMapAllocator<E>(capacityBits, 4).alloc();
        }
        return new IntMapCheck<>(intMap, minKey, maxKey);
    }

    @Nullable
    E put(final int key, @NotNull final E value);

    @Nullable
    E get(final int key);

    @Nullable
    E remove(final int key);

    int size();

    class IntMapCheck<E> implements IntMap<E> {

        private final IntMap<E> delegate;
        private final int minKey;
        private final int maxKey;

        IntMapCheck(@NotNull final IntMap<E> delegate, final int minKey, final int maxKey) {
            this.delegate = delegate;
            this.minKey = minKey;
            this.maxKey = maxKey;
        }

        @Nullable
        @Override
        public E put(final int key, @NotNull final E value) {
            return delegate.put(checkKey(key), value);
        }

        @Nullable
        @Override
        public E get(final int key) {
            return delegate.get(checkKey(key));
        }

        @Nullable
        @Override
        public E remove(final int key) {
            return delegate.remove(checkKey(key));
        }

        @Override
        public int size() {
            return delegate.size();
        }

        private int checkKey(final int key) {
            if (key > maxKey || key < minKey) {
                throw new IllegalArgumentException();
            }
            return key - minKey;
        }

    }

    class IntMapArray<E> implements IntMap<E> {

        private final E[] values;
        private int size;

        @SuppressWarnings("unchecked")
        IntMapArray(final int size) {
            this.values = (E[]) new Object[size];
        }

        @Nullable
        public E put(final int key, @NotNull final E value) {
            final E previousValue = values[key];
            values[key] = value;
            if (previousValue == null) {
                size++;
            }
            return previousValue;
        }

        @Nullable
        public E get(final int key) {
            return values[key];
        }

        @Nullable
        public E remove(final int key) {
            final E previousValue = values[key];
            values[key] = null;
            if (previousValue != null) {
                size--;
            }
            return previousValue;
        }

        @Override
        public int size() {
            return size;
        }

    }

    class IntMapAllocator<E> {

        private final int shift;
        private final int mask;
        private final int indexCapacity;
        private final IntMapAllocator<E> next;
        private IntMap<E> free;

        IntMapAllocator(final int capacityBits, final int split) {
            if (split == 1) {
                shift = -1;
                mask = -1;
                indexCapacity = 1 << capacityBits;
                next = null;
            } else {
                final int indexBits = capacityBits / split;
                shift = capacityBits - indexBits;
                mask = (1 << shift) - 1;
                indexCapacity = 1 << indexBits;
                next = new IntMapAllocator<>(capacityBits - indexBits, split - 1);
            }
        }

        @NotNull
        IntMap<E> alloc() {
            if (free != null) {
                final IntMap<E> allocated = free;
                free = null;
                return allocated;
            }
            if (next == null) {
                return new IntMapArray<>(indexCapacity);
            }
            return new IntMapLevel<>(shift, mask, indexCapacity, next);
        }

        void free(@NotNull final IntMap<E> intMap) {
            free = intMap;
        }

    }

    class IntMapLevel<E> implements IntMap<E> {

        private final int shift;
        private final int mask;
        private final IntMap<E>[] subLevels;
        private final IntMapAllocator<E> allocator;
        private int size;

        @SuppressWarnings("unchecked")
        IntMapLevel(
                final int shift, final int mask, final int indexCapacity, @NotNull final IntMapAllocator<E> allocator) {

            this.shift = shift;
            this.mask = mask;
            subLevels = (IntMap<E>[]) new IntMap[indexCapacity];
            this.allocator = allocator;
        }

        @Nullable
        public E put(final int key, @NotNull final E value) {
            final int index = key >> shift;
            IntMap<E> subLevel = subLevels[index];
            if (subLevel == null) {
                subLevel = allocator.alloc();
                subLevels[index] = subLevel;
            }
            final E put = subLevel.put(key & mask, value);
            if (put == null) {
                size++;
            }
            return put;
        }

        @Nullable
        public E get(final int key) {
            final IntMap<E> subLevel = subLevels[key >> shift];
            if (subLevel == null) {
                return null;
            }
            return subLevel.get(key & mask);
        }

        @Nullable
        public E remove(final int key) {
            final int index = key >> shift;
            final IntMap<E> subLevel = subLevels[index];
            if (subLevel == null) {
                return null;
            }
            final E value = subLevel.remove(key & mask);
            if (value != null) {
                size--;
                if (subLevel.size() == 0) {
                    allocator.free(subLevel);
                    subLevels[index] = null;
                }
            }
            return value;
        }

        @Override
        public int size() {
            return size;
        }

    }

}
