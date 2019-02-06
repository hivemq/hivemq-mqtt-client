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

package org.mqttbee.internal.mqtt.message.subscribe.suback;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.annotations.Immutable;
import org.mqttbee.internal.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.internal.mqtt.datatypes.MqttUtf8StringImpl;
import org.mqttbee.internal.mqtt.message.MqttMessageWithUserProperties;
import org.mqttbee.internal.util.collections.ImmutableList;
import org.mqttbee.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAck;
import org.mqttbee.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAckReasonCode;

/**
 * @author Silvio Giebl
 */
@Immutable
public class MqttSubAck extends MqttMessageWithUserProperties.WithReason.WithCodesAndId<Mqtt5SubAckReasonCode>
        implements Mqtt5SubAck {

    public MqttSubAck(
            final int packetIdentifier, final @NotNull ImmutableList<Mqtt5SubAckReasonCode> reasonCodes,
            final @Nullable MqttUtf8StringImpl reasonString, final @NotNull MqttUserPropertiesImpl userProperties) {

        super(packetIdentifier, reasonCodes, reasonString, userProperties);
    }

    @Override
    protected @NotNull String toAttributeString() {
        return "reasonCodes=" + getReasonCodes() + super.toAttributeString();
    }

    @Override
    public @NotNull String toString() {
        return "MqttSubAck{" + toAttributeString() + "}";
    }
}
