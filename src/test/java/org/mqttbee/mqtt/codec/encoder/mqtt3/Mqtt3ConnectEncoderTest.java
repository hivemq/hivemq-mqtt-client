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

import com.google.common.primitives.Bytes;
import io.netty.buffer.ByteBuf;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.junit.jupiter.api.Test;
import org.mqttbee.api.mqtt.datatypes.MqttQoS;
import org.mqttbee.mqtt.codec.encoder.AbstractMqtt5EncoderTest;
import org.mqttbee.mqtt.datatypes.MqttClientIdentifierImpl;
import org.mqttbee.mqtt.datatypes.MqttTopicImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.message.connect.MqttConnect;
import org.mqttbee.mqtt.message.connect.MqttStatefulConnect;
import org.mqttbee.mqtt.message.connect.mqtt3.Mqtt3ConnectView;
import org.mqttbee.mqtt.message.publish.MqttWillPublish;

import java.nio.ByteBuffer;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class Mqtt3ConnectEncoderTest extends AbstractMqtt5EncoderTest {

    private static final byte[] EXAMPLE_CONNECT = {
            // FIXED HEADER
            // packet type and flags
            0x10,
            // remaining length (16 Bytes)
            0x10,
            // VARIABLE HEADER
            // protocol name
            0x00, //msb
            0x04, //lsb
            'M', 'Q', 'T', 'T',
            //protocol level
            0x04,
            //connect flags (only clean session is set)
            0b0000_0010,
            //keep alive (60)
            0x00, 0x3c,
            //clientId
            0x00, 0x04, 'T', 'E', 'S', 'T'
    };

    Mqtt3ConnectEncoderTest() {
        super(code -> new Mqtt3ConnectEncoder(), false);
    }

    @Test
    void encode_SUCCESS() {
        final MqttClientIdentifierImpl identifier = Objects.requireNonNull(MqttClientIdentifierImpl.from("TEST"));
        final MqttConnect connect = Mqtt3ConnectView.wrapped(60, true, null, null);
        final MqttStatefulConnect connectWrapper = connect.createStateful(identifier, null);
        encode(EXAMPLE_CONNECT, connectWrapper);
    }

    @Test
    void test_SUCCESS_WITH_WILL_WITH_PAHO() throws MqttException {
        final String clientId = "Test123";
        final boolean cleanSession = true;
        final int keepAlive = 120;
        final String username = null;
        final String password = null;
        final String willTopic = "my/last/will";
        final String myLastWill = "mylastwillpayload";
        final boolean isRetained = false;
        final int qosWill = 1;

        //PAHO
        final org.eclipse.paho.client.mqttv3.MqttMessage will =
                new org.eclipse.paho.client.mqttv3.MqttMessage(myLastWill.getBytes());
        will.setQos(qosWill);
        final org.eclipse.paho.client.mqttv3.internal.wire.MqttConnect pahoConnect =
                new org.eclipse.paho.client.mqttv3.internal.wire.MqttConnect(clientId, 4, cleanSession, keepAlive,
                        username, password == null ? "".toCharArray() : password.toCharArray(), will, willTopic);

        final byte[] expected = Bytes.concat(pahoConnect.getHeader(), pahoConnect.getPayload());

        final MqttWillPublish willMessage = new MqttWillPublish(Objects.requireNonNull(MqttTopicImpl.from(willTopic)),
                ByteBuffer.wrap(myLastWill.getBytes()), Objects.requireNonNull(MqttQoS.fromCode(qosWill)), isRetained,
                MqttWillPublish.MESSAGE_EXPIRY_INTERVAL_INFINITY, null, null, null, null,
                MqttUserPropertiesImpl.NO_USER_PROPERTIES, 0);

        final MqttConnect connect = Mqtt3ConnectView.wrapped(keepAlive, cleanSession, null, willMessage);
        final MqttStatefulConnect connectWrapper =
                connect.createStateful(Objects.requireNonNull(MqttClientIdentifierImpl.from(clientId)), null);

        encode(expected, connectWrapper);
    }

    /**
     * Pseudo test to assure we get the right bytes from paho methods
     */
    @Test
    void test_PAHO_GETPAYLOAD_METHOD() throws MqttException {
        final org.eclipse.paho.client.mqttv3.internal.wire.MqttConnect pahoConnect =
                new org.eclipse.paho.client.mqttv3.internal.wire.MqttConnect("TEST", 4, true, 60, null, null, null,
                        null);
        final byte[] actual = Bytes.concat(pahoConnect.getHeader(), pahoConnect.getPayload());
        assertArrayEquals(EXAMPLE_CONNECT, actual);
    }

    private void encode(final byte[] expected, final MqttStatefulConnect connect) {
        channel.writeOutbound(connect);
        final ByteBuf byteBuf = channel.readOutbound();
        final byte[] actual = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(actual);
        byteBuf.release();
        assertArrayEquals(expected, actual);
    }

}