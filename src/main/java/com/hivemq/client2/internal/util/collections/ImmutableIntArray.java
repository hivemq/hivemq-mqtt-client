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

package com.hivemq.client2.internal.util.collections;

import com.hivemq.client2.internal.util.Checks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Arrays;

/**
 * @author Silvio Giebl
 */
@Unmodifiable class ImmutableIntArray implements ImmutableIntList {

    private final int @NotNull [] array;

    ImmutableIntArray(final int @NotNull ... array) {
        this.array = array;
        assert array.length > 1;
    }

    @Override
    public int size() {
        return array.length;
    }

    @Override
    public int get(final int index) {
        return array[Checks.index(index, array.length)];
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ImmutableIntList)) {
            return false;
        }
        if (o instanceof ImmutableIntArray) {
            return Arrays.equals(array, ((ImmutableIntArray) o).array);
        }
        final ImmutableIntList that = (ImmutableIntList) o;

        if (array.length != that.size()) {
            return false;
        }
        for (int i = 0; i < array.length; i++) {
            if (array[i] != that.get(i)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(array);
    }

    @Override
    public @NotNull String toString() {
        return Arrays.toString(array);
    }
}
