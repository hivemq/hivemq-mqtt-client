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

package org.mqttbee.internal.mqtt.codec.decoder.mqtt5;

import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.mqttbee.internal.mqtt.codec.decoder.MqttMessageDecoders;
import org.mqttbee.mqtt.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.mqtt.mqtt5.message.disconnect.Mqtt5Disconnect;
import org.mqttbee.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import org.mqttbee.mqtt.mqtt5.message.ping.Mqtt5PingResp;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Silvio Giebl
 */
class Mqtt5PingRespDecoderTest extends AbstractMqtt5DecoderTest {

    Mqtt5PingRespDecoderTest() {
        super(new MqttMessageDecoders() {{
            decoders[Mqtt5MessageType.PINGRESP.getCode()] = createPingRespDecoder();
        }});
    }

    @Test
    void decode_valid() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b1101_0000);
        //   remaining length
        byteBuf.writeByte(0);

        channel.writeInbound(byteBuf);
        final Mqtt5PingResp pingResp = channel.readInbound();

        assertNotNull(pingResp);
    }

    @Test
    void decode_not_enough_bytes() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b1101_0000);

        channel.writeInbound(byteBuf);
        final Mqtt5PingResp pingResp = channel.readInbound();

        assertNull(pingResp);

        final Mqtt5Disconnect disconnect = channel.readOutbound();

        assertNull(disconnect);
    }

    @Test
    void decode_wrong_flags() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b1101_0100);
        //   remaining length
        byteBuf.writeByte(0);

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET);
    }

    @Test
    void decode_remaining_length_not_0() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b1101_0000);
        //   remaining length
        byteBuf.writeByte(1);
        byteBuf.writeByte(0);

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET);
    }

    private void testDisconnect(final @NotNull Mqtt5DisconnectReasonCode reasonCode) {
        final Mqtt5PingResp pingResp = channel.readInbound();
        assertNull(pingResp);

        final Mqtt5Disconnect disconnect = channel.readOutbound();
        assertNotNull(disconnect);
        assertEquals(reasonCode, disconnect.getReasonCode());
        assertTrue(disconnect.getReasonString().isPresent());
    }
}
