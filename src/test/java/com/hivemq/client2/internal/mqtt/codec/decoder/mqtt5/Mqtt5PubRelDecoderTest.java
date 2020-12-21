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

package com.hivemq.client2.internal.mqtt.codec.decoder.mqtt5;

import com.hivemq.client2.internal.mqtt.codec.decoder.MqttMessageDecoders;
import com.hivemq.client2.internal.mqtt.datatypes.MqttUserPropertyImpl;
import com.hivemq.client2.internal.mqtt.message.publish.MqttPubRel;
import com.hivemq.client2.internal.util.collections.ImmutableList;
import com.hivemq.client2.mqtt.mqtt5.message.Mqtt5MessageType;
import com.hivemq.client2.mqtt.mqtt5.message.disconnect.Mqtt5Disconnect;
import com.hivemq.client2.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import com.hivemq.client2.mqtt.mqtt5.message.publish.Mqtt5PubRelReasonCode;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static com.hivemq.client2.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode.MALFORMED_PACKET;
import static com.hivemq.client2.mqtt.mqtt5.message.publish.Mqtt5PubRelReasonCode.SUCCESS;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Silvio Giebl
 * @author David Katz
 */
class Mqtt5PubRelDecoderTest extends AbstractMqtt5DecoderTest {

    Mqtt5PubRelDecoderTest() {
        super(new MqttMessageDecoders() {{
            decoders[Mqtt5MessageType.PUBREL.getCode()] = new Mqtt5PubRelDecoder();
        }});
    }

    @Test
    void decode_allParameters() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0110_0010,
                //   remaining length
                43,
                // variable header
                //   packet identifier
                0, 5,
                //   reason code (success)
                0x00,
                //   properties
                39,
                //     reason string
                0x1F, 0, 7, 's', 'u', 'c', 'c', 'e', 's', 's',
                //     user properties
                0x26, 0, 4, 't', 'e', 's', 't', 0, 5, 'v', 'a', 'l', 'u', 'e', //
                0x26, 0, 4, 't', 'e', 's', 't', 0, 6, 'v', 'a', 'l', 'u', 'e', '2',
        };

        final MqttPubRel pubRel = decodeOk(encoded);

        assertEquals(SUCCESS, pubRel.getReasonCode());
        assertTrue(pubRel.getReasonString().isPresent());
        assertEquals("success", pubRel.getReasonString().get().toString());

        final ImmutableList<MqttUserPropertyImpl> userProperties = pubRel.getUserProperties().asList();
        assertEquals(2, userProperties.size());
        assertEquals("test", userProperties.get(0).getName().toString());
        assertEquals("value", userProperties.get(0).getValue().toString());
        assertEquals("test", userProperties.get(1).getName().toString());
        assertEquals("value2", userProperties.get(1).getValue().toString());
    }

    @Test
    void decode_reasonCodeAndPropertiesOmittedOnSuccess() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0110_0010,
                //   remaining length
                2,
                // variable header
                //   packet identifier
                0, 5
        };

        final MqttPubRel pubRel = decodeOk(encoded);
        assertEquals(SUCCESS, pubRel.getReasonCode());
    }

    @ParameterizedTest
    @EnumSource(value = Mqtt5PubRelReasonCode.class, mode = EnumSource.Mode.EXCLUDE, names = {"SUCCESS"})
    void decode_allReasonCodes(final @NotNull Mqtt5PubRelReasonCode reasonCode) {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0110_0010,
                //   remaining length
                4,
                // variable header
                //   packet identifier
                0, 5,
                //   reason code placeholder
                0x00,
                //   properties length
                0

        };
        encoded[4] = (byte) reasonCode.getCode();
        final MqttPubRel pubRel = decodeOk(encoded);
        assertEquals(reasonCode, pubRel.getReasonCode());
    }

    @Test
    void decode_multipleUserProperties() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0110_0010,
                //   remaining length
                33,
                // variable header
                //   packet identifier
                0, 5,
                // reason code
                0x00,
                //   properties
                29,
                // user properties
                0x26, 0, 4, 't', 'e', 's', 't', 0, 5, 'v', 'a', 'l', 'u', 'e', //
                0x26, 0, 4, 't', 'e', 's', 't', 0, 6, 'v', 'a', 'l', 'u', 'e', '2'
        };

        final MqttPubRel pubRel = decodeOk(encoded);
        final ImmutableList<MqttUserPropertyImpl> userProperties = pubRel.getUserProperties().asList();
        assertEquals(2, userProperties.size());
        assertEquals("test", userProperties.get(0).getName().toString());
        assertEquals("value", userProperties.get(0).getValue().toString());
        assertEquals("test", userProperties.get(1).getName().toString());
        assertEquals("value2", userProperties.get(1).getValue().toString());
    }

    @Test
    void decode_invalidFlags_returnsNull() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0110_1010,
                //   remaining length
                2,
                // variable header
                //   packet identifier
                0, 5
        };
        decodeNok(encoded, MALFORMED_PACKET);
    }

    @Test
    void decode_packetLengthLargerThanMaxPacketSize_returnsNull() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0110_0010,
                //   remaining length
                32,
                // variable header
                //   packet identifier
                0, 5,
                // reason code
                0x00,
                //   properties
                28,
                // user properties
                0x26, 0, 4, 't', 'e', 's', 't', 0, 5, 'v', 'a', 'l', 'u', 'e', //
                0x26, 0, 4, 't', 'e', 's', 't', 0, 5, 'v', 'a', 'l', 'u', 'e'
        };
        setMaximumPacketSize(encoded.length - 1);
        decodeNok(encoded, Mqtt5DisconnectReasonCode.PACKET_TOO_LARGE);
    }

    @Test
    void decode_packetTooSmall_returnsNull() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0110_0010,
                //   remaining length
                0
        };
        decodeNok(encoded, MALFORMED_PACKET);
    }

    @Test
    void decode_propertyLengthLessThanZero_returnsNull() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0110_0010,
                //   remaining length
                4,
                // variable header
                //   packet identifier
                0, 5,
                // reason code
                0x00,
                //   properties
                -1
        };
        decodeNok(encoded, MALFORMED_PACKET);
    }

    @Test
    void decode_propertyLengthTooLarge_returnsNull() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0110_0010,
                //   remaining length
                4,
                // variable header
                //   packet identifier
                0, 5,
                // reason code
                0x00,
                //   properties
                10
        };
        decodeNok(encoded, MALFORMED_PACKET);
    }

    @Test
    void decode_negativePropertyIdentifier_returnsNull() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0110_0010,
                //   remaining length
                11,
                // variable header
                //   packet identifier
                0, 5,
                // reason code
                0x00,
                //   properties
                7,
                // negative property id
                (byte) -3, 0, 4, 't', 'e', 's', 't'
        };
        decodeNok(encoded, MALFORMED_PACKET);
    }

    @Test
    void decode_invalidPropertyIdentifier_returnsNull() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0110_0010,
                //   remaining length
                11,
                // variable header
                //   packet identifier
                0, 5,
                // reason code
                0x00,
                //   properties
                7,
                // invalid property id
                (byte) 0x03, 0, 4, 't', 'e', 's', 't'
        };
        decodeNok(encoded, MALFORMED_PACKET);
    }

    @Test
    void decode_invalidReasonCode_returnsNull() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0110_0010,
                //   remaining length
                3,
                // variable header
                //   packet identifier
                0, 5,
                //   reason code (success)
                (byte) 0xFF
        };
        decodeNok(encoded, MALFORMED_PACKET);
    }

    @Test
    void decode_nullUserProperty_returnsNull() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0110_0010,
                //   remaining length
                11,
                // variable header
                //   packet identifier
                0, 5,
                // reason code
                0x00,
                //   properties
                7,
                // user properties
                0x26, 0, 1, '\u0000', 0, 1, 'x'
        };
        decodeNok(encoded, Mqtt5DisconnectReasonCode.MALFORMED_PACKET);
    }

    @Test
    void decode_nullReasonString_returnsNull() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0110_0010,
                //   remaining length
                8,
                // variable header
                //   packet identifier
                0, 5,
                //   reason code (success)
                0x00,
                //   properties
                4,
                //     reason string
                0x1F, 0, 1, '\u0000'
        };
        decodeNok(encoded, MALFORMED_PACKET);
    }

    private @NotNull MqttPubRel decodeOk(final @NotNull byte[] encoded) {
        final MqttPubRel pubRel = decode(encoded);
        assertNotNull(pubRel);
        return pubRel;
    }

    private void decodeNok(final @NotNull byte[] encoded, final @NotNull Mqtt5DisconnectReasonCode reasonCode) {
        final MqttPubRel pubRel = decode(encoded);
        assertNull(pubRel);

        final Mqtt5Disconnect disconnect = channel.readOutbound();
        assertNotNull(disconnect);
        assertEquals(reasonCode, disconnect.getReasonCode());

        createChannel();
    }

    private @Nullable MqttPubRel decode(final @NotNull byte[] encoded) {
        final ByteBuf byteBuf = channel.alloc().buffer();
        byteBuf.writeBytes(encoded);
        channel.writeInbound(byteBuf);

        return channel.readInbound();
    }
}