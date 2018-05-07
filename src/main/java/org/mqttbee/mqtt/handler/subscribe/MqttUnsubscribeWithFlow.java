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

import io.reactivex.SingleEmitter;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt5.message.unsubscribe.unsuback.Mqtt5UnsubAck;
import org.mqttbee.mqtt.message.unsubscribe.MqttUnsubscribe;
import org.mqttbee.mqtt.message.unsubscribe.MqttUnsubscribeWrapper;

/**
 * @author Silvio Giebl
 */
public class MqttUnsubscribeWithFlow {

    private final MqttUnsubscribe unsubscribe;
    private final SingleEmitter<Mqtt5UnsubAck> unsubAckFlow;

    public MqttUnsubscribeWithFlow(
        @NotNull final MqttUnsubscribe unsubscribe, @NotNull final SingleEmitter<Mqtt5UnsubAck> unsubAckFlow) {

        this.unsubscribe = unsubscribe;
        this.unsubAckFlow = unsubAckFlow;
    }

    @NotNull
    public MqttUnsubscribeWrapperWithFlow wrap(final int packetIdentifier) {
        return new MqttUnsubscribeWrapperWithFlow(unsubscribe.wrap(packetIdentifier), unsubAckFlow);
    }


    public static class MqttUnsubscribeWrapperWithFlow {

        private final MqttUnsubscribeWrapper unsubscribe;
        private final SingleEmitter<Mqtt5UnsubAck> unsubAckFlow;

        private MqttUnsubscribeWrapperWithFlow(
            @NotNull final MqttUnsubscribeWrapper unsubscribe,
            @NotNull final SingleEmitter<Mqtt5UnsubAck> unsubAckFlow) {

            this.unsubscribe = unsubscribe;
            this.unsubAckFlow = unsubAckFlow;
        }

        @NotNull
        public MqttUnsubscribeWrapper getUnsubscribe() {
            return unsubscribe;
        }

        @NotNull
        public SingleEmitter<Mqtt5UnsubAck> getUnsubAckFlow() {
            return unsubAckFlow;
        }

    }

}
