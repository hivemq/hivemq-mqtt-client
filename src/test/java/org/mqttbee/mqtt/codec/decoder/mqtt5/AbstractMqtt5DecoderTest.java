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

package org.mqttbee.mqtt.codec.decoder.mqtt5;

import org.jetbrains.annotations.NotNull;
import org.mqttbee.mqtt.MqttClientConfig;
import org.mqttbee.mqtt.MqttClientExecutorConfigImpl;
import org.mqttbee.mqtt.MqttVersion;
import org.mqttbee.mqtt.codec.decoder.AbstractMqttDecoderTest;
import org.mqttbee.mqtt.codec.decoder.MqttMessageDecoders;
import org.mqttbee.mqtt.datatypes.MqttClientIdentifierImpl;
import org.mqttbee.mqtt.message.connect.MqttConnect;
import org.mqttbee.mqtt.message.connect.MqttConnectBuilder;
import org.mqttbee.mqtt.message.connect.MqttConnectRestrictions;

/**
 * @author Silvio Giebl
 */
abstract class AbstractMqtt5DecoderTest extends AbstractMqttDecoderTest {

    private static @NotNull MqttClientConfig createClientData() {
        return new MqttClientConfig(MqttVersion.MQTT_5_0, MqttClientIdentifierImpl.of("test"), "localhost", 1883, null,
                null, false, false, MqttClientExecutorConfigImpl.DEFAULT, null);
    }

    private static @NotNull MqttConnect createConnect(final int maximumPacketSize) {
        return new MqttConnectBuilder.Default().restrictions()
                .maximumPacketSize(maximumPacketSize)
                .topicAliasMaximum(3)
                .applyRestrictions()
                .responseInformationRequested(true)
                .build();
    }

    AbstractMqtt5DecoderTest(final @NotNull MqttMessageDecoders decoders) {
        super(decoders, createClientData(),
                createConnect(MqttConnectRestrictions.DEFAULT_MAXIMUM_PACKET_SIZE_NO_LIMIT));
    }

    void setMaximumPacketSize(final int maximumPacketSize) {
        connect = createConnect(maximumPacketSize);
        createChannel();
    }
}
