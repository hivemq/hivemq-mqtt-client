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

package com.hivemq.client2.internal.mqtt.handler.subscribe;

import com.hivemq.client2.internal.mqtt.message.subscribe.MqttSubAck;
import com.hivemq.client2.internal.mqtt.message.subscribe.MqttSubscribe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Silvio Giebl
 */
class MqttSubscribeWithFlow extends MqttSubOrUnsubWithFlow {

    final @NotNull MqttSubscribe subscribe;
    final int subscriptionIdentifier;
    private final @Nullable MqttSubscriptionFlow<MqttSubAck> flow;

    MqttSubscribeWithFlow(
            final @NotNull MqttSubscribe subscribe,
            final int subscriptionIdentifier,
            final @Nullable MqttSubscriptionFlow<MqttSubAck> flow) {

        this.subscribe = subscribe;
        this.subscriptionIdentifier = subscriptionIdentifier;
        this.flow = flow;
    }

    @Override
    @Nullable MqttSubscriptionFlow<MqttSubAck> getFlow() {
        return flow;
    }
}
