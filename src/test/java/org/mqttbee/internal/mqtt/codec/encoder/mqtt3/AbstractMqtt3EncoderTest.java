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

package org.mqttbee.internal.mqtt.codec.encoder.mqtt3;

import com.google.common.primitives.Bytes;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttWireMessage;
import org.jetbrains.annotations.NotNull;
import org.mqttbee.internal.mqtt.MqttClientConfig;
import org.mqttbee.internal.mqtt.MqttClientExecutorConfigImpl;
import org.mqttbee.internal.mqtt.codec.encoder.AbstractMqttEncoderTest;
import org.mqttbee.internal.mqtt.codec.encoder.MqttMessageEncoders;
import org.mqttbee.internal.mqtt.datatypes.MqttClientIdentifierImpl;
import org.mqttbee.mqtt.MqttVersion;

/**
 * @author Alex Stockinger
 */
abstract class AbstractMqtt3EncoderTest extends AbstractMqttEncoderTest {

    AbstractMqtt3EncoderTest(final @NotNull MqttMessageEncoders messageEncoders, final boolean connected) {
        super(messageEncoders, connected, createClientData());
    }

    private static MqttClientConfig createClientData() {
        return new MqttClientConfig(MqttVersion.MQTT_3_1_1, MqttClientIdentifierImpl.of("test"), "localhost", 1883,
                MqttClientExecutorConfigImpl.DEFAULT, null, null, false, null);
    }

    static @NotNull byte[] bytesOf(final @NotNull MqttWireMessage message) throws MqttException {
        return Bytes.concat(message.getHeader(), message.getPayload());
    }
}
