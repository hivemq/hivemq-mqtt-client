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

package org.mqttbee.internal.mqtt.codec.decoder.mqtt3;

import org.jetbrains.annotations.NotNull;
import org.mqttbee.internal.mqtt.MqttClientConfig;
import org.mqttbee.internal.mqtt.MqttClientExecutorConfigImpl;
import org.mqttbee.internal.mqtt.codec.decoder.AbstractMqttDecoderTest;
import org.mqttbee.internal.mqtt.codec.decoder.MqttMessageDecoders;
import org.mqttbee.internal.mqtt.datatypes.MqttClientIdentifierImpl;
import org.mqttbee.internal.mqtt.message.connect.MqttConnect;
import org.mqttbee.internal.mqtt.message.connect.mqtt3.Mqtt3ConnectView;
import org.mqttbee.mqtt.MqttVersion;

/**
 * @author Silvio Giebl
 */
abstract class AbstractMqtt3DecoderTest extends AbstractMqttDecoderTest {

    private static @NotNull MqttClientConfig createClientData() {
        return new MqttClientConfig(MqttVersion.MQTT_3_1_1, MqttClientIdentifierImpl.of("test"), "localhost", 1883,
                null, null, false, MqttClientExecutorConfigImpl.DEFAULT, null);
    }

    private static @NotNull MqttConnect createConnect() {
        return Mqtt3ConnectView.DEFAULT.getDelegate();
    }

    AbstractMqtt3DecoderTest(final @NotNull MqttMessageDecoders decoders) {
        super(decoders, createClientData(), createConnect());
    }
}
