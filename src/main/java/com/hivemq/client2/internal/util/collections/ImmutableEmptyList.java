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

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * @author Silvio Giebl
 */
@Unmodifiable class ImmutableEmptyList implements ImmutableList<Object> {

    private static final @NotNull ImmutableEmptyList INSTANCE = new ImmutableEmptyList();
    private static final @NotNull Object @NotNull [] EMPTY = {};

    static <E> @NotNull ImmutableList<E> of() {
        //noinspection unchecked
        return (ImmutableList<E>) ImmutableEmptyList.INSTANCE;
    }

    private ImmutableEmptyList() {}

    @Override
    public int size() {
        return 0;
    }

    @Override
    public @NotNull Object get(final int index) {
        throw new IndexOutOfBoundsException("Empty list");
    }

    @Override
    public @NotNull Object @NotNull [] toArray() {
        return EMPTY;
    }

    @Override
    public <T> T @NotNull [] toArray(final T @NotNull [] other) {
        Checks.notNull(other, "Array");
        if (other.length > 0) {
            other[0] = null;
        }
        return other;
    }

    @Override
    public int indexOf(final @Nullable Object o) {
        return -1;
    }

    @Override
    public int lastIndexOf(final @Nullable Object o) {
        return -1;
    }

    @Override
    public @NotNull ImmutableListIterator<Object> listIterator(final int index) {
        Checks.cursorIndex(index, 0);
        return EmptyIterator.of();
    }

    @Override
    public @NotNull Spliterator<Object> spliterator() {
        return EmptySpliterator.of();
    }

    @Override
    public void forEach(final @Nullable Consumer<? super Object> consumer) {
        Checks.notNull(consumer, "Consumer");
    }

    @Override
    public @NotNull ImmutableList<Object> subList(final int fromIndex, final int toIndex) {
        Checks.indexRange(fromIndex, toIndex, 0);
        return this;
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof List)) {
            return false;
        }
        return ((List<?>) o).size() == 0;
    }

    @Override
    public int hashCode() {
        return 1;
    }

    @Override
    public @NotNull String toString() {
        return "[]";
    }

    private static class EmptyIterator implements ImmutableListIterator<Object> {

        private static final @NotNull EmptyIterator INSTANCE = new EmptyIterator();

        static <E> @NotNull ImmutableListIterator<E> of() {
            //noinspection unchecked
            return (ImmutableListIterator<E>) INSTANCE;
        }

        private EmptyIterator() {}

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public @NotNull Object next() {
            throw new NoSuchElementException();
        }

        @Override
        public int nextIndex() {
            return 0;
        }

        @Override
        public boolean hasPrevious() {
            return false;
        }

        @Override
        public @NotNull Object previous() {
            throw new NoSuchElementException();
        }

        @Override
        public int previousIndex() {
            return -1;
        }

        @Override
        public void forEachRemaining(final @Nullable Consumer<? super Object> consumer) {
            Checks.notNull(consumer, "Consumer");
        }
    }

    private static class EmptySpliterator implements Spliterator<Object> {

        private static final @NotNull EmptySpliterator INSTANCE = new EmptySpliterator();

        static <E> @NotNull Spliterator<E> of() {
            //noinspection unchecked
            return (Spliterator<E>) INSTANCE;
        }

        private EmptySpliterator() {}

        @Override
        public boolean tryAdvance(final @Nullable Consumer<? super Object> consumer) {
            Checks.notNull(consumer, "Consumer");
            return false;
        }

        @Override
        public @Nullable Spliterator<Object> trySplit() {
            return null;
        }

        @Override
        public long estimateSize() {
            return 0;
        }

        @Override
        public long getExactSizeIfKnown() {
            return 0;
        }

        @Override
        public int characteristics() {
            return Spliterator.SIZED | Spliterator.SUBSIZED | Spliterator.IMMUTABLE | Spliterator.NONNULL |
                    Spliterator.DISTINCT | Spliterator.ORDERED;
        }

        @Override
        public void forEachRemaining(final @Nullable Consumer<? super Object> consumer) {
            Checks.notNull(consumer, "Consumer");
        }
    }
}
