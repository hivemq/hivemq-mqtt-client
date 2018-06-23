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
            intMap = new IntMapFirstLevel<>(capacity);
        } else if (capacityBits < 15) {
            intMap = IntMapSecondLevel.create(capacityBits);
        } else if (capacityBits < 22) {
            intMap = IntMapThirdLevel.create(capacityBits);
        } else {
            intMap = IntMapFourthLevel.create(capacityBits);
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

    class IntMapFirstLevel<E> implements IntMap<E> {

        private final E[] values;
        private int size;

        @SuppressWarnings("unchecked")
        IntMapFirstLevel(final int size) {
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

    abstract class IntMapLevel<E> implements IntMap<E> {

        private final int shift;
        private final int mask;
        private final IntMap<E>[] subLevels;
        private IntMap<E> freeSubLevel;
        private int size;

        @SuppressWarnings("unchecked")
        IntMapLevel(final int shift, final int mask, final int indexCapacity) {
            this.shift = shift;
            this.mask = mask;
            subLevels = (IntMap<E>[]) new IntMap[indexCapacity];
        }

        @Nullable
        public E put(final int key, @NotNull final E value) {
            final int index = key >> shift;
            IntMap<E> subLevel = subLevels[index];
            if (subLevel == null) {
                if (freeSubLevel == null) {
                    subLevel = newSubLevel();
                } else {
                    subLevel = freeSubLevel;
                    freeSubLevel = null;
                }
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
                    freeSubLevel = subLevel;
                    subLevels[index] = null;
                }
            }
            return value;
        }

        @Override
        public int size() {
            return size;
        }

        abstract IntMap<E> newSubLevel();

    }

    class IntMapSecondLevel<E> extends IntMapLevel<E> {

        @NotNull
        static <E> IntMap<E> create(final int secondLevelCapacityBits) {
            final int secondLevelIndexBits = secondLevelCapacityBits / 2;
            final int secondLevelShift = secondLevelCapacityBits - secondLevelIndexBits;
            final int secondLevelMask = (1 << secondLevelShift) - 1;
            final int secondLevelIndexCapacity = 1 << secondLevelIndexBits;

            final int firstLevelCapacityBits = secondLevelCapacityBits - secondLevelIndexBits;
            final int firstLevelIndexCapacity = 1 << firstLevelCapacityBits;

            return new IntMapSecondLevel<>(secondLevelShift, secondLevelMask, secondLevelIndexCapacity,
                    firstLevelIndexCapacity);
        }

        private final int firstLevelIndexCapacity;

        IntMapSecondLevel(final int shift, final int mask, final int indexCapacity, final int firstLevelIndexCapacity) {
            super(shift, mask, indexCapacity);
            this.firstLevelIndexCapacity = firstLevelIndexCapacity;
        }

        @Override
        IntMap<E> newSubLevel() {
            return new IntMapFirstLevel<>(firstLevelIndexCapacity);
        }
    }

    class IntMapThirdLevel<E> extends IntMapLevel<E> {

        @NotNull
        static <E> IntMap<E> create(final int thirdLevelCapacityBits) {
            final int thirdLevelIndexBits = thirdLevelCapacityBits / 3;
            final int thirdLevelShift = thirdLevelCapacityBits - thirdLevelIndexBits;
            final int thirdLevelMask = (1 << thirdLevelShift) - 1;
            final int thirdLevelIndexCapacity = 1 << thirdLevelIndexBits;

            final int secondLevelCapacityBits = thirdLevelCapacityBits - thirdLevelIndexBits;
            final int secondLevelIndexBits = secondLevelCapacityBits / 2;
            final int secondLevelShift = secondLevelCapacityBits - secondLevelIndexBits;
            final int secondLevelMask = (1 << secondLevelShift) - 1;
            final int secondLevelIndexCapacity = 1 << secondLevelIndexBits;

            final int firstLevelCapacityBits = secondLevelCapacityBits - secondLevelIndexBits;
            final int firstLevelIndexCapacity = 1 << firstLevelCapacityBits;

            return new IntMapThirdLevel<>(thirdLevelShift, thirdLevelMask, thirdLevelIndexCapacity, secondLevelShift,
                    secondLevelMask, secondLevelIndexCapacity, firstLevelIndexCapacity);
        }

        private final int secondLevelShift;
        private final int secondLevelMask;
        private final int secondLevelIndexCapacity;
        private final int firstLevelIndexCapacity;

        IntMapThirdLevel(
                final int shift, final int mask, final int indexCapacity, final int secondLevelShift,
                final int secondLevelMask, final int secondLevelIndexCapacity, final int firstLevelIndexCapacity) {

            super(shift, mask, indexCapacity);
            this.secondLevelShift = secondLevelShift;
            this.secondLevelMask = secondLevelMask;
            this.secondLevelIndexCapacity = secondLevelIndexCapacity;
            this.firstLevelIndexCapacity = firstLevelIndexCapacity;
        }

        @Override
        IntMap<E> newSubLevel() {
            return new IntMapSecondLevel<>(
                    secondLevelShift, secondLevelMask, secondLevelIndexCapacity, firstLevelIndexCapacity);
        }
    }

    class IntMapFourthLevel<E> extends IntMapLevel<E> {

        @NotNull
        static <E> IntMap<E> create(final int fourthLevelCapacityBits) {
            final int fourthLevelIndexBits = fourthLevelCapacityBits / 4;
            final int fourthLevelShift = fourthLevelCapacityBits - fourthLevelIndexBits;
            final int fourthLevelMask = (1 << fourthLevelShift) - 1;
            final int fourthLevelIndexCapacity = 1 << fourthLevelIndexBits;

            final int thirdLevelCapacityBits = fourthLevelCapacityBits - fourthLevelIndexBits;
            final int thirdLevelIndexBits = thirdLevelCapacityBits / 3;
            final int thirdLevelShift = thirdLevelCapacityBits - thirdLevelIndexBits;
            final int thirdLevelMask = (1 << thirdLevelShift) - 1;
            final int thirdLevelIndexCapacity = 1 << thirdLevelIndexBits;

            final int secondLevelCapacityBits = thirdLevelCapacityBits - thirdLevelIndexBits;
            final int secondLevelIndexBits = secondLevelCapacityBits / 2;
            final int secondLevelShift = secondLevelCapacityBits - secondLevelIndexBits;
            final int secondLevelMask = (1 << secondLevelShift) - 1;
            final int secondLevelIndexCapacity = 1 << secondLevelIndexBits;

            final int firstLevelCapacityBits = secondLevelCapacityBits - secondLevelIndexBits;
            final int firstLevelIndexCapacity = 1 << firstLevelCapacityBits;

            return new IntMapFourthLevel<>(fourthLevelShift, fourthLevelMask, fourthLevelIndexCapacity, thirdLevelShift,
                    thirdLevelMask, thirdLevelIndexCapacity, secondLevelShift, secondLevelMask,
                    secondLevelIndexCapacity, firstLevelIndexCapacity);
        }

        private final int thirdLevelShift;
        private final int thirdLevelMask;
        private final int thirdLevelIndexCapacity;
        private final int secondLevelShift;
        private final int secondLevelMask;
        private final int secondLevelIndexCapacity;
        private final int firstLevelIndexCapacity;

        IntMapFourthLevel(
                final int shift, final int mask, final int indexCapacity, final int thirdLevelShift,
                final int thirdLevelMask, final int thirdLevelIndexCapacity, final int secondLevelShift,
                final int secondLevelMask, final int secondLevelIndexCapacity, final int firstLevelIndexCapacity) {

            super(shift, mask, indexCapacity);
            this.thirdLevelShift = thirdLevelShift;
            this.thirdLevelMask = thirdLevelMask;
            this.thirdLevelIndexCapacity = thirdLevelIndexCapacity;
            this.secondLevelShift = secondLevelShift;
            this.secondLevelMask = secondLevelMask;
            this.secondLevelIndexCapacity = secondLevelIndexCapacity;
            this.firstLevelIndexCapacity = firstLevelIndexCapacity;
        }

        @Override
        IntMap<E> newSubLevel() {
            return new IntMapThirdLevel<>(thirdLevelShift, thirdLevelMask, thirdLevelIndexCapacity, secondLevelShift,
                    secondLevelMask, secondLevelIndexCapacity, firstLevelIndexCapacity);
        }
    }

}
