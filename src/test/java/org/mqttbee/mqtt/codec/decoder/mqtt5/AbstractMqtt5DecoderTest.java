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
import org.mqttbee.api.mqtt.mqtt5.message.connect.Mqtt5ConnectRestrictions;
import org.mqttbee.mqtt.MqttClientConfig;
import org.mqttbee.mqtt.MqttClientConnectionConfig;
import org.mqttbee.mqtt.MqttClientExecutorConfigImpl;
import org.mqttbee.mqtt.MqttVersion;
import org.mqttbee.mqtt.codec.decoder.AbstractMqttDecoderTest;
import org.mqttbee.mqtt.codec.decoder.MqttMessageDecoders;
import org.mqttbee.mqtt.datatypes.MqttClientIdentifierImpl;

import java.util.Objects;

/**
 * @author Silvio Giebl
 */
abstract class AbstractMqtt5DecoderTest extends AbstractMqttDecoderTest {

    private static @NotNull MqttClientConfig createClientData() {
        return new MqttClientConfig(MqttVersion.MQTT_5_0, Objects.requireNonNull(MqttClientIdentifierImpl.from("test")),
                "localhost", 1883, null, null, false, false, MqttClientExecutorConfigImpl.DEFAULT, null);
    }

    private int maximumPacketSize = Mqtt5ConnectRestrictions.DEFAULT_MAXIMUM_PACKET_SIZE_NO_LIMIT;

    AbstractMqtt5DecoderTest(final @NotNull MqttMessageDecoders decoders) {
        super(createClientData(), decoders);
    }

    @Override
    protected void initChannel() {
        clientData.setClientConnectionConfig(
                new MqttClientConnectionConfig(10, 10, Mqtt5ConnectRestrictions.DEFAULT_RECEIVE_MAXIMUM,
                        maximumPacketSize, 3, null, false, true, true, channel));
        super.initChannel();
    }

    void setMaximumPacketSize(final int maximumPacketSize) {
        this.maximumPacketSize = maximumPacketSize;
        createChannel();
    }

}
