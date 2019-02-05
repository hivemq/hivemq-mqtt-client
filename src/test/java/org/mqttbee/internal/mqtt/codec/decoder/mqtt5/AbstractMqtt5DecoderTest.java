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

package org.mqttbee.internal.mqtt.codec.decoder.mqtt5;

import org.jetbrains.annotations.NotNull;
import org.mqttbee.internal.mqtt.codec.decoder.AbstractMqttDecoderTest;
import org.mqttbee.internal.mqtt.codec.decoder.MqttMessageDecoders;
import org.mqttbee.internal.mqtt.message.connect.MqttConnect;
import org.mqttbee.internal.mqtt.message.connect.MqttConnectBuilder;
import org.mqttbee.internal.mqtt.message.connect.MqttConnectRestrictions;
import org.mqttbee.mqtt.MqttVersion;

/**
 * @author Silvio Giebl
 */
abstract class AbstractMqtt5DecoderTest extends AbstractMqttDecoderTest {

    private static @NotNull MqttConnect createConnect(final int maximumPacketSize) {
        return new MqttConnectBuilder.Default().restrictions()
                .maximumPacketSize(maximumPacketSize)
                .topicAliasMaximum(3)
                .requestResponseInformation(true)
                .applyRestrictions()
                .build();
    }

    AbstractMqtt5DecoderTest(final @NotNull MqttMessageDecoders decoders) {
        super(decoders, MqttVersion.MQTT_5_0, createConnect(MqttConnectRestrictions.DEFAULT_MAXIMUM_PACKET_SIZE));
    }

    void setMaximumPacketSize(final int maximumPacketSize) {
        connect = createConnect(maximumPacketSize);
        createChannel();
    }
}
