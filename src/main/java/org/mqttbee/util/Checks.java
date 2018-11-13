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

package org.mqttbee.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.RandomAccess;

/**
 * @author Silvio Giebl
 */
public class Checks {

    private Checks() {}

    public static <T> @NotNull T notNull(final @Nullable T object, final @NotNull String name) {
        if (object == null) {
            throw new NullPointerException(name + " must not be null.");
        }
        return object;
    }

    public static @NotNull String notEmpty(final @Nullable String string, final @NotNull String name) {
        notNull(string, name);
        if (string.isEmpty()) {
            throw new IllegalArgumentException(name + " must not be empty.");
        }
        return string;
    }

    public static <T> @Nullable List<@NotNull T> elementsNotNull(
            final @Nullable List<T> list, final @NotNull String name) {

        if (list == null) {
            return null;
        }
        if (list instanceof RandomAccess) {
            for (int i = 0; i < list.size(); i++) {
                elementNotNull(list.get(i), name, i);
            }
        } else {
            int i = 0;
            for (final T element : list) {
                elementNotNull(element, name, i);
                i++;
            }
        }
        return list;
    }

    private static void elementNotNull(final @Nullable Object element, final @NotNull String name, final int index) {
        if (element == null) {
            throw new NullPointerException(name + " must not contain a null element at index " + index);
        }
    }

    public static <S, T extends S> @NotNull T notImplemented(
            final @Nullable S object, final @NotNull Class<T> type, final @NotNull String name) {

        return notImplementedInternal(notNull(object, name), type, name);
    }

    public static <S, T extends S> @Nullable T notImplementedOrNull(
            final @Nullable S object, final @NotNull Class<T> type, final @NotNull String name) {

        return (object == null) ? null : notImplementedInternal(object, type, name);
    }

    @SuppressWarnings("unchecked")
    private static <T, I extends T> @NotNull I notImplementedInternal(
            final @NotNull T object, final @NotNull Class<I> type, final @NotNull String name) {

        if (!type.isInstance(object)) {
            throw new IllegalArgumentException(name + " must not be implemented by the user, but was implemented by " +
                    object.getClass().getTypeName());
        }
        return (I) object;
    }

    public static void state(final boolean condition, final @NotNull String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
