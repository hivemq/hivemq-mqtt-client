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

package org.mqttbee.mqtt.handler.publish.outgoing;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.mqtt.message.publish.MqttPublish;
import org.mqttbee.mqtt.message.publish.pubrel.MqttPubRel;

/**
 * @author Silvio Giebl
 */
class MqttPublishWithFlow {

    private final @NotNull MqttPublish publish;
    private final @NotNull MqttIncomingAckFlow incomingAckFlow;
    private @Nullable MqttPubRel pubRel;

    MqttPublishWithFlow(final @NotNull MqttPublish publish, final @NotNull MqttIncomingAckFlow incomingAckFlow) {
        this.publish = publish;
        this.incomingAckFlow = incomingAckFlow;
    }

    @NotNull MqttPublish getPublish() {
        return publish;
    }

    @NotNull MqttIncomingAckFlow getIncomingAckFlow() {
        return incomingAckFlow;
    }

    void setPubRel(@NotNull final MqttPubRel pubRel) {
        this.pubRel = pubRel;
    }

    @Nullable MqttPubRel getPubRel() {
        return pubRel;
    }

}
