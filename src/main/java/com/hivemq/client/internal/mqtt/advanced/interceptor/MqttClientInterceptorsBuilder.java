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

package com.hivemq.client.internal.mqtt.advanced.interceptor;

import com.hivemq.client.mqtt.mqtt5.advanced.interceptor.Mqtt5ClientInterceptorsBuilder;
import com.hivemq.client.mqtt.mqtt5.advanced.interceptor.qos1.Mqtt5IncomingQos1Interceptor;
import com.hivemq.client.mqtt.mqtt5.advanced.interceptor.qos1.Mqtt5OutgoingQos1Interceptor;
import com.hivemq.client.mqtt.mqtt5.advanced.interceptor.qos2.Mqtt5IncomingQos2Interceptor;
import com.hivemq.client.mqtt.mqtt5.advanced.interceptor.qos2.Mqtt5OutgoingQos2Interceptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * @author Silvio Giebl
 */
public abstract class MqttClientInterceptorsBuilder<B extends MqttClientInterceptorsBuilder<B>> {

    private @Nullable Mqtt5IncomingQos1Interceptor incomingQos1Interceptor;
    private @Nullable Mqtt5OutgoingQos1Interceptor outgoingQos1Interceptor;
    private @Nullable Mqtt5IncomingQos2Interceptor incomingQos2Interceptor;
    private @Nullable Mqtt5OutgoingQos2Interceptor outgoingQos2Interceptor;

    MqttClientInterceptorsBuilder() {}

    MqttClientInterceptorsBuilder(final @Nullable MqttClientInterceptors interceptors) {
        if (interceptors != null) {
            incomingQos1Interceptor = interceptors.getIncomingQos1Interceptor();
            outgoingQos1Interceptor = interceptors.getOutgoingQos1Interceptor();
            incomingQos2Interceptor = interceptors.getIncomingQos2Interceptor();
            outgoingQos2Interceptor = interceptors.getOutgoingQos2Interceptor();
        }
    }

    abstract @NotNull B self();

    public @NotNull B incomingQos1Interceptor(final @Nullable Mqtt5IncomingQos1Interceptor incomingQos1Interceptor) {
        this.incomingQos1Interceptor = incomingQos1Interceptor;
        return self();
    }

    public @NotNull B outgoingQos1Interceptor(final @Nullable Mqtt5OutgoingQos1Interceptor outgoingQos1Interceptor) {
        this.outgoingQos1Interceptor = outgoingQos1Interceptor;
        return self();
    }

    public @NotNull B incomingQos2Interceptor(final @Nullable Mqtt5IncomingQos2Interceptor incomingQos2Interceptor) {
        this.incomingQos2Interceptor = incomingQos2Interceptor;
        return self();
    }

    public @NotNull B outgoingQos2Interceptor(final @Nullable Mqtt5OutgoingQos2Interceptor outgoingQos2Interceptor) {
        this.outgoingQos2Interceptor = outgoingQos2Interceptor;
        return self();
    }

    public @NotNull MqttClientInterceptors build() {
        return new MqttClientInterceptors(incomingQos1Interceptor, outgoingQos1Interceptor, incomingQos2Interceptor,
                outgoingQos2Interceptor);
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
