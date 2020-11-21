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

package com.hivemq.client.internal.mqtt.codec.encoder.mqtt3;

import com.hivemq.client.internal.mqtt.codec.encoder.MqttMessageEncoders;
import com.hivemq.client.internal.mqtt.datatypes.MqttClientIdentifierImpl;
import com.hivemq.client.internal.mqtt.message.connect.MqttConnect;
import com.hivemq.client.internal.mqtt.message.connect.MqttStatefulConnect;
import com.hivemq.client.internal.mqtt.message.connect.mqtt3.Mqtt3ConnectViewBuilder;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt3.message.Mqtt3MessageType;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static org.eclipse.paho.client.mqttv3.MqttConnectOptions.MQTT_VERSION_3_1_1;
import static org.junit.platform.commons.util.StringUtils.isNotBlank;

class Mqtt3ConnectEncoderTest extends AbstractMqtt3EncoderTest {

    Mqtt3ConnectEncoderTest() {
        super(new MqttMessageEncoders() {{
            encoders[Mqtt3MessageType.CONNECT.getCode()] = new Mqtt3ConnectEncoder();
        }}, false);
    }

    @CsvSource({
            "client42, true, 10, , , , , ,", //
            "client42, true, 10, userName, password, , , ,",
            "client42, true, 10, userName, password, willMessage, my/will/topic, 1, true"
    })
    @ParameterizedTest(name = "Connect(\"{0}\", {1}, {2}, \"{3}\", \"{4}\", \"{5}\", \"{6}\")")
    void matchesPaho(
            final @NotNull String clientId,
            final boolean cleanSession,
            final int keepAliveInterval,
            final @Nullable String userName,
            final @Nullable String password,
            final @Nullable String willMessage,
            final @Nullable String willTopic,
            final @Nullable Integer willQos,
            final @Nullable Boolean willRetained) throws MqttException {

        final boolean hasAuth = isNotBlank(userName) && isNotBlank(password);
        final boolean hasWill =
                isNotBlank(willMessage) && isNotBlank(willTopic) && (willQos != null) && (willRetained != null);

        Mqtt3ConnectViewBuilder.Default connectBuilder =
                new Mqtt3ConnectViewBuilder.Default().cleanSession(cleanSession).keepAlive(keepAliveInterval);
        if (hasAuth) {
            connectBuilder = connectBuilder.simpleAuthWith()
                    .username(userName)
                    .password(password.getBytes(StandardCharsets.UTF_8))
                    .applySimpleAuth();
        }
        if (hasWill) {
            connectBuilder = connectBuilder.willPublishWith()
                    .topic(willTopic)
                    .qos(Objects.requireNonNull(MqttQos.fromCode(willQos)))
                    .payload(willMessage.getBytes(StandardCharsets.UTF_8))
                    .retain(willRetained)
                    .applyWillPublish();
        }

        final MqttConnect beeConnect = connectBuilder.build().getDelegate();
        final MqttStatefulConnect statefulConnect =
                beeConnect.createStateful(MqttClientIdentifierImpl.of(clientId), null);

        final org.eclipse.paho.client.mqttv3.internal.wire.MqttConnect pahoConnect =
                new org.eclipse.paho.client.mqttv3.internal.wire.MqttConnect(
                        clientId, //
                        MQTT_VERSION_3_1_1, // MQTT bee only supports 3.1.1, so constant for PAHO
                        cleanSession, //
                        keepAliveInterval, //
                        !hasAuth ? null : userName, //
                        !hasAuth ? null : password.toCharArray(), //
                        !hasWill ? null : new MqttMessage(willMessage.getBytes(StandardCharsets.UTF_8)) {
                            {
                                setQos(willQos);
                                setRetained(willRetained);
                            }
                        }, //
                        !hasWill ? null : willTopic);

        encode(statefulConnect, bytesOf(pahoConnect));
    }
}
