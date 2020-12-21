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
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.lang.reflect.Array;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * @author Silvio Giebl
 */
@Unmodifiable class ImmutableElement<E> implements ImmutableList<E> {

    @Contract("null -> fail")
    static <E> @NotNull ImmutableList<E> of(final @Nullable E e) {
        return of(e, "Immutable list");
    }

    @Contract("null, _ -> fail")
    static <E> @NotNull ImmutableList<E> of(final @Nullable E e, final @NotNull String name) {
        return new ImmutableElement<>(Checks.elementNotNull(e, name, 0));
    }

    private final @NotNull E element;

    ImmutableElement(final @NotNull E element) {
        this.element = element;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public @NotNull E get(final int index) {
        Checks.index(index, 1);
        return element;
    }

    @Override
    public @NotNull Object @NotNull [] toArray() {
        return new Object[]{element};
    }

    @Override
    public <T> T @NotNull [] toArray(T @Nullable [] other) {
        Checks.notNull(other, "Array");
        if (other.length < 1) {
            //noinspection unchecked
            other = (T[]) Array.newInstance(other.getClass().getComponentType(), 1);
        } else if (other.length > 1) {
            other[1] = null;
        }
        ((Object[]) other)[0] = element;
        return other;
    }

    @Override
    public int indexOf(final @Nullable Object o) {
        return (element.equals(o)) ? 0 : -1;
    }

    @Override
    public int lastIndexOf(final @Nullable Object o) {
        return indexOf(o);
    }

    @Override
    public @NotNull ImmutableListIterator<E> listIterator(final int index) {
        return new ElementIterator(Checks.cursorIndex(index, 1));
    }

    @Override
    public @NotNull Spliterator<E> spliterator() {
        return new ElementSpliterator();
    }

    @Override
    public void forEach(final @Nullable Consumer<? super E> consumer) {
        Checks.notNull(consumer, "Consumer");
        consumer.accept(element);
    }

    @Override
    public @NotNull ImmutableList<E> subList(final int fromIndex, final int toIndex) {
        Checks.indexRange(fromIndex, toIndex, 1);
        return (toIndex == fromIndex) ? ImmutableList.of() : this;
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

        return (that.size() == 1) && element.equals(that.get(0));
    }

    @Override
    public int hashCode() {
        return 31 + element.hashCode();
    }

    @Override
    public @NotNull String toString() {
        return "[" + element + "]";
    }

    private class ElementIterator implements ImmutableListIterator<E> {

        private int index;

        ElementIterator(final int index) {
            this.index = index;
        }

        @Override
        public boolean hasNext() {
            return index == 0;
        }

        @Override
        public @NotNull E next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            index = 1;
            return element;
        }

        @Override
        public int nextIndex() {
            return index;
        }

        @Override
        public boolean hasPrevious() {
            return index == 1;
        }

        @Override
        public @NotNull E previous() {
            if (!hasPrevious()) {
                throw new NoSuchElementException();
            }
            index = 0;
            return element;
        }

        @Override
        public int previousIndex() {
            return index - 1;
        }

        @Override
        public void forEachRemaining(final @Nullable Consumer<? super E> consumer) {
            Checks.notNull(consumer, "Consumer");
            if (hasNext()) {
                consumer.accept(next());
            }
        }
    }

    private class ElementSpliterator implements Spliterator<E> {

        private int size = 1;

        @Override
        public boolean tryAdvance(final @Nullable Consumer<? super E> consumer) {
            Checks.notNull(consumer, "Consumer");
            if (size == 1) {
                consumer.accept(element);
                size = 0;
                return true;
            }
            return false;
        }

        @Override
        public @Nullable Spliterator<E> trySplit() {
            return null;
        }

        @Override
        public long estimateSize() {
            return size;
        }

        @Override
        public long getExactSizeIfKnown() {
            return size;
        }

        @Override
        public int characteristics() {
            return Spliterator.SIZED | Spliterator.SUBSIZED | Spliterator.IMMUTABLE | Spliterator.NONNULL |
                    Spliterator.DISTINCT | Spliterator.ORDERED;
        }

        @Override
        public void forEachRemaining(final @Nullable Consumer<? super E> consumer) {
            Checks.notNull(consumer, "Consumer");
            tryAdvance(consumer);
        }
    }
}
