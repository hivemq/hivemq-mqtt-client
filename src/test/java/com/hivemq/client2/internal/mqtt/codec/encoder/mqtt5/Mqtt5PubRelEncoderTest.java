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

package com.hivemq.client2.internal.mqtt.codec.encoder.mqtt5;

import com.hivemq.client2.internal.mqtt.codec.encoder.MqttMessageEncoders;
import com.hivemq.client2.internal.mqtt.datatypes.MqttUserPropertiesImpl;
import com.hivemq.client2.internal.mqtt.datatypes.MqttUtf8StringImpl;
import com.hivemq.client2.internal.mqtt.datatypes.MqttVariableByteInteger;
import com.hivemq.client2.internal.mqtt.message.publish.MqttPubRel;
import com.hivemq.client2.mqtt.mqtt5.message.Mqtt5MessageType;
import com.hivemq.client2.mqtt.mqtt5.message.publish.Mqtt5PubRelReasonCode;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static com.hivemq.client2.internal.mqtt.datatypes.MqttVariableByteInteger.MAXIMUM_PACKET_SIZE_LIMIT;
import static com.hivemq.client2.mqtt.mqtt5.message.publish.Mqtt5PubRelReasonCode.SUCCESS;
import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;

/**
 * @author David Katz
 */
class Mqtt5PubRelEncoderTest extends AbstractMqtt5EncoderWithUserPropertiesTest {

    Mqtt5PubRelEncoderTest() {
        super(new MqttMessageEncoders() {{
            encoders[Mqtt5MessageType.PUBREL.getCode()] = new Mqtt5PubRelEncoder();
        }}, true);
    }

    @Test
    void encode_simple() {
        final byte[] expected = {
                // fixed header
                //   type, flags
                0b0110_0010,
                //   remaining length
                3,
                // variable header
                //   packet identifier
                0, 5,
                // reason code
                (byte) 0x92
        };

        final Mqtt5PubRelReasonCode reasonCode = Mqtt5PubRelReasonCode.PACKET_IDENTIFIER_NOT_FOUND;
        final MqttUtf8StringImpl reasonString = null;
        final MqttUserPropertiesImpl userProperties = MqttUserPropertiesImpl.NO_USER_PROPERTIES;
        final MqttPubRel pubRel = new MqttPubRel(5, reasonCode, reasonString, userProperties);

        encode(expected, pubRel);
    }

    @Test
    void encode_reasonCodeOmittedWhenSuccessWithoutProperties() {
        final byte[] expected = {
                // fixed header
                //   type, flags
                0b0110_0010,
                //   remaining length
                2,
                // variable header
                //   packet identifier
                0, 5
        };

        final MqttPubRel pubRel = new MqttPubRel(5, SUCCESS, null, MqttUserPropertiesImpl.NO_USER_PROPERTIES);

        encode(expected, pubRel);
    }

    @ParameterizedTest
    @EnumSource(value = Mqtt5PubRelReasonCode.class, mode = EXCLUDE, names = {"SUCCESS"})
    void encode_reasonCodes(final @NotNull Mqtt5PubRelReasonCode reasonCode) {
        final byte[] expected = {
                // fixed header
                //   type, flags
                0b0110_0010,
                //   remaining length
                3,
                // variable header
                //   packet identifier
                6, 5,
                //   reason code placeholder
                (byte) 0xFF
        };

        expected[4] = (byte) reasonCode.getCode();
        final MqttPubRel pubRel = new MqttPubRel(0x0605, reasonCode, null, MqttUserPropertiesImpl.NO_USER_PROPERTIES);

        encode(expected, pubRel);
    }

    @Test
    void encode_reasonString() {
        final byte[] expected = {
                // fixed header
                //   type, flags
                0b0110_0010,
                //   remaining length
                13,
                // variable header
                //   packet identifier
                0, 9,
                //   reason code
                (byte) 0x92,
                //   properties
                9,
                // reason string
                0x1F, 0, 6, 'r', 'e', 'a', 's', 'o', 'n'
        };

        final Mqtt5PubRelReasonCode reasonCode = Mqtt5PubRelReasonCode.PACKET_IDENTIFIER_NOT_FOUND;
        final MqttUtf8StringImpl reasonString = MqttUtf8StringImpl.of("reason");
        final MqttUserPropertiesImpl userProperties = MqttUserPropertiesImpl.NO_USER_PROPERTIES;
        final MqttPubRel pubRel = new MqttPubRel(9, reasonCode, reasonString, userProperties);

        encode(expected, pubRel);
    }

    @Test
    void encode_userProperty() {
        final byte[] expected = {
                // fixed header
                //   type, flags
                0b0110_0010,
                //   remaining length
                21,
                // variable header
                //   packet identifier
                0, 5,
                //   reason code
                (byte) 0x92,
                //   properties
                17,
                // user Property
                0x26, 0, 4, 'u', 's', 'e', 'r', 0, 8, 'p', 'r', 'o', 'p', 'e', 'r', 't', 'y'
        };

        final Mqtt5PubRelReasonCode reasonCode = Mqtt5PubRelReasonCode.PACKET_IDENTIFIER_NOT_FOUND;
        final MqttUserPropertiesImpl userProperties = getUserProperties(1);
        final MqttPubRel pubRel = new MqttPubRel(5, reasonCode, null, userProperties);

        encode(expected, pubRel);
    }

    @Test
    void encode_maximumPacketSizeExceeded_omitUserProperties() {
        final byte[] expected = {
                // fixed header
                //   type, flags
                0b0110_0010,
                //   remaining length
                3,
                // variable header
                //   packet identifier
                0, 5,
                // reason code
                (byte) 0x92
        };

        connected(expected.length + 2);
        final Mqtt5PubRelReasonCode reasonCode = Mqtt5PubRelReasonCode.PACKET_IDENTIFIER_NOT_FOUND;
        final MqttUtf8StringImpl reasonString = null;
        final MqttUserPropertiesImpl userProperties = getUserProperties(1);
        final MqttPubRel pubRel = new MqttPubRel(5, reasonCode, reasonString, userProperties);

        encode(expected, pubRel);
    }

    @Test
    void encode_maximumPacketSizeExceeded_omitReasonString() {
        final byte[] expected = {
                // fixed header
                //   type, flags
                0b0110_0010,
                //   remaining length
                21,
                // variable header
                //   packet identifier
                0, 5,
                //   reason code
                (byte) 0x92,
                //   properties
                17,
                // user Property
                0x26, 0, 4, 'u', 's', 'e', 'r', 0, 8, 'p', 'r', 'o', 'p', 'e', 'r', 't', 'y'
        };
        connected(expected.length + 2);
        final Mqtt5PubRelReasonCode reasonCode = Mqtt5PubRelReasonCode.PACKET_IDENTIFIER_NOT_FOUND;
        final MqttUserPropertiesImpl userProperties = getUserProperties(1);
        final MqttPubRel pubRel = new MqttPubRel(5, reasonCode, null, userProperties);

        encode(expected, pubRel);
    }

    @Test
    void encode_propertyLengthExceeded_omitUserProperties() {
        final byte[] expected = {
                // fixed header
                //   type, flags
                0b0110_0010,
                //   remaining length
                3,
                // variable header
                //   packet identifier
                0, 5,
                // reason code
                (byte) 0x92
        };

        connected(expected.length + 2);
        final Mqtt5PubRelReasonCode reasonCode = Mqtt5PubRelReasonCode.PACKET_IDENTIFIER_NOT_FOUND;
        final MqttUtf8StringImpl reasonString = null;
        final MqttUserPropertiesImpl userProperties =
                getUserProperties((VARIABLE_BYTE_INTEGER_FOUR_BYTES_MAX_VALUE / userPropertyBytes) + 1);

        final MqttPubRel pubRel = new MqttPubRel(5, reasonCode, reasonString, userProperties);

        encode(expected, pubRel);
    }

    @Test
    void encode_propertyLengthExceeded_omitReasonString() {
        final MaximumPacketBuilder maxPacket = new MaximumPacketBuilder();
        final MqttUserPropertiesImpl maxUserProperties = maxPacket.getMaxPossibleUserProperties();

        final ByteBuf expected = Unpooled.buffer(
                MAXIMUM_PACKET_SIZE_LIMIT - maxPacket.getRemainingPropertyBytes(),
                MAXIMUM_PACKET_SIZE_LIMIT - maxPacket.getRemainingPropertyBytes());

        // fixed header
        // type, reserved
        expected.writeByte(0b0110_0010);
        // remaining length (2 + 1 + 4 + (userPropertyBytes * maxPossibleUserPropertiesCount) = 268435447
        expected.writeByte(0xf7);
        expected.writeByte(0xff);
        expected.writeByte(0xff);
        expected.writeByte(0x7f);
        // packet identifier
        expected.writeByte(0);
        expected.writeByte(5);
        // reason code
        expected.writeByte(0x92);
        // properties length
        expected.writeByte(0xf0);
        expected.writeByte(0xff);
        expected.writeByte(0xff);
        expected.writeByte(0x7f);
        // user properties
        maxUserProperties.encode(expected);

        final Mqtt5PubRelReasonCode reasonCode = Mqtt5PubRelReasonCode.PACKET_IDENTIFIER_NOT_FOUND;
        final MqttUtf8StringImpl reasonString = maxPacket.getPaddedUtf8StringTooLong();
        final MqttPubRel pubRel = new MqttPubRel(5, reasonCode, reasonString, maxUserProperties);
        encode(expected.array(), pubRel);
        expected.release();
    }

    private void encode(final @NotNull byte[] expected, final @NotNull MqttPubRel pubRel) {
        encode(pubRel, expected);
    }

    @Override
    int getMaxPropertyLength() {
        return MqttVariableByteInteger.MAXIMUM_PACKET_SIZE_LIMIT - 1  // type, reserved
                - 4  // remaining length
                - 4  // property length
                - 2  // packet identifier
                - 1; // reason code;
    }
}
