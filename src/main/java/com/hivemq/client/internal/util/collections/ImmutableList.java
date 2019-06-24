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

import com.hivemq.client.annotations.Immutable;
import com.hivemq.client.internal.util.Checks;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * @author Silvio Giebl
 */
@Immutable
public interface ImmutableList<@NotNull E> extends List<E>, RandomAccess {

    static <E> @NotNull ImmutableList<E> of() {
        return ImmutableEmptyList.of();
    }

    @Contract("null -> fail")
    static <E> @NotNull ImmutableList<E> of(final @Nullable E e) {
        return ImmutableElement.of(e);
    }

    @Contract("null, _ -> fail; _, null -> fail")
    static <E> @NotNull ImmutableList<E> of(final @Nullable E e1, final @Nullable E e2) {
        return ImmutableArray.of(e1, e2);
    }

    @Contract("null, _, _ -> fail; _, null, _ -> fail; _, _, null -> fail")
    static <E> @NotNull ImmutableList<E> of(final @Nullable E e1, final @Nullable E e2, final @Nullable E e3) {
        return ImmutableArray.of(e1, e2, e3);
    }

    @SafeVarargs
    @Contract("null, _, _, _ -> fail; _, null, _, _ -> fail; _, _, null, _ -> fail; _, _, _, null -> fail")
    static <E> @NotNull ImmutableList<E> of(
            final @Nullable E e1, final @Nullable E e2, final @Nullable E e3, final @Nullable E @Nullable ... others) {

        Checks.notNull(others, "Immutable list elements");
        final Object[] array = new Object[3 + others.length];
        array[0] = e1;
        array[1] = e2;
        array[2] = e3;
        System.arraycopy(others, 0, array, 3, others.length);
        return ImmutableArray.of(array);
    }

    @Contract("null -> fail")
    static <E> @NotNull ImmutableList<E> copyOf(final @Nullable E @Nullable [] elements) {
        return copyOf(elements, "Immutable list");
    }

    @Contract("null, _ -> fail")
    static <E> @NotNull ImmutableList<E> copyOf(final @Nullable E @Nullable [] elements, final @NotNull String name) {
        Checks.notNull(elements, name);
        switch (elements.length) {
            case 0:
                return ImmutableEmptyList.of();
            case 1:
                return ImmutableElement.of(elements[0], name);
            default:
                return ImmutableArray.of(Arrays.copyOf(elements, elements.length, Object[].class), name);
        }
    }

    @Contract("null -> fail")
    static <E> @NotNull ImmutableList<E> copyOf(final @Nullable Collection<@Nullable ? extends E> elements) {
        return copyOf(elements, "Immutable list");
    }

    @Contract("null, _ -> fail")
    static <E> @NotNull ImmutableList<E> copyOf(
            final @Nullable Collection<@Nullable ? extends E> elements, final @NotNull String name) {

        Checks.notNull(elements, name);
        if (elements instanceof ImmutableList) {
            //noinspection unchecked
            return ((ImmutableList<E>) elements).trim();
        }
        switch (elements.size()) {
            case 0:
                return ImmutableEmptyList.of();
            case 1:
                return ImmutableElement.of(Builder.first(elements), name);
            default:
                return ImmutableArray.of(elements.toArray(), name);
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

        private static final int INITIAL_CAPACITY = 4;

        private @Nullable E e;
        private @NotNull Object @Nullable [] array;
        private int size;

        private Builder() {}

        private Builder(final int capacity) {
            if (capacity > 1) {
                array = new Object[capacity];
            }
        }

        private int newCapacity(final int capacity) {
            return capacity + (capacity >> 1);
        }

        private @NotNull Object @NotNull [] ensureCapacity(final int capacity) {
            assert capacity > 1;
            if (array == null) {
                array = new Object[Math.max(INITIAL_CAPACITY, capacity)];
            } else if (capacity > array.length) {
                array = Arrays.copyOf(array, Math.max(newCapacity(array.length), capacity), Object[].class);
            }
            if (this.e != null) {
                array[0] = this.e;
                this.e = null;
            }
            return array;
        }

        public void ensureFree(final int free) {
            final int newCapacity = size + free;
            if (newCapacity > 1) {
                ensureCapacity(newCapacity);
            }
        }

        public @NotNull Builder<E> add(final @NotNull E e) {
            Checks.notNull(e, "Immutable list element");
            if (size == 0) {
                this.e = e;
                size = 1;
            } else {
                final int newSize = size + 1;
                ensureCapacity(newSize)[size] = e;
                size = newSize;
            }
            return this;
        }

        public @NotNull Builder<E> addAll(final @NotNull Collection<@NotNull ? extends E> elements) {
            Checks.notNull(elements, "Immutable list elements");
            final int elementsSize = elements.size();
            switch (elementsSize) {
                case 0:
                    break;
                case 1:
                    add(first(elements));
                    break;
                default:
                    final int newSize = size + elementsSize;
                    final Object[] array = ensureCapacity(newSize);
                    if ((elements instanceof List) && (elements instanceof RandomAccess)) {
                        //noinspection unchecked
                        final List<? extends E> list = (List<? extends E>) elements;
                        for (int i = 0; i < elementsSize; i++) {
                            array[size + i] = Checks.notNull(list.get(i), "Immutable list");
                        }
                    } else {
                        int i = size;
                        for (final E e : elements) {
                            array[i++] = Checks.notNull(e, "Immutable list");
                        }
                    }
                    size = newSize;
            }
            return this;
        }

        public int getSize() {
            return size;
        }

        public @NotNull ImmutableList<E> build() {
            switch (size) {
                case 0:
                    return ImmutableEmptyList.of();
                case 1:
                    assert e != null;
                    return new ImmutableElement<>(e);
                default:
                    assert array != null;
                    if (array.length == size) {
                        return new ImmutableArray<>(array);
                    }
                    return new ImmutableArray<>(Arrays.copyOfRange(array, 0, size, Object[].class));
            }
        }

        static <E> @NotNull E first(final @NotNull Collection<@NotNull E> elements) {
            return (elements instanceof List) ? ((List<E>) elements).get(0) : elements.iterator().next();
        }
    }
}
