/*
 * Copyright 2018-present HiveMQ and the HiveMQ Community
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
 */

package com.hivemq.client.internal.util.collections;

import com.hivemq.client.internal.util.Checks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Arrays;

/**
 * @author Silvio Giebl
 */
@Unmodifiable
public interface ImmutableIntList {

    static @NotNull ImmutableIntList of() {
        return ImmutableEmptyIntList.INSTANCE;
    }

    static @NotNull ImmutableIntList of(final int i) {
        return new ImmutableIntElement(i);
    }

    static @NotNull ImmutableIntList of(final int i1, final int i2) {
        return new ImmutableIntArray(i1, i2);
    }

    static @NotNull ImmutableIntList of(final int i1, final int i2, final int i3) {
        return new ImmutableIntArray(i1, i2, i3);
    }

    static @NotNull ImmutableIntList of(final int i1, final int i2, final int i3, final @NotNull int... others) {
        Checks.notNull(others, "Int array");
        final int[] array = new int[3 + others.length];
        array[0] = i1;
        array[0] = i2;
        array[0] = i3;
        System.arraycopy(others, 0, array, 3, others.length);
        return new ImmutableIntArray(array);
    }

    static @NotNull ImmutableIntList copyOf(final int @NotNull [] array) {
        Checks.notNull(array, "Int array");
        switch (array.length) {
            case 0:
                return ImmutableEmptyIntList.INSTANCE;
            case 1:
                return new ImmutableIntElement(array[0]);
            default:
                return new ImmutableIntArray(array.clone());
        }
    }

    static @NotNull Builder builder() {
        return new Builder();
    }

    static @NotNull Builder builder(final int capacity) {
        return new Builder(capacity);
    }

    int size();

    default boolean isEmpty() {
        return size() == 0;
    }

    int get(int index);

    class Builder {

        private static final int INITIAL_CAPACITY = 4;

        private int i;
        private int @Nullable [] array;
        private int size;

        private Builder() {}

        private Builder(final int capacity) {
            if (capacity > 1) {
                array = new int[capacity];
            }
        }

        private int newCapacity(final int capacity) {
            return capacity + (capacity >> 1);
        }

        public @NotNull Builder add(final int i) {
            if (size == 0) {
                this.i = i;
                size = 1;
            } else {
                if (array == null) {
                    array = new int[INITIAL_CAPACITY];
                } else if (size == array.length) {
                    array = Arrays.copyOf(array, newCapacity(array.length));
                }
                if (size == 1) {
                    array[0] = this.i;
                }
                array[size++] = i;
            }
            return this;
        }

        public @NotNull ImmutableIntList build() {
            switch (size) {
                case 0:
                    return ImmutableEmptyIntList.INSTANCE;
                case 1:
                    return new ImmutableIntElement(i);
                default:
                    assert array != null;
                    if (array.length == size) {
                        return new ImmutableIntArray(array);
                    }
                    return new ImmutableIntArray(Arrays.copyOfRange(array, 0, size));
            }
        }
    }
}
