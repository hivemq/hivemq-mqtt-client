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

package com.hivemq.client2.internal.mqtt.advanced.interceptor;

import com.hivemq.client2.mqtt.mqtt5.advanced.interceptor.Mqtt5ClientInterceptorsBuilder;
import com.hivemq.client2.mqtt.mqtt5.advanced.interceptor.qos1.Mqtt5InboundQos1Interceptor;
import com.hivemq.client2.mqtt.mqtt5.advanced.interceptor.qos1.Mqtt5OutboundQos1Interceptor;
import com.hivemq.client2.mqtt.mqtt5.advanced.interceptor.qos2.Mqtt5InboundQos2Interceptor;
import com.hivemq.client2.mqtt.mqtt5.advanced.interceptor.qos2.Mqtt5OutboundQos2Interceptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * @author Silvio Giebl
 */
public abstract class MqttClientInterceptorsBuilder<B extends MqttClientInterceptorsBuilder<B>> {

    private @Nullable Mqtt5InboundQos1Interceptor inboundQos1Interceptor;
    private @Nullable Mqtt5OutboundQos1Interceptor outboundQos1Interceptor;
    private @Nullable Mqtt5InboundQos2Interceptor inboundQos2Interceptor;
    private @Nullable Mqtt5OutboundQos2Interceptor outboundQos2Interceptor;

    MqttClientInterceptorsBuilder() {}

    MqttClientInterceptorsBuilder(final @Nullable MqttClientInterceptors interceptors) {
        if (interceptors != null) {
            inboundQos1Interceptor = interceptors.getInboundQos1Interceptor();
            outboundQos1Interceptor = interceptors.getOutboundQos1Interceptor();
            inboundQos2Interceptor = interceptors.getInboundQos2Interceptor();
            outboundQos2Interceptor = interceptors.getOutboundQos2Interceptor();
        }
    }

    abstract @NotNull B self();

    public @NotNull B inboundQos1Interceptor(final @Nullable Mqtt5InboundQos1Interceptor inboundQos1Interceptor) {
        this.inboundQos1Interceptor = inboundQos1Interceptor;
        return self();
    }

    public @NotNull B outboundQos1Interceptor(final @Nullable Mqtt5OutboundQos1Interceptor outboundQos1Interceptor) {
        this.outboundQos1Interceptor = outboundQos1Interceptor;
        return self();
    }

    public @NotNull B inboundQos2Interceptor(final @Nullable Mqtt5InboundQos2Interceptor inboundQos2Interceptor) {
        this.inboundQos2Interceptor = inboundQos2Interceptor;
        return self();
    }

    public @NotNull B outboundQos2Interceptor(final @Nullable Mqtt5OutboundQos2Interceptor outboundQos2Interceptor) {
        this.outboundQos2Interceptor = outboundQos2Interceptor;
        return self();
    }

    public @NotNull MqttClientInterceptors build() {
        return new MqttClientInterceptors(inboundQos1Interceptor, outboundQos1Interceptor, inboundQos2Interceptor,
                outboundQos2Interceptor);
    }

    public static class Default extends MqttClientInterceptorsBuilder<Default>
            implements Mqtt5ClientInterceptorsBuilder {

        public Default() {}

        Default(final @Nullable MqttClientInterceptors interceptors) {
            super(interceptors);
        }

        @Override
        @NotNull Default self() {
            return this;
        }
    }

    public static class Nested<P> extends MqttClientInterceptorsBuilder<Nested<P>>
            implements Mqtt5ClientInterceptorsBuilder.Nested<P> {

        private final @NotNull Function<? super MqttClientInterceptors, P> parentConsumer;

        public Nested(
                final @Nullable MqttClientInterceptors interceptors,
                final @NotNull Function<? super MqttClientInterceptors, P> parentConsumer) {

            super(interceptors);
            this.parentConsumer = parentConsumer;
        }

        @Override
        @NotNull Nested<P> self() {
            return this;
        }

        @Override
        public @NotNull P applyInterceptors() {
            return parentConsumer.apply(build());
        }
    }
}
