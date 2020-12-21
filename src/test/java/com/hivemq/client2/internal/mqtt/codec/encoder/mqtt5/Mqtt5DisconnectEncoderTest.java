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
import com.hivemq.client2.internal.mqtt.datatypes.MqttUserPropertyImpl;
import com.hivemq.client2.internal.mqtt.datatypes.MqttUtf8StringImpl;
import com.hivemq.client2.internal.mqtt.datatypes.MqttVariableByteInteger;
import com.hivemq.client2.internal.mqtt.message.disconnect.MqttDisconnect;
import com.hivemq.client2.internal.util.collections.ImmutableList;
import com.hivemq.client2.mqtt.mqtt5.message.Mqtt5MessageType;
import com.hivemq.client2.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Arrays;

import static com.hivemq.client2.internal.mqtt.message.disconnect.MqttDisconnect.SESSION_EXPIRY_INTERVAL_FROM_CONNECT;
import static com.hivemq.client2.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode.*;
import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;

/**
 * @author David Katz
 */
class Mqtt5DisconnectEncoderTest extends AbstractMqtt5EncoderWithUserPropertiesTest {

    Mqtt5DisconnectEncoderTest() {
        super(new MqttMessageEncoders() {{
            encoders[Mqtt5MessageType.DISCONNECT.getCode()] = new Mqtt5DisconnectEncoder();
        }}, true);
    }

    @Test
    void encode_allProperties() {
        final byte[] expected = {
                // fixed header
                //   type, flags
                (byte) 0b1110_0000,
                // remaining length
                54,
                // variable header
                //   reason code Malformed Packet
                (byte) 0x81,
                //  Properties
                52,
                //    Session Expiry Interval
                0x11, 0, 0, 0, 1,
                //    Server Reference
                0x1C, 0, 6, 's', 'e', 'r', 'v', 'e', 'r',
                //    Reason String
                0x1F, 0, 6, 'r', 'e', 'a', 's', 'o', 'n',
                // User Properties
                0x26, 0, 4, 't', 'e', 's', 't', 0, 5, 'v', 'a', 'l', 'u', 'e', //
                0x26, 0, 4, 't', 'e', 's', 't', 0, 6, 'v', 'a', 'l', 'u', 'e', '2'

        };
        final MqttUtf8StringImpl reasonString = MqttUtf8StringImpl.of("reason");
        final MqttUtf8StringImpl serverReference = MqttUtf8StringImpl.of("server");
        final long sessionExpiryInterval = 1;
        final MqttUtf8StringImpl test = MqttUtf8StringImpl.of("test");
        final MqttUtf8StringImpl value = MqttUtf8StringImpl.of("value");
        final MqttUtf8StringImpl value2 = MqttUtf8StringImpl.of("value2");
        final MqttUserPropertyImpl userProperty1 = new MqttUserPropertyImpl(test, value);
        final MqttUserPropertyImpl userProperty2 = new MqttUserPropertyImpl(test, value2);
        final MqttUserPropertiesImpl userProperties =
                MqttUserPropertiesImpl.of(ImmutableList.of(userProperty1, userProperty2));

        final MqttDisconnect disconnect =
                new MqttDisconnect(MALFORMED_PACKET, sessionExpiryInterval, serverReference, reasonString,
                        userProperties);

        encode(expected, disconnect);
    }

    @Test
    void encode_reasonCode() {
        final byte[] expected = {
                // fixed header
                //   type, flags
                (byte) 0b1110_0000,
                // remaining length
                1,
                // variable header
                //   reason code Malformed Packet
                (byte) 0x81
        };

        final MqttDisconnect disconnect =
                new MqttDisconnect(MALFORMED_PACKET, SESSION_EXPIRY_INTERVAL_FROM_CONNECT, null, null,
                        MqttUserPropertiesImpl.NO_USER_PROPERTIES);
        encode(expected, disconnect);
    }

    @ParameterizedTest
    @EnumSource(value = Mqtt5DisconnectReasonCode.class, mode = EXCLUDE, names = "NORMAL_DISCONNECTION")
    void encode_allReasonCodes(final @NotNull Mqtt5DisconnectReasonCode reasonCode) {
        final byte[] expected = {
                // fixed header
                //   type, flags
                (byte) 0b1110_0000,
                // remaining length
                1,
                // variable header
                //   reason code placeholder
                (byte) 0xFF
        };

        expected[2] = (byte) reasonCode.getCode();
        final MqttDisconnect disconnect =
                new MqttDisconnect(reasonCode, SESSION_EXPIRY_INTERVAL_FROM_CONNECT, null, null,
                        MqttUserPropertiesImpl.NO_USER_PROPERTIES);
        encode(expected, disconnect);
    }

    @Test
    void encode_noReasonCodeIfNormalWithoutProperties() {
        final byte[] expected = {
                // fixed header
                //   type, flags
                (byte) 0b1110_0000,
                // remaining length
                0
        };

        final MqttDisconnect disconnect =
                new MqttDisconnect(NORMAL_DISCONNECTION, SESSION_EXPIRY_INTERVAL_FROM_CONNECT, null, null,
                        MqttUserPropertiesImpl.NO_USER_PROPERTIES);
        encode(expected, disconnect);
    }

    @Test
    void encode_reasonString() {
        final byte[] expected = {
                // fixed header
                //   type, flags
                (byte) 0b1110_0000,
                // remaining length
                11,
                // variable header
                //   reason code Malformed Packet
                (byte) 0x81,
                //  Properties
                9,
                //    Reason String
                0x1F, 0, 6, 'r', 'e', 'a', 's', 'o', 'n'

        };
        final MqttUtf8StringImpl reasonString = MqttUtf8StringImpl.of("reason");
        final MqttDisconnect disconnect =
                new MqttDisconnect(MALFORMED_PACKET, SESSION_EXPIRY_INTERVAL_FROM_CONNECT, null, reasonString,
                        MqttUserPropertiesImpl.NO_USER_PROPERTIES);

        encode(expected, disconnect);
    }

    @Test
    void encode_serverReference() {
        final byte[] expected = {
                // fixed header
                //   type, flags
                (byte) 0b1110_0000,
                // remaining length
                11,
                // variable header
                //   reason code Malformed Packet
                (byte) 0x81,
                //  Properties
                9,
                //    Server Reference
                0x1C, 0, 6, 's', 'e', 'r', 'v', 'e', 'r',

        };
        final MqttUtf8StringImpl serverReference = MqttUtf8StringImpl.of("server");
        final MqttDisconnect disconnect =
                new MqttDisconnect(MALFORMED_PACKET, SESSION_EXPIRY_INTERVAL_FROM_CONNECT, serverReference, null,
                        MqttUserPropertiesImpl.NO_USER_PROPERTIES);

        encode(expected, disconnect);
    }

    @Test
    void encode_sessionExpiryInterval() {
        final byte[] expected = {
                // fixed header
                //   type, flags
                (byte) 0b1110_0000,
                // remaining length
                7,
                // variable header
                //   reason code Malformed Packet
                (byte) 0x81,
                //  Properties
                5,
                //    Session Expiry Interval 123456789 as hex
                0x11, 0x07, 0x5B, (byte) 0xCD, 0x15

        };

        final long sessionExpiryInterval = 123456789;
        final MqttDisconnect disconnect = new MqttDisconnect(MALFORMED_PACKET, sessionExpiryInterval, null, null,
                MqttUserPropertiesImpl.NO_USER_PROPERTIES);

        encode(expected, disconnect);
    }

    @Test
    void encode_maximumPacketSizeExceededOnSuccess_omitUserProperties() {
        final MaximumPacketBuilder maxPacket = new MaximumPacketBuilder();
        final byte[] expected = {
                // fixed header
                //   type, flags
                (byte) 0b1110_0000,
                // remaining length
                0
        };

        final MqttDisconnect disconnect =
                new MqttDisconnect(NORMAL_DISCONNECTION, SESSION_EXPIRY_INTERVAL_FROM_CONNECT, null, null,
                        getUserProperties(maxPacket.getMaxUserPropertiesCount() + 1));

        encode(expected, disconnect);
    }

    @Test
    void encode_maximumPacketSizeExceeded_omitReasonString() {
        final MaximumPacketBuilder maxPacket = new MaximumPacketBuilder();
        final MqttUserPropertiesImpl maxUserProperties = getUserProperties(maxPacket.getMaxUserPropertiesCount());
        final MqttUtf8StringImpl reasonString = getPaddedUtf8String(maxPacket.getRemainingPropertyBytes() + 1);

        final ByteBuf expected = Unpooled.buffer(5 + 268435445, 5 + 268435445);

        // fixed header
        // type, reserved
        expected.writeByte(0b1110_0000);
        // remaining length (1 + 4 + (userPropertyBytes * maxPossibleUserPropertiesCount = 268435445
        expected.writeByte(0xf5);
        expected.writeByte(0xff);
        expected.writeByte(0xff);
        expected.writeByte(0x7f);
        // reason code
        expected.writeByte((byte) 0x82);
        // properties length
        expected.writeByte(0xf0);
        expected.writeByte(0xff);
        expected.writeByte(0xff);
        expected.writeByte(0x7f);
        // user properties
        maxUserProperties.encode(expected);

        final MqttDisconnect disconnect =
                new MqttDisconnect(PROTOCOL_ERROR, SESSION_EXPIRY_INTERVAL_FROM_CONNECT, null, reasonString,
                        maxUserProperties);

        encode(expected.array(), disconnect);
        expected.release();
    }

    @Test
    void encode_maximumPacketSizeExceeded_omitUserProperties() {
        final MaximumPacketBuilder maxPacket = new MaximumPacketBuilder();
        final byte[] expected = {
                // fixed header
                //   type, flags
                (byte) 0b1110_0000,
                // remaining length
                1, (byte) 0x82
        };

        final MqttDisconnect disconnect =
                new MqttDisconnect(PROTOCOL_ERROR, SESSION_EXPIRY_INTERVAL_FROM_CONNECT, null, null,
                        getUserProperties(maxPacket.getMaxUserPropertiesCount() + 1));

        encode(expected, disconnect);
    }

    @Test
    void encode_propertyLengthExceededOnSuccess_omitUserProperties() {
        final byte[] expected = {
                // fixed header
                //   type, flags
                (byte) 0b1110_0000,
                // remaining length
                0
        };

        final MqttDisconnect disconnect =
                new MqttDisconnect(NORMAL_DISCONNECTION, SESSION_EXPIRY_INTERVAL_FROM_CONNECT, null, null,
                        getUserProperties((VARIABLE_BYTE_INTEGER_FOUR_BYTES_MAX_VALUE / userPropertyBytes) + 1));

        encode(expected, disconnect);
    }

    @Test
    void encode_propertyLengthExceeded_omitUserProperties() {
        final byte[] expected = {
                // fixed header
                //   type, flags
                (byte) 0b1110_0000,
                // remaining length
                1, (byte) 0x81
        };

        final MqttDisconnect disconnect =
                new MqttDisconnect(MALFORMED_PACKET, SESSION_EXPIRY_INTERVAL_FROM_CONNECT, null, null,
                        getUserProperties((VARIABLE_BYTE_INTEGER_FOUR_BYTES_MAX_VALUE / userPropertyBytes) + 1));

        encode(expected, disconnect);
    }

    @Test
    void encode_propertyLengthExceeded_omitReasonString() {
        final int maxUserPropertiesCount = VARIABLE_BYTE_INTEGER_FOUR_BYTES_MAX_VALUE / userPropertyBytes;
        final MqttUserPropertiesImpl maxUserProperties = getUserProperties(maxUserPropertiesCount);
        final int maxReasonStringLength = VARIABLE_BYTE_INTEGER_FOUR_BYTES_MAX_VALUE % userPropertyBytes;
        final char[] reasonStringBytes = new char[maxReasonStringLength];
        Arrays.fill(reasonStringBytes, 'r');
        final MqttUtf8StringImpl reasonString = MqttUtf8StringImpl.of(new String(reasonStringBytes));

        final ByteBuf expected = Unpooled.buffer(5 + 268435445, 5 + 268435445);

        // fixed header
        // type, reserved
        expected.writeByte(0b1110_0000);
        // remaining length (1 + 4 + (userPropertyBytes * maxPossibleUserPropertiesCount = 268435445
        expected.writeByte(0xf5);
        expected.writeByte(0xff);
        expected.writeByte(0xff);
        expected.writeByte(0x7f);
        // reason code
        expected.writeByte((byte) 0x81);
        // properties length
        expected.writeByte(0xf0);
        expected.writeByte(0xff);
        expected.writeByte(0xff);
        expected.writeByte(0x7f);
        // user properties
        maxUserProperties.encode(expected);

        final MqttDisconnect disconnect =
                new MqttDisconnect(MALFORMED_PACKET, SESSION_EXPIRY_INTERVAL_FROM_CONNECT, null, reasonString,
                        maxUserProperties);

        encode(expected.array(), disconnect);
        expected.release();
    }

    private void encode(final @NotNull byte[] expected, final @NotNull MqttDisconnect disconnect) {
        encode(disconnect, expected);
    }

    @Override
    int getMaxPropertyLength() {
        return MqttVariableByteInteger.MAXIMUM_PACKET_SIZE_LIMIT - 1  // type, reserved
                - 4  // remaining length
                - 1  // reason code
                - 4  // properties length
                - 3; // minimum reason string 'r'
    }
}
