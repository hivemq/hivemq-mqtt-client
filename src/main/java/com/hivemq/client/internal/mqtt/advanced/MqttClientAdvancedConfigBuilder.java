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

package com.hivemq.client.internal.mqtt.advanced;

import com.hivemq.client.internal.mqtt.advanced.interceptor.MqttClientInterceptors;
import com.hivemq.client.internal.mqtt.advanced.interceptor.MqttClientInterceptorsBuilder;
import com.hivemq.client.internal.util.Checks;
import com.hivemq.client.mqtt.mqtt5.advanced.Mqtt5ClientAdvancedConfigBuilder;
import com.hivemq.client.mqtt.mqtt5.advanced.interceptor.Mqtt5ClientInterceptors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * @author Silvio Giebl
 */
public abstract class MqttClientAdvancedConfigBuilder<B extends MqttClientAdvancedConfigBuilder<B>> {

    private boolean allowServerReAuth;
    private boolean validatePayloadFormat;
    private @Nullable MqttClientInterceptors interceptors;

    MqttClientAdvancedConfigBuilder() {}

    MqttClientAdvancedConfigBuilder(final @NotNull MqttClientAdvancedConfig advancedConfig) {
        allowServerReAuth = advancedConfig.isAllowServerReAuth();
        validatePayloadFormat = advancedConfig.isValidatePayloadFormat();
        interceptors = advancedConfig.getInterceptors();
    }

    abstract @NotNull B self();

    public @NotNull B allowServerReAuth(final boolean allowServerReAuth) {
        this.allowServerReAuth = allowServerReAuth;
        return self();
    }

    public @NotNull B validatePayloadFormat(final boolean validatePayloadFormat) {
        this.validatePayloadFormat = validatePayloadFormat;
        return self();
    }

    public @NotNull B interceptors(final @Nullable Mqtt5ClientInterceptors interceptors) {
        this.interceptors = Checks.notImplementedOrNull(interceptors, MqttClientInterceptors.class, "Interceptors");
        return self();
    }

    public MqttClientInterceptorsBuilder.@NotNull Nested<B> interceptors() {
        return new MqttClientInterceptorsBuilder.Nested<>(interceptors, this::interceptors);
    }

    public @NotNull MqttClientAdvancedConfig build() {
        return new MqttClientAdvancedConfig(allowServerReAuth, validatePayloadFormat, interceptors);
    }

    public static class Default extends MqttClientAdvancedConfigBuilder<Default>
            implements Mqtt5ClientAdvancedConfigBuilder {

        public Default() {}

        Default(final @NotNull MqttClientAdvancedConfig advancedConfig) {
            super(advancedConfig);
        }

        @Override
        @NotNull Default self() {
            return this;
        }
    }

    public static class Nested<P> extends MqttClientAdvancedConfigBuilder<Nested<P>>
            implements Mqtt5ClientAdvancedConfigBuilder.Nested<P> {

        private final @NotNull Function<? super MqttClientAdvancedConfig, P> parentConsumer;

        public Nested(
                final @NotNull MqttClientAdvancedConfig advancedConfig,
                final @NotNull Function<? super MqttClientAdvancedConfig, P> parentConsumer) {

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
