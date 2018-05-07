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

import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAck;
import org.mqttbee.mqtt.handler.publish.MqttSubscriptionFlow;
import org.mqttbee.mqtt.message.subscribe.MqttSubscribe;
import org.mqttbee.mqtt.message.subscribe.MqttSubscribeWrapper;
import org.mqttbee.rx.SingleFlow;

/**
 * @author Silvio Giebl
 */
public class MqttSubscribeWithFlow {

    private final MqttSubscribe subscribe;
    private final SingleFlow<Mqtt5SubAck> subAckFlow;
    private final MqttSubscriptionFlow subscriptionFlow;

    public MqttSubscribeWithFlow(
        @NotNull final MqttSubscribe subscribe, @NotNull final SingleFlow<Mqtt5SubAck> subAckFlow) {

        this.subscribe = subscribe;
        this.subAckFlow = subAckFlow;
        this.subscriptionFlow = null;
    }

    public MqttSubscribeWithFlow(
        @NotNull final MqttSubscribe subscribe, @NotNull final MqttSubscriptionFlow subscriptionFlow) {

        this.subscribe = subscribe;
        this.subAckFlow = subscriptionFlow;
        this.subscriptionFlow = subscriptionFlow;
    }

    @NotNull
    public MqttSubscribeWrapperWithFlow wrap(final int packetIdentifier, final int subscriptionIdentifier) {
        return new MqttSubscribeWrapperWithFlow(
            subscribe.wrap(packetIdentifier, subscriptionIdentifier), subAckFlow, subscriptionFlow);
    }


    public static class MqttSubscribeWrapperWithFlow {

        private final MqttSubscribeWrapper subscribe;
        private final SingleFlow<Mqtt5SubAck> subAckFlow;
        private final MqttSubscriptionFlow subscriptionFlow;

        private MqttSubscribeWrapperWithFlow(
            @NotNull final MqttSubscribeWrapper subscribe, @NotNull final SingleFlow<Mqtt5SubAck> subAckFlow,
            @Nullable final MqttSubscriptionFlow subscriptionFlow) {

            this.subscribe = subscribe;
            this.subAckFlow = subAckFlow;
            this.subscriptionFlow = subscriptionFlow;
        }

        @NotNull
        public MqttSubscribeWrapper getSubscribe() {
            return subscribe;
        }

        @NotNull
        public SingleFlow<Mqtt5SubAck> getSubAckFlow() {
            return subAckFlow;
        }

        @Nullable
        public MqttSubscriptionFlow getSubscriptionFlow() {
            return subscriptionFlow;
        }

    }

}
