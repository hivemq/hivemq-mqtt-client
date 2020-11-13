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

package com.hivemq.client.internal.mqtt.handler.subscribe;

import com.hivemq.client.internal.mqtt.message.unsubscribe.MqttUnsubAck;
import com.hivemq.client.internal.mqtt.message.unsubscribe.MqttUnsubscribe;
import org.jetbrains.annotations.NotNull;

/**
 * @author Silvio Giebl
 */
class MqttUnsubscribeWithFlow extends MqttSubOrUnsubWithFlow {

    final @NotNull MqttUnsubscribe unsubscribe;
    private final @NotNull MqttSubOrUnsubAckFlow<MqttUnsubAck> unsubAckFlow;

    MqttUnsubscribeWithFlow(
            final @NotNull MqttUnsubscribe unsubscribe,
            final @NotNull MqttSubOrUnsubAckFlow<MqttUnsubAck> unsubAckFlow) {

        this.unsubscribe = unsubscribe;
        this.unsubAckFlow = unsubAckFlow;
    }

    @Override
    @NotNull MqttSubOrUnsubAckFlow<MqttUnsubAck> getFlow() {
        return unsubAckFlow;
    }
}
