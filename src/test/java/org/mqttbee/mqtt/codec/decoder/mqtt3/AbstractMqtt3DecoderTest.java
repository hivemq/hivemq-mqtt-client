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

package org.mqttbee.mqtt.codec.decoder.mqtt3;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt5.message.connect.Mqtt5Connect;
import org.mqttbee.api.mqtt.mqtt5.message.connect.Mqtt5ConnectRestrictions;
import org.mqttbee.mqtt.MqttClientConnectionData;
import org.mqttbee.mqtt.MqttClientData;
import org.mqttbee.mqtt.MqttClientExecutorConfigImpl;
import org.mqttbee.mqtt.MqttVersion;
import org.mqttbee.mqtt.codec.decoder.AbstractMqttDecoderTest;
import org.mqttbee.mqtt.codec.decoder.MqttMessageDecoders;
import org.mqttbee.mqtt.datatypes.MqttClientIdentifierImpl;
import org.mqttbee.mqtt.datatypes.MqttVariableByteInteger;
import org.mqttbee.mqtt.ioc.ChannelComponent;

import java.util.Objects;

/**
 * @author Silvio Giebl
 */
abstract class AbstractMqtt3DecoderTest extends AbstractMqttDecoderTest {

    private final MqttClientData clientData;

    AbstractMqtt3DecoderTest(@NotNull final MqttMessageDecoders decoders) {
        super(decoders);
        clientData = new MqttClientData(MqttVersion.MQTT_3_1_1,
                Objects.requireNonNull(MqttClientIdentifierImpl.from("test")), "localhost", 1883, "", false, false, false, false,
                MqttClientExecutorConfigImpl.DEFAULT, null);
    }

    @Override
    protected void createChannel() {
        super.createChannel();
        clientData.to(channel);
        ChannelComponent.create(channel, clientData);
        clientData.setClientConnectionData(new MqttClientConnectionData(10, Mqtt5Connect.NO_SESSION_EXPIRY,
                Mqtt5ConnectRestrictions.DEFAULT_RECEIVE_MAXIMUM, 0, MqttVariableByteInteger.MAXIMUM_PACKET_SIZE_LIMIT,
                null, false, false, false, channel));
    }

}
