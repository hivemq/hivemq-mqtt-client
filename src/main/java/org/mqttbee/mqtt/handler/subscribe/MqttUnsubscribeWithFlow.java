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
import org.mqttbee.api.mqtt.mqtt5.message.unsubscribe.unsuback.Mqtt5UnsubAck;
import org.mqttbee.mqtt.message.unsubscribe.MqttStatefulUnsubscribe;
import org.mqttbee.mqtt.message.unsubscribe.MqttUnsubscribe;
import org.mqttbee.rx.SingleFlow;

/**
 * @author Silvio Giebl
 */
class MqttUnsubscribeWithFlow {

    private final MqttUnsubscribe unsubscribe;
    private final SingleFlow<Mqtt5UnsubAck> unsubAckFlow;

    MqttUnsubscribeWithFlow(
            @NotNull final MqttUnsubscribe unsubscribe, @NotNull final SingleFlow<Mqtt5UnsubAck> unsubAckFlow) {

        this.unsubscribe = unsubscribe;
        this.unsubAckFlow = unsubAckFlow;
    }

    @NotNull
    MqttStatefulUnsubscribeWithFlow createStateful(final int packetIdentifier) {
        return new MqttStatefulUnsubscribeWithFlow(unsubscribe.createStateful(packetIdentifier), unsubAckFlow);
    }

    static class MqttStatefulUnsubscribeWithFlow {

        private final MqttStatefulUnsubscribe unsubscribe;
        private final SingleFlow<Mqtt5UnsubAck> unsubAckFlow;

        MqttStatefulUnsubscribeWithFlow(
                @NotNull final MqttStatefulUnsubscribe unsubscribe,
                @NotNull final SingleFlow<Mqtt5UnsubAck> unsubAckFlow) {

            this.unsubscribe = unsubscribe;
            this.unsubAckFlow = unsubAckFlow;
        }

        @NotNull
        MqttStatefulUnsubscribe getUnsubscribe() {
            return unsubscribe;
        }

        @NotNull
        SingleFlow<Mqtt5UnsubAck> getUnsubAckFlow() {
            return unsubAckFlow;
        }

    }

}
