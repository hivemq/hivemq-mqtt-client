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

package org.mqttbee.internal.mqtt.advanced;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.mqtt.mqtt5.advanced.Mqtt5ClientAdvancedConfigBuilder;
import org.mqttbee.mqtt.mqtt5.advanced.qos1.Mqtt5IncomingQos1Interceptor;
import org.mqttbee.mqtt.mqtt5.advanced.qos1.Mqtt5OutgoingQos1Interceptor;
import org.mqttbee.mqtt.mqtt5.advanced.qos2.Mqtt5IncomingQos2Interceptor;
import org.mqttbee.mqtt.mqtt5.advanced.qos2.Mqtt5OutgoingQos2Interceptor;

import java.util.function.Function;

/**
 * @author Silvio Giebl
 */
public abstract class MqttClientAdvancedConfigBuilder<B extends MqttClientAdvancedConfigBuilder<B>> {

    private @Nullable Mqtt5IncomingQos1Interceptor incomingQos1Interceptor;
    private @Nullable Mqtt5OutgoingQos1Interceptor outgoingQos1Interceptor;
    private @Nullable Mqtt5IncomingQos2Interceptor incomingQos2Interceptor;
    private @Nullable Mqtt5OutgoingQos2Interceptor outgoingQos2Interceptor;

    abstract @NotNull B self();

    public @NotNull B incomingQos1Interceptor(
            final @Nullable Mqtt5IncomingQos1Interceptor incomingQos1Interceptor) {

        this.incomingQos1Interceptor = incomingQos1Interceptor;
        return self();
    }

    public @NotNull B outgoingQos1Interceptor(
            final @Nullable Mqtt5OutgoingQos1Interceptor outgoingQos1Interceptor) {

        this.outgoingQos1Interceptor = outgoingQos1Interceptor;
        return self();
    }

    public @NotNull B incomingQos2Interceptor(
            final @Nullable Mqtt5IncomingQos2Interceptor incomingQos2Interceptor) {

        this.incomingQos2Interceptor = incomingQos2Interceptor;
        return self();
    }

    public @NotNull B outgoingQos2Interceptor(
            final @Nullable Mqtt5OutgoingQos2Interceptor outgoingQos2Interceptor) {

        this.outgoingQos2Interceptor = outgoingQos2Interceptor;
        return self();
    }

    public @NotNull MqttClientAdvancedConfig build() {
        return new MqttClientAdvancedConfig(incomingQos1Interceptor, outgoingQos1Interceptor, incomingQos2Interceptor,
                outgoingQos2Interceptor);
    }

    public static class Default extends MqttClientAdvancedConfigBuilder<Default>
            implements Mqtt5ClientAdvancedConfigBuilder {

        @Override
        @NotNull Default self() {
            return this;
        }
    }

    public static class Nested<P> extends MqttClientAdvancedConfigBuilder<Nested<P>>
            implements Mqtt5ClientAdvancedConfigBuilder.Nested<P> {

        private final @NotNull Function<? super MqttClientAdvancedConfig, P> parentConsumer;

        public Nested(final @NotNull Function<? super MqttClientAdvancedConfig, P> parentConsumer) {
            this.parentConsumer = parentConsumer;
        }

        @Override
        @NotNull Nested<P> self() {
            return this;
        }

        @Override
        public @NotNull P applyAdvanced() {
            return parentConsumer.apply(build());
        }
    }
}
