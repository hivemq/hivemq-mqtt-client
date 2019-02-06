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

package org.mqttbee.internal.mqtt.message.unsubscribe.unsuback.mqtt3;

import org.jetbrains.annotations.NotNull;
import org.mqttbee.annotations.Immutable;
import org.mqttbee.internal.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.internal.mqtt.message.unsubscribe.unsuback.MqttUnsubAck;
import org.mqttbee.internal.util.collections.ImmutableList;
import org.mqttbee.mqtt.mqtt3.message.unsubscribe.unsuback.Mqtt3UnsubAck;
import org.mqttbee.mqtt.mqtt5.message.unsubscribe.unsuback.Mqtt5UnsubAckReasonCode;

/**
 * @author Silvio Giebl
 */
@Immutable
public class Mqtt3UnsubAckView implements Mqtt3UnsubAck {

    public static final @NotNull ImmutableList<Mqtt5UnsubAckReasonCode> REASON_CODES_ALL_SUCCESS = ImmutableList.of();
    public static final @NotNull Mqtt3UnsubAckView INSTANCE = new Mqtt3UnsubAckView();

    public static @NotNull MqttUnsubAck delegate(final int packetIdentifier) {
        return new MqttUnsubAck(
                packetIdentifier, REASON_CODES_ALL_SUCCESS, null, MqttUserPropertiesImpl.NO_USER_PROPERTIES);
    }

    @Override
    public @NotNull String toString() {
        return "MqttUnsubAck{}";
    }
}
