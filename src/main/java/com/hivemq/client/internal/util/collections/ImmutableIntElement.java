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

/**
 * @author Silvio Giebl
 */
@Unmodifiable class ImmutableIntElement implements ImmutableIntList {

    private final int element;

    ImmutableIntElement(final int element) {
        this.element = element;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public int get(final int index) {
        Checks.index(index, 1);
        return element;
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ImmutableIntList)) {
            return false;
        }
        final ImmutableIntList that = (ImmutableIntList) o;

        return (that.size() == 1) && (element == that.get(0));
    }

    @Override
    public int hashCode() {
        return 31 + element;
    }

    @Override
    public @NotNull String toString() {
        return "[" + element + "]";
    }
}
