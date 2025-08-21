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

package com.hivemq.mqtt.client2.internal.codec.encoder.mqtt5;

import com.hivemq.mqtt.client2.internal.codec.encoder.MqttPingReqEncoder;
import com.hivemq.mqtt.client2.internal.message.ping.MqttPingReq;
import com.hivemq.mqtt.client2.mqtt5.message.Mqtt5MessageType;
import io.netty.buffer.ByteBuf;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

/**
 * @author David Katz
 */
class Mqtt5PingReqEncoderTest extends AbstractMqtt5EncoderTest {

    Mqtt5PingReqEncoderTest() {
        super(Mqtt5MessageType.PINGREQ, MqttPingReqEncoder.INSTANCE, true);
    }

    @Test
    void encode_pingreq() {
        final byte[] expected = {
                // fixed header
                //   type, reserved
                (byte) 0b1100_0000,
                //   remaining length
                0
        };

        final MqttPingReq pingReq = MqttPingReq.INSTANCE;
        channel.writeOutbound(pingReq);
        final ByteBuf byteBuf = channel.readOutbound();

        final byte[] actual = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(actual);
        byteBuf.release();

        assertArrayEquals(expected, actual);
    }

}
