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

import java.util.function.Consumer;

/**
 * @author Silvio Giebl
 */
public interface TypeSwitch<T> {

    static <T> @NotNull TypeSwitch<T> never() {
        //noinspection unchecked
        return (TypeSwitch<T>) Never.INSTANCE;
    }

    static <T> @NotNull TypeSwitch<T> when(final @Nullable T t) {
        return (t == null) ? never() : new TypeSwitch.Default<>(t);
    }

    <I extends T> @NotNull TypeSwitch<T> is(final @NotNull Class<I> type, final @NotNull Consumer<I> consumer);

    class Default<T> implements TypeSwitch<T> {

        private final @Nullable T t;

        Default(final @Nullable T t) {
            this.t = t;
        }

        @Override
        public <I extends T> @NotNull TypeSwitch<T> is(
                final @NotNull Class<I> type, final @NotNull Consumer<I> consumer) {

            if (type.isInstance(t)) {
                //noinspection unchecked
                consumer.accept((I) t);
                return never();
            }
            return this;
        }
    }

    class Never implements TypeSwitch<Object> {

        static @NotNull Never INSTANCE = new Never();

        private Never() {}

        @Override
        public <I> @NotNull Never is(final @NotNull Class<I> type, final @NotNull Consumer<I> consumer) {
            return this;
        }
    }
}
