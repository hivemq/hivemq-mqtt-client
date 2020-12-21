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
import com.hivemq.client2.internal.mqtt.message.unsubscribe.MqttUnsubAck;
import com.hivemq.client2.mqtt.datatypes.MqttUtf8String;
import com.hivemq.client2.mqtt.mqtt5.datatypes.Mqtt5UserProperty;
import com.hivemq.client2.mqtt.mqtt5.message.Mqtt5MessageType;
import com.hivemq.client2.mqtt.mqtt5.message.disconnect.Mqtt5Disconnect;
import com.hivemq.client2.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import com.hivemq.client2.mqtt.mqtt5.message.unsubscribe.Mqtt5UnsubAck;
import com.hivemq.client2.mqtt.mqtt5.message.unsubscribe.Mqtt5UnsubAckReasonCode;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;
import java.util.Optional;

import static com.hivemq.client2.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode.MALFORMED_PACKET;
import static com.hivemq.client2.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode.PROTOCOL_ERROR;
import static com.hivemq.client2.mqtt.mqtt5.message.unsubscribe.Mqtt5UnsubAckReasonCode.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author David Katz
 */
class Mqtt5UnsubAckDecoderTest extends AbstractMqtt5DecoderTest {

    Mqtt5UnsubAckDecoderTest() {
        super(new MqttMessageDecoders() {{
            decoders[Mqtt5MessageType.UNSUBACK.getCode()] = new Mqtt5UnsubAckDecoder();
        }});
    }

    @Test
    void decode_simple() {
        final byte[] encoded = {
                // fixed header
                //   type _ flags (reserved)
                (byte) 0b1011_0000,
                //   remaining length
                27,
                // variable header
                //   packet identifier MSB, LSB
                1, 2,
                //   properties length in bytes
                23,
                //     reason string
                0x1F, 0, 6, 'r', 'e', 'a', 's', 'o', 'n',
                //     user property
                0x26, 0, 4, 'n', 'a', 'm', 'e', 0, 5, 'v', 'a', 'l', 'u', 'e',
                // payload reason code: success
                0x00
        };

        final MqttUnsubAck unsubAck = decodeOk(encoded);

        //0x0102 = 258
        assertEquals(258, unsubAck.getPacketIdentifier());
        final Optional<MqttUtf8String> reasonString = unsubAck.getReasonString();
        assertTrue(reasonString.isPresent());
        assertEquals("reason", reasonString.get().toString());

        final List<MqttUserPropertyImpl> userProperties = unsubAck.getUserProperties().asList();
        assertEquals(1, userProperties.size());
        assertEquals("name", userProperties.get(0).getName().toString());
        assertEquals("value", userProperties.get(0).getValue().toString());

        assertEquals(1, unsubAck.getReasonCodes().size());
        assertEquals(SUCCESS, unsubAck.getReasonCodes().get(0));
    }

    @Test
    void decode_propertyLengthNegative_returnsNull() {
        final byte[] encoded = {
                // fixed header
                //   type _ flags (reserved)
                (byte) 0b1011_0000,
                //   remaining length
                3,
                // variable header
                //   packet identifier MSB, LSB
                1, 2,
                //   properties length in bytes
                -1
        };
        decodeNok(encoded, MALFORMED_PACKET);
    }

    @Test
    void decode_negativePropertyIdentifier_returnsNull() {
        final byte[] encoded = {
                // fixed header
                //   type _ flags (reserved)
                (byte) 0b1011_0000,
                //   remaining length
                13,
                // variable header
                //   packet identifier MSB, LSB
                1, 2,
                //   properties length in bytes
                9,
                //     reason string
                (byte) 0xFF, 0, 6, 'r', 'e', 'a', 's', 'o', 'n',
                // payload reason code: success
                0x00
        };
        decodeNok(encoded, MALFORMED_PACKET);
    }

    @Test
    void decode_invalidHeaderFlags_returnsNull() {
        final byte[] encoded = {
                // fixed header
                //   type _ flags (reserved)
                (byte) 0b1011_1000,
                //   remaining length
                27,
                // variable header
                //   packet identifier MSB, LSB
                1, 2,
                //   properties length in bytes
                23,
                //     reason string
                0x1F, 0, 6, 'r', 'e', 'a', 's', 'o', 'n',
                //     user property
                0x26, 0, 4, 'n', 'a', 'm', 'e', 0, 5, 'v', 'a', 'l', 'u', 'e',
                // payload reason code: success
                0x00
        };
        decodeNok(encoded, MALFORMED_PACKET);
    }

    @Test
    void decode_tooShort_returnsNull() {
        final byte[] encoded = {
                // fixed header
                //   type _ flags (reserved)
                (byte) 0b1011_0000,
                //   remaining length
                0
        };

        decodeNok(encoded, MALFORMED_PACKET);
    }

    @Test
    void decode_reasonStringMissing() {
        final byte[] encoded = {
                // fixed header
                //   type _ flags (reserved)
                (byte) 0b1011_0000,
                //   remaining length
                18,
                // variable header
                //   packet identifier MSB, LSB
                0xF, 0x5,
                //   properties length in bytes
                14,
                //     user property
                0x26, 0, 4, 'n', 'a', 'm', 'e', 0, 5, 'v', 'a', 'l', 'u', 'e',
                // payload reason code: success
                0x00
        };

        final MqttUnsubAck unsubAck = decodeOk(encoded);

        // 0x0F05 = 3845
        assertEquals(3845, unsubAck.getPacketIdentifier());
        assertFalse(unsubAck.getReasonString().isPresent());

        final List<MqttUserPropertyImpl> userProperties = unsubAck.getUserProperties().asList();
        assertEquals(1, userProperties.size());
        assertEquals("name", userProperties.get(0).getName().toString());
        assertEquals("value", userProperties.get(0).getValue().toString());

        assertEquals(1, unsubAck.getReasonCodes().size());
        assertEquals(SUCCESS, unsubAck.getReasonCodes().get(0));
    }

    @Test
    void decode_userProperty() {
        final byte[] encoded = {
                // fixed header
                //   type _ flags (reserved)
                (byte) 0b1011_0000,
                //   remaining length
                18,
                // variable header
                //   packet identifier MSB, LSB
                0, 2,
                //   properties length in bytes
                14,
                //     user properties
                0x26, 0, 4, 'n', 'a', 'm', 'e', 0, 5, 'v', 'a', 'l', 'u', 'e',
                // payload reason code: success
                0x00
        };

        final Mqtt5UnsubAck unsuback = decodeOk(encoded);
        assertNotNull(unsuback);
        final List<? extends Mqtt5UserProperty> userProperties = unsuback.getUserProperties().asList();
        assertEquals(1, userProperties.size());
        assertEquals("name", userProperties.get(0).getName().toString());
        assertEquals("value", userProperties.get(0).getValue().toString());
    }

    @Test
    void decode_userPropertyInvalid_returnsNull() {
        final byte[] encoded = {
                // fixed header
                //   type _ flags (reserved)
                (byte) 0b1011_0000,
                //   remaining length
                8,
                // variable header
                //   packet identifier MSB, LSB
                0, 2,
                //   properties length in bytes
                4,
                //     user properties
                0x26, 0, 1, 'n',
                // payload reason code: success
                0x00
        };
        decodeNok(encoded, MALFORMED_PACKET);
    }

    @Test
    void decode_userPropertiesMultiple() {
        final byte[] encoded = {
                // fixed header
                //   type _ flags (reserved)
                (byte) 0b1011_0000,
                //   remaining length
                55,
                // variable header
                //   packet identifier MSB, LSB
                0, 2,
                //   properties length in bytes
                51,
                //     reason string
                0x1F, 0, 6, 'r', 'e', 'a', 's', 'o', 'n',
                //     user properties
                0x26, 0, 4, 'n', 'a', 'm', 'e', 0, 5, 'v', 'a', 'l', 'u', 'e', //
                0x26, 0, 4, 'n', 'a', 'm', 'e', 0, 5, 'v', 'a', 'l', 'u', 'e', //
                0x26, 0, 4, 'n', 'a', 'm', 'e', 0, 5, 'v', 'a', 'l', 'u', 'e',
                // payload reason code: success
                0x00
        };

        final MqttUnsubAck unsubAck = decodeOk(encoded);

        assertEquals(2, unsubAck.getPacketIdentifier());
        final Optional<MqttUtf8String> reasonString = unsubAck.getReasonString();
        assertTrue(reasonString.isPresent());
        assertEquals("reason", reasonString.get().toString());

        final List<MqttUserPropertyImpl> userProperties = unsubAck.getUserProperties().asList();
        assertEquals(3, userProperties.size());
        assertEquals("name", userProperties.get(0).getName().toString());
        assertEquals("value", userProperties.get(0).getValue().toString());
        assertEquals("name", userProperties.get(1).getName().toString());
        assertEquals("value", userProperties.get(1).getValue().toString());
        assertEquals("name", userProperties.get(2).getName().toString());
        assertEquals("value", userProperties.get(2).getValue().toString());

        assertEquals(1, unsubAck.getReasonCodes().size());
        assertEquals(SUCCESS, unsubAck.getReasonCodes().get(0));
    }

    @ParameterizedTest
    @EnumSource(Mqtt5UnsubAckReasonCode.class)
    void decode_reasonCodes(final @NotNull Mqtt5UnsubAckReasonCode reasonCode) {
        final byte[] encoded = {
                // fixed header
                //   type _ flags (reserved)
                (byte) 0b1011_0000,
                //   remaining length
                4,
                // variable header
                //   packet identifier MSB, LSB
                8, 8,
                //   properties length in bytes
                0,
                // placeholder payload reason codes
                (byte) 0xFF
        };

        encoded[5] = (byte) reasonCode.getCode();
        final Mqtt5UnsubAck unsuback = decodeOk(encoded);
        assertNotNull(unsuback);
        assertEquals(1, unsuback.getReasonCodes().size());
        assertEquals(reasonCode, unsuback.getReasonCodes().get(0));
    }

    @Test
    void decode_missingReasonCode_returnsNull() {
        final byte[] encoded = {
                // fixed header
                //   type _ flags (reserved)
                (byte) 0b1011_0000,
                //   remaining length
                3,
                // variable header
                //   packet identifier MSB, LSB
                8, 8,
                //   properties length in bytes
                0
        };

        decodeNok(encoded, PROTOCOL_ERROR);
    }

    @Test
    void decode_reasonCodesMultiple() {
        final byte[] encoded = {
                // fixed header
                //   type _ flags (reserved)
                (byte) 0b1011_0000,
                //   remaining length
                33,
                // variable header
                //   packet identifier MSB, LSB
                8, 8,
                //   properties length in bytes
                23,
                //     reason string
                0x1F, 0, 6, 'r', 'e', 'a', 's', 'o', 'n',
                //     user property
                0x26, 0, 4, 'n', 'a', 'm', 'e', 0, 5, 'v', 'a', 'l', 'u', 'e',
                // payload reason codes, one of each possible, in order of spec
                0x00, 0x11, (byte) 0x80, (byte) 0x83, (byte) 0x87, (byte) 0x8F, (byte) 0x91
        };

        final MqttUnsubAck unsubAck = decodeOk(encoded);

        // 0x0808 = 2056
        assertEquals(2056, unsubAck.getPacketIdentifier());
        final Optional<MqttUtf8String> reasonString = unsubAck.getReasonString();
        assertTrue(reasonString.isPresent());
        assertEquals("reason", reasonString.get().toString());

        final List<MqttUserPropertyImpl> userProperties = unsubAck.getUserProperties().asList();
        assertEquals(1, userProperties.size());
        assertEquals("name", userProperties.get(0).getName().toString());
        assertEquals("value", userProperties.get(0).getValue().toString());

        assertEquals(7, unsubAck.getReasonCodes().size());
        assertEquals(SUCCESS, unsubAck.getReasonCodes().get(0));
        assertEquals(NO_SUBSCRIPTIONS_EXISTED, unsubAck.getReasonCodes().get(1));
        assertEquals(UNSPECIFIED_ERROR, unsubAck.getReasonCodes().get(2));
        assertEquals(IMPLEMENTATION_SPECIFIC_ERROR, unsubAck.getReasonCodes().get(3));
        assertEquals(NOT_AUTHORIZED, unsubAck.getReasonCodes().get(4));
        assertEquals(TOPIC_FILTER_INVALID, unsubAck.getReasonCodes().get(5));
        assertEquals(PACKET_IDENTIFIER_IN_USE, unsubAck.getReasonCodes().get(6));
    }

    @Test
    void decode_reasonCodesInvalid_returnsNull() {
        final byte[] encoded = {
                // fixed header
                //   type _ flags (reserved)
                (byte) 0b1011_0000,
                //   remaining length
                27,
                // variable header
                //   packet identifier MSB, LSB
                0, 1,
                //   properties length in bytes
                23,
                //     reason string
                0x1F, 0, 6, 'r', 'e', 'a', 's', 'o', 'n',
                //     user property
                0x26, 0, 4, 'n', 'a', 'm', 'e', 0, 5, 'v', 'a', 'l', 'u', 'e',
                // payload reason code
                (byte) 0x00
        };

        decodeOk(encoded);
        assertEquals(0x00, encoded[28]);

        encoded[28] = (byte) 0xFF; // invalid reason code
        decodeNok(encoded, MALFORMED_PACKET);

        encoded[28] = 0x00; // set reason code ok again
        decodeOk(encoded);
    }

    @Test
    void decode_propertiesLengthTooLong_returnsNull() {
        final byte[] encoded = {
                // fixed header
                //   type _ flags (reserved)
                (byte) 0b1011_0000,
                //   remaining length
                27,
                // variable header
                //   packet identifier MSB, LSB
                0, 1,
                //   properties length in bytes
                23,
                //     reason string
                0x1F, 0, 6, 'r', 'e', 'a', 's', 'o', 'n',
                //     user property
                0x26, 0, 4, 'n', 'a', 'm', 'e', 0, 5, 'v', 'a', 'l', 'u', 'e',
                // payload reason code: SUCCESS
                (byte) 0x00
        };

        decodeOk(encoded);
        assertEquals(23, encoded[4]);

        encoded[4] = 125; // property length
        decodeNok(encoded, MALFORMED_PACKET);

        encoded[4] = 23; // set length ok again
        decodeOk(encoded);
    }

    @Test
    void decode_propertiesLengthTooShort_returnsNull() {
        final byte[] encoded = {
                // fixed header
                //   type _ flags (reserved)
                (byte) 0b1011_0000,
                //   remaining length
                27,
                // variable header
                //   packet identifier MSB, LSB
                0, 1,
                //   properties length in bytes
                23,
                //     reason string
                0x1F, 0, 6, 'r', 'e', 'a', 's', 'o', 'n',
                //     user property
                0x26, 0, 4, 'n', 'a', 'm', 'e', 0, 5, 'v', 'a', 'l', 'u', 'e',
                // payload reason code: SUCCESS
                (byte) 0x00
        };

        decodeOk(encoded);
        assertEquals(23, encoded[4]);

        encoded[4] = 10; // set length too short
        decodeNok(encoded, MALFORMED_PACKET);

        encoded[4] = 23; // set length ok again
        decodeOk(encoded);
    }

    @Test
    void decode_reasonString() {
        final byte[] encoded = {
                // fixed header
                //   type _ flags (reserved)
                (byte) 0b1011_0000,
                //   remaining length
                13,
                // variable header
                //   packet identifier MSB, LSB
                0, 1,
                //   properties length in bytes
                9,
                //     reason string
                0x1F, 0, 6, 'r', 'e', 'a', 's', 'o', 'n',
                // payload reason code: SUCCESS
                (byte) 0x00
        };

        final Mqtt5UnsubAck unsuback = decodeOk(encoded);
        final Optional<MqttUtf8String> reasonString = unsuback.getReasonString();
        assertTrue(reasonString.isPresent());
        assertEquals("reason", reasonString.get().toString());
    }

    @Test
    void decode_reasonString_invalid_returnsNull() {
        final byte[] encoded = {
                // fixed header
                //   type _ flags (reserved)
                (byte) 0b1011_0000,
                //   remaining length
                8,
                // variable header
                //   packet identifier MSB, LSB
                0, 1,
                //   properties length in bytes
                3,
                //     reason string with invalid character
                0x1F, 0, 1, '\u0000',
                // payload reason code: SUCCESS
                (byte) 0x00
        };
        decodeNok(encoded, MALFORMED_PACKET);
    }

    @Test
    void decode_reasonStringLengthTooLong_returnsNull() {
        final byte[] encoded = {
                // fixed header
                //   type _ flags (reserved)
                (byte) 0b1011_0000,
                //   remaining length
                27,
                // variable header
                //   packet identifier MSB, LSB
                0, 1,
                //   properties length in bytes
                23,
                //     reason string
                0x1F, 0, 6, 'r', 'e', 'a', 's', 'o', 'n',
                //     user property
                0x26, 0, 4, 'n', 'a', 'm', 'e', 0, 5, 'v', 'a', 'l', 'u', 'e',
                // payload reason code: SUCCESS
                (byte) 0x00
        };

        decodeOk(encoded);
        assertEquals(6, encoded[7]);

        encoded[7] = 7; // set length too long
        decodeNok(encoded, MALFORMED_PACKET);

        encoded[7] = 6; // set length ok again
        decodeOk(encoded);
    }

    @NotNull
    private MqttUnsubAck decodeOk(final @NotNull byte[] encoded) {
        final MqttUnsubAck unsubAck = decode(encoded);
        assertNotNull(unsubAck);
        return unsubAck;
    }

    private void decodeNok(final @NotNull byte[] encoded, final @NotNull Mqtt5DisconnectReasonCode reasonCode) {
        final MqttUnsubAck unsubAck = decode(encoded);
        assertNull(unsubAck);

        final Mqtt5Disconnect disconnect = channel.readOutbound();
        assertNotNull(disconnect);
        assertEquals(reasonCode, disconnect.getReasonCode());

        createChannel();
    }

    @Nullable
    private MqttUnsubAck decode(final @NotNull byte[] encoded) {
        final ByteBuf byteBuf = channel.alloc().buffer();
        byteBuf.writeBytes(encoded);
        channel.writeInbound(byteBuf);

        return channel.readInbound();
    }

}
