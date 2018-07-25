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

package org.mqttbee.mqtt.codec.encoder.mqtt3;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.mqttbee.api.mqtt.datatypes.MqttQos;
import org.mqttbee.mqtt.datatypes.MqttClientIdentifierImpl;
import org.mqttbee.mqtt.datatypes.MqttTopicImpl;
import org.mqttbee.mqtt.datatypes.MqttUTF8StringImpl;
import org.mqttbee.mqtt.message.auth.MqttSimpleAuth;
import org.mqttbee.mqtt.message.connect.MqttConnect;
import org.mqttbee.mqtt.message.connect.MqttStatefulConnect;
import org.mqttbee.mqtt.message.connect.mqtt3.Mqtt3ConnectView;
import org.mqttbee.mqtt.message.publish.MqttWillPublish;
import org.mqttbee.mqtt.message.publish.mqtt3.Mqtt3PublishView;

import java.nio.ByteBuffer;

import static org.eclipse.paho.client.mqttv3.MqttConnectOptions.MQTT_VERSION_3_1_1;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.platform.commons.util.StringUtils.isNotBlank;

class Mqtt3ConnectEncoderTest extends AbstractMqtt3EncoderTest {

    Mqtt3ConnectEncoderTest() {
        super(code -> new Mqtt3ConnectEncoder(), false);
    }

    @CsvFileSource(resources = "/testParams/mqtt3/Connect.csv")
    @ParameterizedTest(name = "Connect(\"{0}\", {1}, {2}, \"{3}\", \"{4}\", \"{5}\", \"{6}\")")
    public void matchesPaho(
            final String clientId,
            final boolean cleanSession,
            final int keepAliveInterval,
            final String userName,
            final String password,
            final String willMessage,
            final String willTopic,
            final Integer willQos,
            final Boolean willRetained
    ) throws MqttException {
        final boolean hasAuth = isNotBlank(userName) && isNotBlank(password);
        final boolean hasWill = isNotBlank(willMessage)
                && isNotBlank(willTopic)
                && willQos != null
                && willRetained != null;

        final MqttSimpleAuth auth = !hasAuth ? null : new MqttSimpleAuth(
                MqttUTF8StringImpl.from(userName),
                ByteBuffer.wrap(password.getBytes(UTF8))
        );
        final MqttClientIdentifierImpl identifier = MqttClientIdentifierImpl.from(clientId);
        final MqttWillPublish beeWill = hasWill ? Mqtt3PublishView.of(
                MqttTopicImpl.from(willTopic),
                ByteBuffer.wrap(willMessage.getBytes()),
                MqttQos.fromCode(willQos),
                willRetained
        ).getWillDelegate() : null;
        final MqttConnect beeConnect = Mqtt3ConnectView.delegate(keepAliveInterval, cleanSession, auth, beeWill);
        final MqttStatefulConnect connectWrapper = beeConnect.createStateful(identifier, null);

        org.eclipse.paho.client.mqttv3.internal.wire.MqttConnect pahoConnect =
                new org.eclipse.paho.client.mqttv3.internal.wire.MqttConnect(
                        clientId,
                        MQTT_VERSION_3_1_1, // MQTT bee only supports 3.1.1, so constant for PAHO
                        cleanSession,
                        keepAliveInterval,
                        !hasAuth ? null : userName,
                        !hasAuth ? null : password.toCharArray(),
                        !hasWill ? null : new MqttMessage(willMessage.getBytes(UTF8)) {
                            {
                                setQos(willQos);
                                setRetained(willRetained);
                            }
                        },
                        !hasWill ? null : willTopic
                );

        assertArrayEquals(bytesOf(pahoConnect), bytesOf(connectWrapper));
    }
}
