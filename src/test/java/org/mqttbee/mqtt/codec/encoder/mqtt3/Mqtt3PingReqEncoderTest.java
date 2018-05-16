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

import io.netty.buffer.ByteBuf;
import org.junit.jupiter.api.Test;
import org.mqttbee.mqtt.codec.encoder.AbstractMqtt5EncoderTest;
import org.mqttbee.mqtt.codec.encoder.MqttPingReqEncoder;
import org.mqttbee.mqtt.message.ping.MqttPingReq;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class Mqtt3PingReqEncoderTest extends AbstractMqtt5EncoderTest {

    Mqtt3PingReqEncoderTest() {
        super(code -> new MqttPingReqEncoder(), true);
    }

    @Test
    void encode() {
        // Given
        final MqttPingReq pingRequest = MqttPingReq.INSTANCE;
        final byte[] expected = {(byte) 0b11000000, 0b0};

        // When
        channel.writeOutbound(pingRequest);

        // Then
        ByteBuf byteBuf = null;
        try {
            byteBuf = channel.readOutbound();
            final byte[] actual = new byte[byteBuf.readableBytes()];
            byteBuf.readBytes(actual);
            assertArrayEquals(expected, actual);
        } finally {
            if (byteBuf != null) {
                byteBuf.release();
            }
        }
    }

}
