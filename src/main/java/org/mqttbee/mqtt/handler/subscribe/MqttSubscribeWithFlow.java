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

package org.mqttbee.mqtt.handler.subscribe;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.api.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAck;
import org.mqttbee.mqtt.handler.publish.incoming.MqttSubscriptionFlow;
import org.mqttbee.mqtt.message.subscribe.MqttStatefulSubscribe;
import org.mqttbee.mqtt.message.subscribe.MqttSubscribe;
import org.mqttbee.rx.SingleFlow;

/**
 * @author Silvio Giebl
 */
public class MqttSubscribeWithFlow {

    private final @NotNull MqttSubscribe subscribe;
    private final @NotNull SingleFlow<Mqtt5SubAck> subAckFlow;
    private final @Nullable MqttSubscriptionFlow subscriptionFlow;

    MqttSubscribeWithFlow(
            final @NotNull MqttSubscribe subscribe, final @NotNull SingleFlow<Mqtt5SubAck> subAckFlow) {

        this.subscribe = subscribe;
        this.subAckFlow = subAckFlow;
        this.subscriptionFlow = null;
    }

    public MqttSubscribeWithFlow(
            final @NotNull MqttSubscribe subscribe, final @NotNull MqttSubscriptionFlow subscriptionFlow) {

        this.subscribe = subscribe;
        this.subAckFlow = subscriptionFlow;
        this.subscriptionFlow = subscriptionFlow;
    }

    @NotNull SingleFlow<Mqtt5SubAck> getSubAckFlow() {
        return subAckFlow;
    }

    @NotNull MqttStatefulSubscribeWithFlow createStateful(
            final int packetIdentifier, final int subscriptionIdentifier) {
        return new MqttStatefulSubscribeWithFlow(
                subscribe.createStateful(packetIdentifier, subscriptionIdentifier), subAckFlow, subscriptionFlow);
    }

    static class MqttStatefulSubscribeWithFlow {

        private final @NotNull MqttStatefulSubscribe subscribe;
        private final @NotNull SingleFlow<Mqtt5SubAck> subAckFlow;
        private final @Nullable MqttSubscriptionFlow subscriptionFlow;

        MqttStatefulSubscribeWithFlow(
                final @NotNull MqttStatefulSubscribe subscribe, final @NotNull SingleFlow<Mqtt5SubAck> subAckFlow,
                final @Nullable MqttSubscriptionFlow subscriptionFlow) {

            this.subscribe = subscribe;
            this.subAckFlow = subAckFlow;
            this.subscriptionFlow = subscriptionFlow;
        }

        @NotNull MqttStatefulSubscribe getSubscribe() {
            return subscribe;
        }

        @NotNull SingleFlow<Mqtt5SubAck> getSubAckFlow() {
            return subAckFlow;
        }

        @Nullable MqttSubscriptionFlow getSubscriptionFlow() {
            return subscriptionFlow;
        }

    }

}
