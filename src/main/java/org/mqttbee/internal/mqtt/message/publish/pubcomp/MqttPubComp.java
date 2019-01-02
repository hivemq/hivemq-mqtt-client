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

package org.mqttbee.internal.mqtt.message.publish.pubcomp;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.annotations.Immutable;
import org.mqttbee.internal.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.internal.mqtt.datatypes.MqttUtf8StringImpl;
import org.mqttbee.internal.mqtt.message.MqttMessageWithUserProperties;
import org.mqttbee.mqtt.mqtt5.message.publish.pubcomp.Mqtt5PubComp;
import org.mqttbee.mqtt.mqtt5.message.publish.pubcomp.Mqtt5PubCompReasonCode;

/**
 * @author Silvio Giebl
 */
@Immutable
public class MqttPubComp extends MqttMessageWithUserProperties.WithReason.WithCode.WithId<Mqtt5PubCompReasonCode>
        implements Mqtt5PubComp {

    public static final @NotNull Mqtt5PubCompReasonCode DEFAULT_REASON_CODE = Mqtt5PubCompReasonCode.SUCCESS;

    public MqttPubComp(
            final int packetIdentifier, final @NotNull Mqtt5PubCompReasonCode reasonCode,
            final @Nullable MqttUtf8StringImpl reasonString, final @NotNull MqttUserPropertiesImpl userProperties) {

        super(packetIdentifier, reasonCode, reasonString, userProperties);
    }
}
