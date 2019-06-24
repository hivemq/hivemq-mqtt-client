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

package com.hivemq.client.internal.util;

import com.hivemq.client.internal.util.collections.ImmutableList;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Silvio Giebl
 */
public final class Checks {

    @Contract("null, _ -> fail")
    public static <T> @NotNull T notNull(final @Nullable T object, final @NotNull String name) {
        if (object == null) {
            throw new NullPointerException(name + " must not be null.");
        }
        return object;
    }

    @Contract("null, _ -> fail")
    public static @NotNull String notEmpty(final @Nullable String string, final @NotNull String name) {
        notNull(string, name);
        if (string.isEmpty()) {
            throw new IllegalArgumentException(name + " must be at least one character long.");
        }
        return string;
    }

    @Contract("null, _, _ -> fail")
    public static <S, T extends S> @NotNull T notImplemented(
            final @Nullable S object, final @NotNull Class<T> type, final @NotNull String name) {

        return notImplementedInternal(notNull(object, name), type, name);
    }

    @Contract("null, _, _ -> null")
    public static <S, T extends S> @Nullable T notImplementedOrNull(
            final @Nullable S object, final @NotNull Class<T> type, final @NotNull String name) {

        return (object == null) ? null : notImplementedInternal(object, type, name);
    }

    private static <S, T extends S> @NotNull T notImplementedInternal(
            final @NotNull S object, final @NotNull Class<T> type, final @NotNull String name) {

        if (!type.isInstance(object)) {
            throw new IllegalArgumentException(name + " must not be implemented by the user, but was implemented by " +
                    object.getClass().getTypeName() + ".");
        }
        //noinspection unchecked
        return (T) object;
    }

    @Contract("null, _ -> fail")
    public static <T> @NotNull T @NotNull [] elementsNotNull(
            final @Nullable T @Nullable [] array, final @NotNull String name) {

        notNull(array, name);
        for (int i = 0; i < array.length; i++) {
            elementNotNull(array[i], name, i);
        }
        //noinspection NullableProblems
        return array;
    }

    @Contract("null, _, _ -> fail")
    public static <E> @NotNull E elementNotNull(final @Nullable E e, final @NotNull String name, final int index) {
        if (e == null) {
            throw new NullPointerException(name + " must not contain a null element, found at index " + index + ".");
        }
        return e;
    }

    public static <S, T extends S> @NotNull ImmutableList<T> elementsNotImplemented(
            final @NotNull ImmutableList<S> list, final @NotNull Class<T> type, final @NotNull String name) {

        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < list.size(); i++) {
            notImplementedInternal(list.get(i), type, name);
        }
        //noinspection unchecked
        return (ImmutableList<T>) list;
    }

    public static int unsignedShort(final int value, final @NotNull String name) {
        if (!UnsignedDataTypes.isUnsignedShort(value)) {
            throw new IllegalArgumentException(name + " must not exceed the value range of unsigned short [0, " +
                    UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE + "], but was " + value + ".");
        }
        return value;
    }

    public static int unsignedShortNotZero(final int value, final @NotNull String name) {
        if (value == 0) {
            throw new IllegalArgumentException(name + " must not be zero.");
        }
        unsignedShort(value, name);
        return value;
    }

    public static long unsignedInt(final long value, final @NotNull String name) {
        if (!UnsignedDataTypes.isUnsignedInt(value)) {
            throw new IllegalArgumentException(name + " must not exceed the value range of unsigned int [0, " +
                    UnsignedDataTypes.UNSIGNED_INT_MAX_VALUE + "], but was " + value + ".");
        }
        return value;
    }

    public static int index(final int index, final int size) {
        if ((index < 0) || (index >= size)) {
            if (index < 0) {
                throw new IndexOutOfBoundsException("Index must not be smaller than 0, but was " + index + ".");
            } else {
                throw new IndexOutOfBoundsException(
                        "Index must not be greater than or equal to the size (" + size + "), but was " + index + ".");
            }
        }
        return index;
    }

    public static int cursorIndex(final int index, final int size) {
        if ((index < 0) || (index > size)) {
            if (index < 0) {
                throw new IndexOutOfBoundsException("Cursor index must not be smaller than 0, but was " + index + ".");
            } else {
                throw new IndexOutOfBoundsException(
                        "Cursor index must not be greater than the size (" + size + "), but was " + index + ".");
            }
        }
        return index;
    }

    public static void indexRange(final int start, final int end, final int size) {
        if ((start < 0) || (start > end) || (end > size)) {
            if (start < 0) {
                throw new IndexOutOfBoundsException("Start index must not be smaller than 0, but was " + start + ".");
            } else if (start > end) {
                throw new IndexOutOfBoundsException(
                        "Start index must be greater than the end index, but " + start + " > " + end + ".");
            } else {
                throw new IndexOutOfBoundsException(
                        "End index must not be greater than or equal to the size (" + size + "), but was " + end + ".");
            }
        }
    }

    @Contract("false, _ -> fail")
    public static void state(final boolean condition, final @NotNull String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    public static <T> @NotNull T stateNotNull(final @Nullable T object, final @NotNull String name) {
        if (object == null) {
            throw new IllegalStateException(name + " must not be null. This must not happen and is a bug.");
        }
        return object;
    }

    private Checks() {}
}
