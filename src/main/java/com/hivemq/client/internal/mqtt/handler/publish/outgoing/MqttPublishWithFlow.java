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

package com.hivemq.client.internal.mqtt.handler.publish.outgoing;

import com.hivemq.client.internal.mqtt.message.publish.MqttPublish;
import org.jetbrains.annotations.NotNull;

/**
 * @author Silvio Giebl
 */
class MqttPublishWithFlow extends MqttPubOrRelWithFlow {

    private final @NotNull MqttPublish publish;

    MqttPublishWithFlow(final @NotNull MqttPublish publish, final @NotNull MqttAckFlow ackFlow) {
        super(ackFlow);
        this.publish = publish;
    }

    @NotNull MqttPublish getPublish() {
        return publish;
    }
}
