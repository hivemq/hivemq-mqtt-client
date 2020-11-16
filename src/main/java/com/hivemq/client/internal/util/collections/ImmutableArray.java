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
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Consumer;

/**
 * @author Silvio Giebl
 */
@Unmodifiable class ImmutableArray<E> implements ImmutableList<E> {

    @Contract("null -> fail")
    static <E> @NotNull ImmutableList<E> of(final @Nullable Object @Nullable ... elements) {
        return of(elements, "Immutable list");
    }

    @Contract("null, _ -> fail")
    static <E> @NotNull ImmutableList<E> of(final @Nullable Object @Nullable [] elements, final @NotNull String name) {
        return new ImmutableArray<>(Checks.elementsNotNull(elements, name));
    }

    private final @NotNull Object @NotNull [] array;

    ImmutableArray(final @NotNull Object @NotNull [] array) {
        this.array = array;
        assert array.length > 1;
    }

    int getFromIndex() {
        return 0;
    }

    int getToIndex() {
        return array.length;
    }

    @Override
    public int size() {
        return getToIndex() - getFromIndex();
    }

    @Override
    public @NotNull E get(final int index) {
        //noinspection unchecked
        return (E) array[getFromIndex() + Checks.index(index, size())];
    }

    @Override
    public @NotNull Object @NotNull [] toArray() {
        return Arrays.copyOfRange(array, getFromIndex(), getToIndex());
    }

    @Override
    public <T> T @NotNull [] toArray(@Nullable T @NotNull [] other) {
        Checks.notNull(other, "Array");
        final int size = size();
        if (other.length < size) {
            //noinspection unchecked
            other = (T[]) Array.newInstance(other.getClass().getComponentType(), size);
        } else if (other.length > size) {
            other[size] = null;
        }
        //noinspection SuspiciousSystemArraycopy
        System.arraycopy(array, getFromIndex(), other, 0, size);
        return other;
    }

    @Override
    public int indexOf(final @Nullable Object o) {
        if (o == null) {
            return -1;
        }
        final int fromIndex = getFromIndex();
        final int toIndex = getToIndex();
        for (int i = fromIndex; i < toIndex; i++) {
            if (o.equals(array[i])) {
                return i - fromIndex;
            }
        }
        return -1;
    }

    @Override
    public int lastIndexOf(final @Nullable Object o) {
        if (o == null) {
            return -1;
        }
        final int fromIndex = getFromIndex();
        final int toIndex = getToIndex();
        for (int i = toIndex - 1; i >= fromIndex; i--) {
            if (o.equals(array[i])) {
                return i - fromIndex;
            }
        }
        return -1;
    }

    @Override
    public @NotNull ImmutableListIterator<E> listIterator(final int index) {
        return new ArrayIterator(getFromIndex() + Checks.cursorIndex(index, size()));
    }

    @Override
    public @NotNull Spliterator<E> spliterator() {
        return Spliterators.spliterator(
                array, getFromIndex(), getToIndex(), Spliterator.IMMUTABLE | Spliterator.NONNULL | Spliterator.ORDERED);
    }

    @Override
    public void forEach(final @Nullable Consumer<? super E> consumer) {
        Checks.notNull(consumer, "Consumer");
        final int fromIndex = getFromIndex();
        final int toIndex = getToIndex();
        for (int i = fromIndex; i < toIndex; i++) {
            //noinspection unchecked
            consumer.accept((E) array[i]);
        }
    }

    @Override
    public @NotNull ImmutableList<E> subList(final int fromIndex, final int toIndex) {
        final int size = size();
        Checks.indexRange(fromIndex, toIndex, size);
        final int startIndex = getFromIndex();
        final int subSize = toIndex - fromIndex;
        switch (subSize) {
            case 0:
                return ImmutableEmptyList.of();
            case 1:
                //noinspection unchecked
                return new ImmutableElement<>((E) array[startIndex + fromIndex]);
            default:
                return (subSize == size) ? this : new SubArray<>(array, startIndex + fromIndex, startIndex + toIndex);
        }
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof List)) {
            return false;
        }
        final List<?> that = (List<?>) o;
        final int fromIndex = getFromIndex();
        final int toIndex = getToIndex();
        if ((toIndex - fromIndex) != that.size()) {
            return false;
        }
        if (that instanceof RandomAccess) {
            for (int i = fromIndex; i < toIndex; i++) {
                if (!array[i].equals(that.get(i))) {
                    return false;
                }
            }
        } else {
            int i = fromIndex;
            for (final Object e : that) {
                if (!array[i++].equals(e)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int fromIndex = getFromIndex();
        final int toIndex = getToIndex();
        int hashCode = 1;
        for (int i = fromIndex; i < toIndex; i++) {
            hashCode = 31 * hashCode + array[i].hashCode();
        }
        return hashCode;
    }

    @Override
    public @NotNull String toString() {
        int i = getFromIndex();
        final int toIndex = getToIndex();
        final StringBuilder sb = new StringBuilder().append('[');
        while (true) {
            sb.append(array[i++]);
            if (i == toIndex) {
                return sb.append(']').toString();
            }
            sb.append(", ");
        }
    }

    private static class SubArray<E> extends ImmutableArray<E> {

        private final int fromIndex;
        private final int toIndex;

        SubArray(final @NotNull Object @NotNull [] array, final int fromIndex, final int toIndex) {
            super(array);
            this.fromIndex = fromIndex;
            this.toIndex = toIndex;
            assert (toIndex - fromIndex) > 1;
            assert (toIndex - fromIndex) < array.length;
        }

        @Override
        public int getFromIndex() {
            return fromIndex;
        }

        @Override
        public int getToIndex() {
            return toIndex;
        }

        @Override
        public @NotNull ImmutableList<E> trim() {
            return new ImmutableArray<>(toArray());
        }
    }

    private class ArrayIterator implements ImmutableListIterator<E> {

        private int index;

        ArrayIterator(final int index) {
            this.index = index;
        }

        @Override
        public boolean hasNext() {
            return index < getToIndex();
        }

        @Override
        public @NotNull E next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            //noinspection unchecked
            return (E) array[index++];
        }

        @Override
        public int nextIndex() {
            return index - getFromIndex();
        }

        @Override
        public boolean hasPrevious() {
            return index > getFromIndex();
        }

        @Override
        public @NotNull E previous() {
            if (!hasPrevious()) {
                throw new NoSuchElementException();
            }
            //noinspection unchecked
            return (E) array[--index];
        }

        @Override
        public int previousIndex() {
            return index - 1 - getFromIndex();
        }

        @Override
        public void forEachRemaining(final @Nullable Consumer<? super E> consumer) {
            Checks.notNull(consumer, "Consumer");
            while (hasNext()) {
                consumer.accept(next());
            }
        }
    }
}
