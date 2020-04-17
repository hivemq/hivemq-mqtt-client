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

package com.hivemq.client.internal.mqtt.lifecycle;

import com.hivemq.client.internal.util.Checks;
import com.hivemq.client.mqtt.lifecycle.MqttClientAutoReconnectBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * @author Silvio Giebl
 */
public abstract class MqttClientAutoReconnectImplBuilder<B extends MqttClientAutoReconnectImplBuilder<B>> {

    private long initialDelayNanos = MqttClientAutoReconnectImpl.DEFAULT_START_DELAY_NANOS;
    private long maxDelayNanos = MqttClientAutoReconnectImpl.DEFAULT_MAX_DELAY_NANOS;

    MqttClientAutoReconnectImplBuilder() {}

    MqttClientAutoReconnectImplBuilder(final @Nullable MqttClientAutoReconnectImpl autoReconnect) {
        if (autoReconnect != null) {
            initialDelayNanos = autoReconnect.getInitialDelay(TimeUnit.NANOSECONDS);
            maxDelayNanos = autoReconnect.getMaxDelay(TimeUnit.NANOSECONDS);
        }
    }

    abstract @NotNull B self();

    public @NotNull B initialDelay(final long initialDelay, final @Nullable TimeUnit timeUnit) {
        if (initialDelay <= 0) {
            throw new IllegalArgumentException("Initial delay must be positive.");
        }
        Checks.notNull(timeUnit, "Time unit");
        this.initialDelayNanos = timeUnit.toNanos(initialDelay);
        return self();
    }

    public @NotNull B maxDelay(final long maxDelay, final @Nullable TimeUnit timeUnit) {
        if (maxDelay < 0) {
            throw new IllegalArgumentException("Maximum delay must be positive or zero.");
        }
        Checks.notNull(timeUnit, "Time unit");
        this.maxDelayNanos = timeUnit.toNanos(maxDelay);
        return self();
    }

    public @NotNull MqttClientAutoReconnectImpl build() {
        return new MqttClientAutoReconnectImpl(initialDelayNanos, maxDelayNanos);
    }

    public static class Default extends MqttClientAutoReconnectImplBuilder<Default>
            implements MqttClientAutoReconnectBuilder {

        public Default() {}

        Default(final @Nullable MqttClientAutoReconnectImpl autoReconnect) {
            super(autoReconnect);
        }

        @Override
        @NotNull Default self() {
            return this;
        }
    }

    public static class Nested<P> extends MqttClientAutoReconnectImplBuilder<Nested<P>>
            implements MqttClientAutoReconnectBuilder.Nested<P> {

        private final @NotNull Function<? super MqttClientAutoReconnectImpl, P> parentConsumer;

        public Nested(
                final @Nullable MqttClientAutoReconnectImpl autoReconnect,
                final @NotNull Function<? super MqttClientAutoReconnectImpl, P> parentConsumer) {

            super(autoReconnect);
            this.parentConsumer = parentConsumer;
        }

        @Override
        @NotNull Nested<P> self() {
            return this;
        }

        @Override
        public @NotNull P applyAutomaticReconnect() {
            return parentConsumer.apply(build());
        }
    }
}
