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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.annotations.Immutable;
import org.mqttbee.util.Checks;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * @author Silvio Giebl
 */
@Immutable
public interface ImmutableList<E> extends List<E>, RandomAccess {

    static <E> @NotNull ImmutableList<@NotNull E> of() {
        return ImmutableEmptyList.of();
    }

    static <E> @NotNull ImmutableList<@NotNull E> of(final @NotNull E e) {
        return ImmutableElement.of(e);
    }

    static <E> @NotNull ImmutableList<@NotNull E> of(final @NotNull E e1, final @NotNull E e2) {
        return ImmutableArray.of(e1, e2);
    }

    static <E> @NotNull ImmutableList<@NotNull E> of(final @NotNull E e1, final @NotNull E e2, final @NotNull E e3) {
        return ImmutableArray.of(e1, e2, e3);
    }

    static <E> @NotNull ImmutableList<@NotNull E> copyOf(final @NotNull E @NotNull [] elements) {
        Checks.notNull(elements, "Immutable list elements");
        switch (elements.length) {
            case 0:
                return ImmutableEmptyList.of();
            case 1:
                return ImmutableElement.of(elements[0]);
            default:
                return ImmutableArray.of(Arrays.copyOf(elements, elements.length, Object[].class));
        }
    }

    static <E> @NotNull ImmutableList<@NotNull E> copyOf(final @NotNull Collection<? extends @NotNull E> elements) {
        Checks.notNull(elements, "Immutable list elements");
        if (elements instanceof ImmutableList) {
            //noinspection unchecked
            return ((ImmutableList<E>) elements).trim();
        }
        switch (elements.size()) {
            case 0:
                return ImmutableEmptyList.of();
            case 1:
                //noinspection unchecked
                return ImmutableElement.of((elements instanceof List) ? ((List<? extends E>) elements).get(0) :
                        elements.iterator().next());
            default:
                return ImmutableArray.of(elements.toArray());
        }
    }

    static <E> @NotNull Builder<E> builder() {
        return new Builder<>();
    }

    static <E> @NotNull Builder<E> builder(final int capacity) {
        return new Builder<>(capacity);
    }

    @Override
    default boolean isEmpty() {
        return size() == 0;
    }

    @Override
    @NotNull E get(int index);

    @Override
    default boolean contains(final @Nullable Object o) {
        return indexOf(o) >= 0;
    }

    @Override
    default boolean containsAll(final @NotNull Collection<?> c) {
        Checks.notNull(c, "Collection");
        for (final Object o : c) {
            if (!contains(o)) {
                return false;
            }
        }
        return true;
    }

    @Override
    default @NotNull ImmutableListIterator<E> iterator() {
        return listIterator();
    }

    @Override
    default @NotNull ImmutableListIterator<E> listIterator() {
        return listIterator(0);
    }

    @Override
    @NotNull ImmutableListIterator<E> listIterator(int index);

    @Override
    @NotNull ImmutableList<E> subList(int fromIndex, int toIndex);

    default @NotNull ImmutableList<E> trim() {
        return this;
    }

    @Override
    @Deprecated
    default boolean add(final @Nullable E e) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    default boolean remove(final @Nullable Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    default boolean addAll(final @NotNull Collection<? extends E> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    default boolean removeAll(final @NotNull Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    default boolean retainAll(final @NotNull Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    default boolean removeIf(final @NotNull Predicate<? super E> filter) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    default void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    default void add(final int index, final @Nullable E element) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    default @Nullable E remove(final int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    default @Nullable E set(final int index, final @Nullable E element) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    default boolean addAll(final int index, final @NotNull Collection<? extends E> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    default void replaceAll(final @NotNull UnaryOperator<E> operator) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    default void sort(final Comparator<? super E> c) {
        throw new UnsupportedOperationException();
    }

    interface ImmutableListIterator<E> extends ListIterator<E> {

        @Override
        @Deprecated
        default void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        @Deprecated
        default void set(final @Nullable E e) {
            throw new UnsupportedOperationException();
        }

        @Override
        @Deprecated
        default void add(final @Nullable E e) {
            throw new UnsupportedOperationException();
        }
    }

    class Builder<E> {

        private @Nullable E e;
        private @NotNull Object @Nullable [] elements;
        private int size;

        private Builder() {}

        private Builder(final int capacity) {
            if (capacity > 1) {
                elements = new Object[capacity];
            }
        }

        public @NotNull Builder<E> add(final @NotNull E e) {
            Checks.notNull(e, "Immutable list element");
            if (this.e == null) {
                this.e = e;
                size = 1;
            } else {
                if (elements == null) {
                    elements = new Object[4];
                } else if (elements.length == size) {
                    final int newLength = elements.length + (elements.length >> 1);
                    elements = Arrays.copyOf(elements, newLength, Object[].class);
                }
                if (size == 1) {
                    elements[0] = this.e;
                }
                elements[size++] = e;
            }
            return this;
        }

        public @NotNull ImmutableList<@NotNull E> build() {
            if (e == null) {
                return ImmutableEmptyList.of();
            }
            if (elements == null) {
                return new ImmutableElement<>(e);
            }
            if (elements.length == size) {
                return new ImmutableArray<>(elements);
            }
            return new ImmutableArray<>(Arrays.copyOfRange(elements, 0, size, Object[].class));
        }
    }
}
