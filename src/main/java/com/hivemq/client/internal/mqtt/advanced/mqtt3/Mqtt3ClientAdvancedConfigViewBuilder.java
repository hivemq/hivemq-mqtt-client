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

package com.hivemq.client.internal.mqtt.advanced.mqtt3;

import com.hivemq.client.internal.util.Checks;
import com.hivemq.client.mqtt.mqtt3.advanced.Mqtt3ClientAdvancedConfigBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * @author Abdullah Imal
 */
public abstract class Mqtt3ClientAdvancedConfigViewBuilder<B extends Mqtt3ClientAdvancedConfigViewBuilder<B>> {

    private int maxConcurrentPublishes = Mqtt3ClientAdvancedConfigView.DEFAULT.maxConcurrentPublishes();

    Mqtt3ClientAdvancedConfigViewBuilder() {}

    Mqtt3ClientAdvancedConfigViewBuilder(final @NotNull Mqtt3ClientAdvancedConfigView advancedConfig) {
        maxConcurrentPublishes = advancedConfig.maxConcurrentPublishes();
    }

    abstract @NotNull B self();

    public @NotNull B maxConcurrentPublishes(final int maxConcurrentPublishes) {
        this.maxConcurrentPublishes =
                (int) Checks.range(maxConcurrentPublishes, 1, Integer.MAX_VALUE, "Maximum concurrent publishes");
        return self();
    }

    public @NotNull Mqtt3ClientAdvancedConfigView build() {
        return Mqtt3ClientAdvancedConfigView.of(maxConcurrentPublishes);
    }

    public static class Default extends Mqtt3ClientAdvancedConfigViewBuilder<Default>
            implements Mqtt3ClientAdvancedConfigBuilder {

        public Default() {}

        Default(final @NotNull Mqtt3ClientAdvancedConfigView advancedConfig) {
            super(advancedConfig);
        }

        @Override
        @NotNull Default self() {
            return this;
        }
    }

    public static class Nested<P> extends Mqtt3ClientAdvancedConfigViewBuilder<Nested<P>>
            implements Mqtt3ClientAdvancedConfigBuilder.Nested<P> {

        private final @NotNull Function<? super Mqtt3ClientAdvancedConfigView, P> parentConsumer;

        public Nested(
                final @NotNull Mqtt3ClientAdvancedConfigView advancedConfig,
                final @NotNull Function<? super Mqtt3ClientAdvancedConfigView, P> parentConsumer) {

            super(advancedConfig);
            this.parentConsumer = parentConsumer;
        }

        @Override
        @NotNull Nested<P> self() {
            return this;
        }

        @Override
        public @NotNull P applyAdvancedConfig() {
            return parentConsumer.apply(build());
        }
    }
}
