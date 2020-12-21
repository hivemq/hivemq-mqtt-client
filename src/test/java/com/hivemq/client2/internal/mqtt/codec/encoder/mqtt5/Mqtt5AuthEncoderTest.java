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
import com.hivemq.client2.internal.mqtt.message.auth.MqttAuth;
import com.hivemq.client2.internal.util.collections.ImmutableList;
import com.hivemq.client2.mqtt.mqtt5.message.Mqtt5MessageType;
import com.hivemq.client2.mqtt.mqtt5.message.auth.Mqtt5AuthReasonCode;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.EncoderException;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.nio.ByteBuffer;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author David Katz
 */
class Mqtt5AuthEncoderTest extends AbstractMqtt5EncoderTest {

    private static final int VARIABLE_BYTE_INTEGER_VALUE_BITS = 7;
    private static final int VARIABLE_BYTE_INTEGER_FOUR_BYTES_MAX_VALUE =
            (1 << (VARIABLE_BYTE_INTEGER_VALUE_BITS * 4)) - 1;

    Mqtt5AuthEncoderTest() {
        super(new MqttMessageEncoders() {{
            encoders[Mqtt5MessageType.AUTH.getCode()] = new Mqtt5AuthEncoder();
        }}, true);
    }

    @Test
    void encode_allProperties() {
        final byte[] expected = {
                // fixed header
                //   type, flags
                (byte) 0b1111_0000,
                //   remaining length (132)
                (byte) (128 + 4), 1,
                // variable header
                //   reason code (continue)
                0x18,
                //   properties (129)
                (byte) (128 + 1), 1,
                //     auth method
                0x15, 0, 8, 'G', 'S', '2', '-', 'K', 'R', 'B', '5',
                //     auth data
                0x16, 0, 60, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 1, 2, 3, 4, 5, 6, 7, 8, 9,
                10, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
                //     reason string
                0x1F, 0, 8, 'c', 'o', 'n', 't', 'i', 'n', 'u', 'e',
                //     user properties
                0x26, 0, 4, 't', 'e', 's', 't', 0, 5, 'v', 'a', 'l', 'u', 'e', //
                0x26, 0, 4, 't', 'e', 's', 't', 0, 6, 'v', 'a', 'l', 'u', 'e', '2', //
                0x26, 0, 5, 't', 'e', 's', 't', '2', 0, 5, 'v', 'a', 'l', 'u', 'e',
        };

        final ByteBuffer data = ByteBuffer.wrap(new byte[]{
                1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 1, 2, 3, 4,
                5, 6, 7, 8, 9, 10, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10
        });

        final MqttUtf8StringImpl reasonString = MqttUtf8StringImpl.of("continue");
        final MqttUtf8StringImpl test = MqttUtf8StringImpl.of("test");
        final MqttUtf8StringImpl test2 = MqttUtf8StringImpl.of("test2");
        final MqttUtf8StringImpl value = MqttUtf8StringImpl.of("value");
        final MqttUtf8StringImpl value2 = MqttUtf8StringImpl.of("value2");
        final MqttUserPropertiesImpl userProperties = MqttUserPropertiesImpl.of(
                ImmutableList.of(new MqttUserPropertyImpl(test, value), new MqttUserPropertyImpl(test, value2),
                        new MqttUserPropertyImpl(test2, value)));

        final MqttUtf8StringImpl method = MqttUtf8StringImpl.of("GS2-KRB5");
        final MqttAuth auth =
                new MqttAuth(Mqtt5AuthReasonCode.CONTINUE_AUTHENTICATION, method, data, reasonString, userProperties);
        encode(expected, auth);
    }

    @ParameterizedTest
    @EnumSource(value = Mqtt5AuthReasonCode.class)
    void encode_simple_reasonCodes(final @NotNull Mqtt5AuthReasonCode reasonCode) {
        final byte[] expected = {
                // fixed header
                //   type, flags
                (byte) 0b1111_0000,
                //   remaining length
                6,
                // variable header
                //   reason code placeholder
                (byte) reasonCode.getCode(),
                //   properties
                4,
                //     auth method
                0x15, 0, 1, 'x'
        };

        final MqttUtf8StringImpl method = MqttUtf8StringImpl.of("x");
        final MqttAuth auth = new MqttAuth(reasonCode, method, null, null, MqttUserPropertiesImpl.NO_USER_PROPERTIES);
        encode(expected, auth);
    }

    @Test
    void encode_authenticationData() {
        final byte[] expected = {
                // fixed header
                //   type, flags
                (byte) 0b1111_0000,
                //   remaining length
                10,
                // variable header
                //   reason code (continue)
                0x18,
                //   properties
                8,
                //     auth method
                0x15, 0, 1, 'x',
                //     auth data
                0x16, 0, 1, 1
        };

        final ByteBuffer data = ByteBuffer.wrap(new byte[]{1});
        final MqttUtf8StringImpl method = MqttUtf8StringImpl.of("x");
        final MqttAuth auth = new MqttAuth(Mqtt5AuthReasonCode.CONTINUE_AUTHENTICATION, method, data, null,
                MqttUserPropertiesImpl.NO_USER_PROPERTIES);
        encode(expected, auth);
    }

    @Test
    @Disabled("auth data will be validated in the builder, remove this test")
    void encode_authenticationDataTooLarge_throwsEncoderException() {
        final ByteBuffer data = ByteBuffer.wrap(new byte[65536]);
        final MqttUtf8StringImpl method = MqttUtf8StringImpl.of("x");
        final MqttAuth auth = new MqttAuth(Mqtt5AuthReasonCode.CONTINUE_AUTHENTICATION, method, data, null,
                MqttUserPropertiesImpl.NO_USER_PROPERTIES);
        encodeNok(auth, EncoderException.class, "binary data size exceeded for authentication data");
    }

    @Test
    void encode_reasonString() {
        final byte[] expected = {
                // fixed header
                //   type, flags
                (byte) 0b1111_0000,
                //   remaining length
                15,
                // variable header
                //   reason code (continue)
                0x18,
                //   properties
                13,
                //     auth method
                0x15, 0, 1, 'x',
                //     reason string
                0x1F, 0, 6, 'r', 'e', 'a', 's', 'o', 'n'
        };

        final MqttUtf8StringImpl reasonString = MqttUtf8StringImpl.of("reason");
        final MqttUtf8StringImpl method = MqttUtf8StringImpl.of("x");
        final MqttAuth auth = new MqttAuth(Mqtt5AuthReasonCode.CONTINUE_AUTHENTICATION, method, null, reasonString,
                MqttUserPropertiesImpl.NO_USER_PROPERTIES);
        encode(expected, auth);
    }

    @Test
    void encode_reasonStringEmpty() {
        final byte[] expected = {
                // fixed header
                //   type, flags
                (byte) 0b1111_0000,
                //   remaining length
                9,
                // variable header
                //   reason code (continue)
                0x18,
                //   properties
                7,
                //     auth method
                0x15, 0, 1, 'x',
                //     reason string
                0x1F, 0, 0
        };

        final MqttUtf8StringImpl reasonString = MqttUtf8StringImpl.of("");
        final MqttUtf8StringImpl method = MqttUtf8StringImpl.of("x");
        final MqttAuth auth = new MqttAuth(Mqtt5AuthReasonCode.CONTINUE_AUTHENTICATION, method, null, reasonString,
                MqttUserPropertiesImpl.NO_USER_PROPERTIES);
        encode(expected, auth);
    }

    @Test
    void encode_maximumPacketSizeExceededByReasonString_omitReasonString_keepUserProperties() {
        final MaximumPacketBuilder maxPacket = new MaximumPacketBuilder().build();

        final MqttAuth auth = new MqttAuth(Mqtt5AuthReasonCode.CONTINUE_AUTHENTICATION, maxPacket.getMethod(), null,
                maxPacket.getReasonStringTooLong(), maxPacket.getMaxPossibleUserProperties());

        encode(maxPacket.getWithOmittedReasonString(), auth);
    }

    @Test
    void encode_maximumPacketSizeExceededByUserProperties_omitUserPropertiesAndReasonString() {
        final MaximumPacketBuilder maxPacket = new MaximumPacketBuilder();
        final MqttUserPropertiesImpl tooManyUserProperties = maxPacket.getUserProperties(
                (VARIABLE_BYTE_INTEGER_FOUR_BYTES_MAX_VALUE / maxPacket.userPropertyBytes) + 1);

        final MqttAuth auth =
                new MqttAuth(Mqtt5AuthReasonCode.CONTINUE_AUTHENTICATION, maxPacket.getMethod(), null, null,
                        tooManyUserProperties);

        encode(maxPacket.getWithOmittedUserPropertiesAndReasonString(), auth);
    }

    private void encode(final @NotNull byte[] expected, final @NotNull MqttAuth auth) {
        encode(auth, expected);
    }

    private void encodeNok(
            final @NotNull MqttAuth auth,
            final @NotNull Class<? extends Exception> expectedException,
            final @NotNull String reason) {

        final Throwable exception = assertThrows(expectedException, () -> channel.writeOutbound(auth));
        assertTrue(exception.getMessage().contains(reason), () -> "found: " + exception.getMessage());
    }

    @SuppressWarnings("NullabilityAnnotations")
    private class MaximumPacketBuilder {

        private ImmutableList.Builder<MqttUserPropertyImpl> userPropertiesBuilder;
        final MqttUserPropertyImpl userProperty =
                new MqttUserPropertyImpl(MqttUtf8StringImpl.of("user"), MqttUtf8StringImpl.of("property"));

        char[] reasonStringBytes;
        final int maxPropertyLength = MqttVariableByteInteger.MAXIMUM_PACKET_SIZE_LIMIT - 1  // type, reserved
                - 4  // remaining length
                - 1  // reason code
                - 4  // properties length
                - 4  // auth method 'x'
                - 4; // reason string 'r'

        final int userPropertyBytes = 1 // identifier
                + 2 // key length
                + 4 // bytes to encode "user"
                + 2 // value length
                + 8; // bytes to encode "property"

        MaximumPacketBuilder build() {
            final int reasonStringLength = maxPropertyLength % userPropertyBytes;

            reasonStringBytes = new char[reasonStringLength];
            Arrays.fill(reasonStringBytes, 'r');

            final int numberOfUserProperties = maxPropertyLength / userPropertyBytes;
            userPropertiesBuilder = ImmutableList.builder();
            for (int i = 0; i < numberOfUserProperties; i++) {
                userPropertiesBuilder.add(userProperty);
            }
            return this;
        }

        MqttUserPropertiesImpl getMaxPossibleUserProperties() {
            return MqttUserPropertiesImpl.of(userPropertiesBuilder.build());
        }

        MqttUserPropertiesImpl getUserProperties(final int totalCount) {
            final ImmutableList.Builder<MqttUserPropertyImpl> builder = ImmutableList.builder();
            for (int i = 0; i < totalCount; i++) {
                builder.add(userProperty);
            }
            return MqttUserPropertiesImpl.of(builder.build());
        }

        MqttUtf8StringImpl getReasonStringTooLong() {
            return MqttUtf8StringImpl.of("r" + new String(reasonStringBytes) + "x");
        }

        MqttUtf8StringImpl getMethod() {
            return MqttUtf8StringImpl.of("x");
        }

        byte[] getWithOmittedUserPropertiesAndReasonString() {
            return new byte[]{
                    // fixed header
                    //   type, flags
                    (byte) 0b1111_0000,
                    //   remaining length
                    6,
                    // variable header
                    //   reason code (continue)
                    0x18,
                    //   properties
                    4,
                    //     auth method
                    0x15, 0, 1, 'x'
            };
        }

        byte[] getWithOmittedReasonString() {
            final int userPropertyCount = userPropertiesBuilder.build().size();

            final int remainingLength = 9 + userPropertyBytes * userPropertyCount;
            final byte remainingLength1 = (byte) (remainingLength % 128 + 0b1000_0000);
            final byte remainingLength2 = (byte) ((remainingLength >>> 7) % 128 + 0b1000_0000);
            final byte remainingLength3 = (byte) ((remainingLength >>> 14) % 128 + 0b1000_0000);
            final byte remainingLength4 = (byte) ((remainingLength >>> 21) % 128);

            final int propertyLength = 4 + userPropertyBytes * userPropertyCount;
            final byte propertyLength1 = (byte) (propertyLength % 128 + 0b1000_0000);
            final byte propertyLength2 = (byte) ((propertyLength >>> 7) % 128 + 0b1000_0000);
            final byte propertyLength3 = (byte) ((propertyLength >>> 14) % 128 + 0b1000_0000);
            final byte propertyLength4 = (byte) ((propertyLength >>> 21) % 128);

            final ByteBuf byteBuf = Unpooled.buffer(5 + remainingLength, 5 + remainingLength);

            // fixed header
            //   type, flags
            byteBuf.writeByte(0b1111_0000);
            //   remaining length
            byteBuf.writeByte(remainingLength1);
            byteBuf.writeByte(remainingLength2);
            byteBuf.writeByte(remainingLength3);
            byteBuf.writeByte(remainingLength4);
            // variable header
            //   reason code (continue)
            byteBuf.writeByte(0x18);
            //   properties
            byteBuf.writeByte(propertyLength1);
            byteBuf.writeByte(propertyLength2);
            byteBuf.writeByte(propertyLength3);
            byteBuf.writeByte(propertyLength4);
            //     auth method
            byteBuf.writeBytes(new byte[]{0x15, 0, 1, 'x'});

            final byte[] userPropertyByteArray = {
                    0x26, 0, 4, 'u', 's', 'e', 'r', 0, 8, 'p', 'r', 'o', 'p', 'e', 'r', 't', 'y'
            };
            for (int i = 0; i < userPropertyCount; i++) {
                byteBuf.writeBytes(userPropertyByteArray);
            }
            return byteBuf.array();
        }
    }
}