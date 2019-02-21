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
import com.hivemq.client.internal.util.Pow2Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Silvio Giebl
 */
@NotThreadSafe
public abstract class IntMap<E> {

    private static final int ONE_LEVEL_CAPACITY_BITS = 7;
    private static final int TWO_LEVEL_CAPACITY_BITS = 12;
    private static final int THREE_LEVEL_CAPACITY_BITS = 18;

    public static @NotNull <E> IntMap<E> range(final int minKey, final int maxKey) {
        final int capacity = maxKey - minKey + 1;
        final int capacityBits = Pow2Util.roundToPowerOf2Bits(capacity);
        return create(capacity, capacityBits, minKey, maxKey);
    }

    public static @NotNull <E> IntMap<E> resize(final @NotNull IntMap<E> oldMap, final int newMaxKey) {
        final int oldMaxKey = oldMap.getMaxKey();
        if (oldMaxKey == newMaxKey) {
            return oldMap;
        }
        final int minKey = oldMap.getMinKey();
        final int newCapacity = newMaxKey - minKey + 1;
        final int newCapacityBits = Pow2Util.roundToPowerOf2Bits(newCapacity);
        if ((newCapacityBits > ONE_LEVEL_CAPACITY_BITS) && (oldMap instanceof IntMapCheck)) {
            final int oldCapacity = oldMaxKey - minKey + 1;
            final int oldCapacityBits = Pow2Util.roundToPowerOf2Bits(oldCapacity);
            if (oldCapacityBits == newCapacityBits) {
                return new IntMapCheck<>(((IntMapCheck<E>) oldMap).delegate, minKey, newMaxKey);
            }
        }
        final IntMap<E> newMap = create(newCapacity, newCapacityBits, minKey, newMaxKey);
        oldMap.forEach((key, value) -> {
            if (key > newMaxKey) {
                return false;
            }
            newMap.put(key, value);
            return true;
        });
        return newMap;
    }

    private static @NotNull <E> IntMap<E> create(
            final int capacity, final int capacityBits, final int minKey, final int maxKey) {

        final IntMap<E> intMap;
        if (capacityBits <= ONE_LEVEL_CAPACITY_BITS) {
            intMap = new IntMapArray<>(capacity);
        } else if (capacityBits <= TWO_LEVEL_CAPACITY_BITS) {
            intMap = new IntMapAllocator<E>(capacityBits, 2).alloc();
        } else if (capacityBits <= THREE_LEVEL_CAPACITY_BITS) {
            intMap = new IntMapAllocator<E>(capacityBits, 3).alloc();
        } else {
            intMap = new IntMapAllocator<E>(capacityBits, 4).alloc();
        }
        return new IntMapCheck<>(intMap, minKey, maxKey);
    }

    public abstract @Nullable E put(int key, @NotNull E value);

    public abstract @Nullable E get(int key);

    public abstract @Nullable E remove(int key);

    public abstract int size();

    public abstract int getMinKey();

    public abstract int getMaxKey();

    public abstract void clear();

    public void forEach(final @NotNull IntMapConsumer<E> consumer) {
        forEach(consumer, 0);
    }

    abstract boolean forEach(@NotNull IntMapConsumer<E> consumer, int baseIndex);

    public interface IntMapConsumer<E> {

        boolean accept(int key, @NotNull E value);
    }

    public static class IntMapCheck<E> extends IntMap<E> {

        private final @NotNull IntMap<E> delegate;
        private final int minKey;
        private final int maxKey;

        IntMapCheck(final @NotNull IntMap<E> delegate, final int minKey, final int maxKey) {
            this.delegate = delegate;
            this.minKey = minKey;
            this.maxKey = maxKey;
        }

        @Override
        public @Nullable E put(final int key, final @NotNull E value) {
            return delegate.put(checkKey(key), value);
        }

        @Override
        public @Nullable E get(final int key) {
            return delegate.get(checkKey(key));
        }

        @Override
        public @Nullable E remove(final int key) {
            return delegate.remove(checkKey(key));
        }

        @Override
        public int size() {
            return delegate.size();
        }

        @Override
        public int getMinKey() {
            return minKey;
        }

        @Override
        public int getMaxKey() {
            return maxKey;
        }

        @Override
        public void clear() {
            delegate.clear();
        }

        @Override
        public void forEach(final @NotNull IntMapConsumer<E> consumer) {
            delegate.forEach(consumer, minKey);
        }

        @Override
        boolean forEach(final @NotNull IntMapConsumer<E> consumer, final int baseIndex) {
            return delegate.forEach(consumer, minKey);
        }

        private int checkKey(final int key) {
            if (key > maxKey || key < minKey) {
                throw new IndexOutOfBoundsException();
            }
            return key - minKey;
        }
    }

    public static class IntMapArray<E> extends IntMap<E> {

        private final @Nullable E @NotNull [] values;
        private int size;

        @SuppressWarnings("unchecked")
        IntMapArray(final int size) {
            this.values = (E[]) new Object[size];
        }

        @Override
        public @Nullable E put(final int key, final @NotNull E value) {
            final E previousValue = values[key];
            values[key] = value;
            if (previousValue == null) {
                size++;
            }
            return previousValue;
        }

        @Override
        public @Nullable E get(final int key) {
            return values[key];
        }

        @Override
        public @Nullable E remove(final int key) {
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

        @Override
        public int getMinKey() {
            return 0;
        }

        @Override
        public int getMaxKey() {
            return values.length - 1;
        }

        @Override
        public void clear() {
            int emitted = 0;
            for (int index = 0; index < values.length; index++) {
                final E value = values[index];
                if (value != null) {
                    values[index] = null;
                    emitted++;
                    if (emitted == size) {
                        break;
                    }
                }
            }
            size = 0;
        }

        @Override
        boolean forEach(final @NotNull IntMapConsumer<E> consumer, final int baseIndex) {
            int emitted = 0;
            for (int index = 0; index < values.length; index++) {
                final E value = values[index];
                if (value != null) {
                    if (!consumer.accept(baseIndex + index, value)) {
                        return false;
                    }
                    emitted++;
                    if (emitted == size) {
                        break;
                    }
                }
            }
            return true;
        }
    }

    private static class IntMapAllocator<E> {

        private final int shift;
        private final int mask;
        private final int indexCapacity;
        private final @Nullable IntMapAllocator<E> next;
        private @Nullable IntMap<E> free;

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

        @NotNull IntMap<E> alloc() {
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

        void free(final @NotNull IntMap<E> intMap) {
            free = intMap;
        }

    }

    public static class IntMapLevel<E> extends IntMap<E> {

        private final int shift;
        private final int mask;
        private final @Nullable IntMap<E> @NotNull [] subLevels;
        private final @NotNull IntMapAllocator<E> allocator;
        private int size;

        @SuppressWarnings("unchecked")
        IntMapLevel(
                final int shift, final int mask, final int indexCapacity, final @NotNull IntMapAllocator<E> allocator) {

            this.shift = shift;
            this.mask = mask;
            subLevels = (IntMap<E>[]) new IntMap[indexCapacity];
            this.allocator = allocator;
        }

        @Override
        public @Nullable E put(final int key, final @NotNull E value) {
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

        @Override
        public @Nullable E get(final int key) {
            final IntMap<E> subLevel = subLevels[key >> shift];
            if (subLevel == null) {
                return null;
            }
            return subLevel.get(key & mask);
        }

        @Override
        public @Nullable E remove(final int key) {
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

        @Override
        public int getMinKey() {
            return 0;
        }

        @Override
        public int getMaxKey() {
            return (subLevels.length - 1) << shift;
        }

        @Override
        public void clear() {
            int cleared = 0;
            for (int i = 0; i < subLevels.length; i++) {
                final IntMap<E> subLevel = subLevels[i];
                if (subLevel != null) {
                    cleared += subLevel.size();
                    subLevel.clear();
                    subLevels[i] = null;
                    if (cleared == size) {
                        break;
                    }
                }
            }
            size = 0;
        }

        @Override
        boolean forEach(final @NotNull IntMapConsumer<E> consumer, int baseIndex) {
            int emitted = 0;
            for (final IntMap<E> subLevel : subLevels) {
                if (subLevel != null) {
                    if (!subLevel.forEach(consumer, baseIndex)) {
                        return false;
                    }
                    emitted += subLevel.size();
                    if (emitted == size) {
                        break;
                    }
                }
                baseIndex += mask + 1;
            }
            return true;
        }
    }

}
