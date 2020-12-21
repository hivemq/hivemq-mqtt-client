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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

/**
 * @author Silvio Giebl
 */
@Unmodifiable class ImmutableEmptyIntList implements ImmutableIntList {

    static final @NotNull ImmutableEmptyIntList INSTANCE = new ImmutableEmptyIntList();

    private ImmutableEmptyIntList() {}

    @Override
    public int size() {
        return 0;
    }

    @Override
    public int get(final int index) {
        throw new IndexOutOfBoundsException("Empty int list");
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ImmutableIntList)) {
            return false;
        }
        return ((ImmutableIntList) o).size() == 0;
    }

    @Override
    public int hashCode() {
        return 1;
    }

    @Override
    public @NotNull String toString() {
        return "[]";
    }
}
