/*
 * Copyright 2018 dc-square and the HiveMQ MQTT Client Project
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

package com.hivemq.client.internal.mqtt.handler.subscribe;

import com.hivemq.client.internal.mqtt.message.MqttStatefulMessage;
import com.hivemq.client.internal.util.collections.NodeList;
import org.jetbrains.annotations.NotNull;

/**
 * @author Silvio Giebl
 */
abstract class MqttSubOrUnsubWithFlow {

    abstract @NotNull MqttSubscriptionFlow<?> getFlow();

    static abstract class Stateful extends NodeList.Node<Stateful> {

        abstract @NotNull MqttStatefulMessage.WithId<?> getMessage();

        abstract @NotNull MqttSubscriptionFlow<?> getFlow();
    }
}
