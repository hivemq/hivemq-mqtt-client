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

package org.mqttbee.internal.mqtt.handler.subscribe;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.internal.mqtt.handler.publish.incoming.MqttSubscriptionFlow;
import org.mqttbee.internal.mqtt.message.subscribe.MqttStatefulSubscribe;
import org.mqttbee.internal.mqtt.message.subscribe.MqttSubscribe;
import org.mqttbee.internal.mqtt.message.subscribe.suback.MqttSubAck;
import org.mqttbee.rx.SingleFlow;

/**
 * @author Silvio Giebl
 */
class MqttSubscribeWithFlow extends MqttSubOrUnsubWithFlow {

    private final @NotNull MqttSubscribe subscribe;
    private final @NotNull SingleFlow<MqttSubAck> subAckFlow;
    private final @Nullable MqttSubscriptionFlow subscriptionFlow;

    MqttSubscribeWithFlow(final @NotNull MqttSubscribe subscribe, final @NotNull SingleFlow<MqttSubAck> subAckFlow) {
        this.subscribe = subscribe;
        this.subAckFlow = subAckFlow;
        this.subscriptionFlow = null;
    }

    MqttSubscribeWithFlow(
            final @NotNull MqttSubscribe subscribe, final @NotNull MqttSubscriptionFlow subscriptionFlow) {

        this.subscribe = subscribe;
        this.subAckFlow = subscriptionFlow;
        this.subscriptionFlow = subscriptionFlow;
    }

    @Override
    @NotNull SingleFlow<MqttSubAck> getAckFlow() {
        return subAckFlow;
    }

    @NotNull Stateful createStateful(final int packetIdentifier, final int subscriptionIdentifier) {
        return new Stateful(
                subscribe.createStateful(packetIdentifier, subscriptionIdentifier), subAckFlow, subscriptionFlow);
    }

    static class Stateful extends MqttSubOrUnsubWithFlow.Stateful {

        private final @NotNull MqttStatefulSubscribe subscribe;
        private final @NotNull SingleFlow<MqttSubAck> subAckFlow;
        private final @Nullable MqttSubscriptionFlow subscriptionFlow;

        Stateful(
                final @NotNull MqttStatefulSubscribe subscribe, final @NotNull SingleFlow<MqttSubAck> subAckFlow,
                final @Nullable MqttSubscriptionFlow subscriptionFlow) {

            this.subscribe = subscribe;
            this.subAckFlow = subAckFlow;
            this.subscriptionFlow = subscriptionFlow;
        }

        @NotNull MqttStatefulSubscribe getSubscribe() {
            return subscribe;
        }

        @Override
        @NotNull SingleFlow<MqttSubAck> getAckFlow() {
            return subAckFlow;
        }

        @Nullable MqttSubscriptionFlow getSubscriptionFlow() {
            return subscriptionFlow;
        }
    }
}
