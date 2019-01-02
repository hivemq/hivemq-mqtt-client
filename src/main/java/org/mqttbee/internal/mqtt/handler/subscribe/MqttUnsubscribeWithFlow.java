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
import org.mqttbee.internal.mqtt.message.unsubscribe.MqttStatefulUnsubscribe;
import org.mqttbee.internal.mqtt.message.unsubscribe.MqttUnsubscribe;
import org.mqttbee.internal.mqtt.message.unsubscribe.unsuback.MqttUnsubAck;
import org.mqttbee.internal.rx.SingleFlow;

/**
 * @author Silvio Giebl
 */
class MqttUnsubscribeWithFlow extends MqttSubOrUnsubWithFlow {

    private final @NotNull MqttUnsubscribe unsubscribe;
    private final @NotNull SingleFlow<MqttUnsubAck> unsubAckFlow;

    MqttUnsubscribeWithFlow(
            final @NotNull MqttUnsubscribe unsubscribe, final @NotNull SingleFlow<MqttUnsubAck> unsubAckFlow) {

        this.unsubscribe = unsubscribe;
        this.unsubAckFlow = unsubAckFlow;
    }

    @Override
    @NotNull SingleFlow<MqttUnsubAck> getAckFlow() {
        return unsubAckFlow;
    }

    @NotNull Stateful createStateful(final int packetIdentifier) {
        return new Stateful(unsubscribe.createStateful(packetIdentifier), unsubAckFlow);
    }

    static class Stateful extends MqttSubOrUnsubWithFlow.Stateful {

        private final @NotNull MqttStatefulUnsubscribe unsubscribe;
        private final @NotNull SingleFlow<MqttUnsubAck> unsubAckFlow;

        Stateful(
                final @NotNull MqttStatefulUnsubscribe unsubscribe,
                final @NotNull SingleFlow<MqttUnsubAck> unsubAckFlow) {

            this.unsubscribe = unsubscribe;
            this.unsubAckFlow = unsubAckFlow;
        }

        @NotNull MqttStatefulUnsubscribe getUnsubscribe() {
            return unsubscribe;
        }

        @Override
        @NotNull SingleFlow<MqttUnsubAck> getAckFlow() {
            return unsubAckFlow;
        }
    }
}
