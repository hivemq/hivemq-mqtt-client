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

package com.hivemq.mqtt.client2.internal.codec.decoder.mqtt5;

import com.hivemq.mqtt.client2.MqttVersion;
import com.hivemq.mqtt.client2.internal.codec.decoder.AbstractMqttDecoderTest;
import com.hivemq.mqtt.client2.internal.codec.decoder.MqttMessageDecoders;
import com.hivemq.mqtt.client2.internal.message.connect.MqttConnectRestrictions;
import com.hivemq.mqtt.client2.internal.message.connect.MqttConnectRestrictionsBuilder;
import org.jetbrains.annotations.NotNull;

/**
 * @author Silvio Giebl
 */
abstract class AbstractMqtt5DecoderTest extends AbstractMqttDecoderTest {

    private static @NotNull MqttConnectRestrictions createConnectRestrictions(final int maximumPacketSize) {
        return new MqttConnectRestrictionsBuilder.Default().maximumPacketSize(maximumPacketSize)
                .topicAliasMaximum(3)
                .requestResponseInformation(true)
                .build();
    }

    AbstractMqtt5DecoderTest(final @NotNull MqttMessageDecoders decoders) {
        super(decoders, MqttVersion.MQTT_5_0, createConnectRestrictions(MqttConnectRestrictions.DEFAULT_MAXIMUM_PACKET_SIZE));
    }

    void setMaximumPacketSize(final int maximumPacketSize) {
        connectRestrictions = createConnectRestrictions(maximumPacketSize);
        createChannel();
    }
}
