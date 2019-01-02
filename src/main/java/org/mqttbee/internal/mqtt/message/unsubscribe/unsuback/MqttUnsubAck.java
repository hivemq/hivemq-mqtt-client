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

package org.mqttbee.internal.mqtt.message.unsubscribe.unsuback;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.annotations.Immutable;
import org.mqttbee.internal.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.internal.mqtt.datatypes.MqttUtf8StringImpl;
import org.mqttbee.internal.mqtt.message.MqttMessageWithUserProperties;
import org.mqttbee.mqtt.mqtt5.message.unsubscribe.unsuback.Mqtt5UnsubAck;
import org.mqttbee.mqtt.mqtt5.message.unsubscribe.unsuback.Mqtt5UnsubAckReasonCode;
import org.mqttbee.util.collections.ImmutableList;

/**
 * @author Silvio Giebl
 */
@Immutable
public class MqttUnsubAck extends MqttMessageWithUserProperties.WithReason.WithCodesAndId<Mqtt5UnsubAckReasonCode>
        implements Mqtt5UnsubAck {

    public MqttUnsubAck(
            final int packetIdentifier, final @NotNull ImmutableList<Mqtt5UnsubAckReasonCode> reasonCodes,
            final @Nullable MqttUtf8StringImpl reasonString, final @NotNull MqttUserPropertiesImpl userProperties) {

        super(packetIdentifier, reasonCodes, reasonString, userProperties);
    }
}
