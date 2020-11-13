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

package com.hivemq.client.util;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Util to enable switching over types.
 * <p>
 * Example:
 * <pre>
 * {@code
 * Mqtt5MessageException e;
 * TypeSwitch.when(e)
 *         .is(Mqtt5ConnAckException.class, c -> System.out.println(c.getMqttMessage().getReasonCode()))
 *         .is(Mqtt5DisconnectException.class, d -> System.out.println(d.getMqttMessage().getServerReference()));
 * }
 * </pre>
 *
 * @param <T> the super type to switch over.
 * @author Silvio Giebl
 */
@ApiStatus.NonExtendable
public interface TypeSwitch<T> {

    /**
     * Returns a TypeSwitch object which does not match any type.
     *
     * @param <T> the super type to switch over.
     * @return the TypeSwitch object.
     */
    static <T> @NotNull TypeSwitch<T> never() {
        //noinspection unchecked
        return (TypeSwitch<T>) Never.INSTANCE;
    }

    /**
     * Returns a TypeSwitch object for switching over an object of type <code>T</code>.
     *
     * @param t   the object of type <code>T</code>.
     * @param <T> the super type to switch over.
     * @return the TypeSwitch object.
     */
    static <T> @NotNull TypeSwitch<T> when(final @Nullable T t) {
        return (t == null) ? never() : new TypeSwitch.Default<>(t);
    }

    /**
     * Checks if the object that is switched over is of a given type and if so executes a callback.
     * <p>
     * If the type matches the returned TypeSwitch will not match any further type.
     *
     * @param type     the class of the type to check.
     * @param consumer the callback to execute if the type matches.
     * @param <I>      the type to check
     * @return the TypeSwitch object.
     */
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

        static final @NotNull Never INSTANCE = new Never();

        private Never() {}

        @Override
        public <I> @NotNull Never is(final @NotNull Class<I> type, final @NotNull Consumer<I> consumer) {
            return this;
        }
    }
}
