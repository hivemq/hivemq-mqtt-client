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

package org.mqttbee.mqtt.advanced;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.api.mqtt.mqtt5.advanced.Mqtt5AdvancedClientDataBuilder;
import org.mqttbee.api.mqtt.mqtt5.advanced.qos1.Mqtt5IncomingQos1ControlProvider;
import org.mqttbee.api.mqtt.mqtt5.advanced.qos1.Mqtt5OutgoingQos1ControlProvider;
import org.mqttbee.api.mqtt.mqtt5.advanced.qos2.Mqtt5IncomingQos2ControlProvider;
import org.mqttbee.api.mqtt.mqtt5.advanced.qos2.Mqtt5OutgoingQos2ControlProvider;

import java.util.function.Function;

/**
 * @author Silvio Giebl
 */
public abstract class MqttAdvancedClientDataBuilder<B extends MqttAdvancedClientDataBuilder<B>> {

    private @Nullable Mqtt5IncomingQos1ControlProvider incomingQos1ControlProvider;
    private @Nullable Mqtt5OutgoingQos1ControlProvider outgoingQos1ControlProvider;
    private @Nullable Mqtt5IncomingQos2ControlProvider incomingQos2ControlProvider;
    private @Nullable Mqtt5OutgoingQos2ControlProvider outgoingQos2ControlProvider;

    abstract @NotNull B self();

    public @NotNull B incomingQos1ControlProvider(
            final @Nullable Mqtt5IncomingQos1ControlProvider incomingQos1ControlProvider) {

        this.incomingQos1ControlProvider = incomingQos1ControlProvider;
        return self();
    }

    public @NotNull B outgoingQos1ControlProvider(
            final @Nullable Mqtt5OutgoingQos1ControlProvider outgoingQos1ControlProvider) {

        this.outgoingQos1ControlProvider = outgoingQos1ControlProvider;
        return self();
    }

    public @NotNull B incomingQos2ControlProvider(
            final @Nullable Mqtt5IncomingQos2ControlProvider incomingQos2ControlProvider) {

        this.incomingQos2ControlProvider = incomingQos2ControlProvider;
        return self();
    }

    public @NotNull B outgoingQos2ControlProvider(
            final @Nullable Mqtt5OutgoingQos2ControlProvider outgoingQos2ControlProvider) {

        this.outgoingQos2ControlProvider = outgoingQos2ControlProvider;
        return self();
    }

    public @NotNull MqttAdvancedClientData build() {
        return new MqttAdvancedClientData(incomingQos1ControlProvider, outgoingQos1ControlProvider,
                incomingQos2ControlProvider, outgoingQos2ControlProvider);
    }

    public static class Default extends MqttAdvancedClientDataBuilder<Default>
            implements Mqtt5AdvancedClientDataBuilder {

        @Override
        @NotNull Default self() {
            return this;
        }
    }

    public static class Nested<P> extends MqttAdvancedClientDataBuilder<Nested<P>>
            implements Mqtt5AdvancedClientDataBuilder.Nested<P> {

        private final @NotNull Function<? super MqttAdvancedClientData, P> parentConsumer;

        public Nested(final @NotNull Function<? super MqttAdvancedClientData, P> parentConsumer) {
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
