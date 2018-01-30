package org.mqttbee.mqtt5.codec.encoder;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.EncoderException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;
import org.mqttbee.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.mqtt5.message.Mqtt5UserProperties;
import org.mqttbee.mqtt5.message.Mqtt5UserProperty;
import org.mqttbee.mqtt5.message.auth.Mqtt5AuthImpl;
import org.mqttbee.mqtt5.message.auth.Mqtt5AuthReasonCode;

import java.util.Arrays;

import static java.util.Objects.requireNonNull;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author David Katz
 */
class Mqtt5AuthEncoderTest {

    private static final int VARIABLE_BYTE_INTEGER_VALUE_BITS = 7;
    private static final int VARIABLE_BYTE_INTEGER_FOUR_BYTES_MAX_VALUE =
            (1 << (VARIABLE_BYTE_INTEGER_VALUE_BITS * 4)) - 1;
    private EmbeddedChannel channel;

    @BeforeEach
    void setUp() {
        channel = new EmbeddedChannel(new Mqtt5Encoder());
    }

    @AfterEach
    void tearDown() {
        channel.close();
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

        final byte[] data = new byte[]{
                1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 1, 2, 3, 4,
                5, 6, 7, 8, 9, 10, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10
        };

        final Mqtt5UTF8String reasonString = Mqtt5UTF8String.from("continue");
        final Mqtt5UTF8String test = requireNonNull(Mqtt5UTF8String.from("test"));
        final Mqtt5UTF8String test2 = requireNonNull(Mqtt5UTF8String.from("test2"));
        final Mqtt5UTF8String value = requireNonNull(Mqtt5UTF8String.from("value"));
        final Mqtt5UTF8String value2 = requireNonNull(Mqtt5UTF8String.from("value2"));
        final Mqtt5UserProperties userProperties = Mqtt5UserProperties.of(ImmutableList
                .of(new Mqtt5UserProperty(test, value), new Mqtt5UserProperty(test, value2),
                        new Mqtt5UserProperty(test2, value)));

        final Mqtt5UTF8String method = requireNonNull(Mqtt5UTF8String.from("GS2-KRB5"));
        final Mqtt5AuthImpl auth =
                new Mqtt5AuthImpl(Mqtt5AuthReasonCode.CONTINUE_AUTHENTICATION, method, data, reasonString,
                        userProperties);
        encode(expected, auth);
    }

    @Test
    void encode_simpleSuccess() {
        final byte[] expected = {
                // fixed header
                //   type, flags
                (byte) 0b1111_0000,
                //   remaining length
                5,
                // variable header
                //   reason code (continue)
                0x00,
                //   properties
                (byte) 3,
                //     auth method
                0x15, 0, 0
        };

        final Mqtt5UTF8String method = requireNonNull(Mqtt5UTF8String.from(""));
        final Mqtt5AuthImpl auth = new Mqtt5AuthImpl(Mqtt5AuthReasonCode.SUCCESS, method, null, null,
                Mqtt5UserProperties.DEFAULT_NO_USER_PROPERTIES);
        encode(expected, auth);
    }

    @Test
    void encode_simple() {
        final byte[] expected = {
                // fixed header
                //   type, flags
                (byte) 0b1111_0000,
                //   remaining length
                6,
                // variable header
                //   reason code (continue)
                0x18,
                //   properties
                (byte) 4,
                //     auth method
                0x15, 0, 1, 'x'
        };

        final Mqtt5UTF8String method = requireNonNull(Mqtt5UTF8String.from("x"));
        final Mqtt5AuthImpl auth = new Mqtt5AuthImpl(Mqtt5AuthReasonCode.CONTINUE_AUTHENTICATION, method, null, null,
                Mqtt5UserProperties.DEFAULT_NO_USER_PROPERTIES);
        encode(expected, auth);
    }

    @ParameterizedTest
    @EnumSource(value = Mqtt5AuthReasonCode.class, mode = EnumSource.Mode.EXCLUDE, names = "SUCCESS")
    void encode_reasonCodes(final Mqtt5AuthReasonCode reasonCode) {

        final byte[] expected = {
                // fixed header
                //   type, flags
                (byte) 0b1111_0000,
                //   remaining length (132)
                6,
                // variable header
                //   reason code placeholder
                (byte) 0xFF,
                //   properties
                4,
                //     auth method
                0x15, 0, 1, 'x'
        };

        expected[2] = (byte) reasonCode.getCode();
        final Mqtt5UTF8String method = requireNonNull(Mqtt5UTF8String.from("x"));
        final Mqtt5AuthImpl auth =
                new Mqtt5AuthImpl(reasonCode, method, null, null, Mqtt5UserProperties.DEFAULT_NO_USER_PROPERTIES);
        encode(expected, auth);
    }

    @Test
    void encode_authenticationData() {
        final byte[] expected = {
                // fixed header
                //   type, flags
                (byte) 0b1111_0000,
                //   remaining length (132)
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

        final byte[] data = new byte[]{1};
        final Mqtt5UTF8String method = requireNonNull(Mqtt5UTF8String.from("x"));
        final Mqtt5AuthImpl auth = new Mqtt5AuthImpl(Mqtt5AuthReasonCode.CONTINUE_AUTHENTICATION, method, data, null,
                Mqtt5UserProperties.DEFAULT_NO_USER_PROPERTIES);
        encode(expected, auth);
    }

    @Test
    void encode_authenticationDataTooLarge_throwsEncoderException() {
        final byte[] data = new byte[65536];
        final Mqtt5UTF8String method = requireNonNull(Mqtt5UTF8String.from("x"));
        final Mqtt5AuthImpl auth = new Mqtt5AuthImpl(Mqtt5AuthReasonCode.CONTINUE_AUTHENTICATION, method, data, null,
                Mqtt5UserProperties.DEFAULT_NO_USER_PROPERTIES);
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

        final Mqtt5UTF8String reasonString = Mqtt5UTF8String.from("reason");
        final Mqtt5UTF8String method = requireNonNull(Mqtt5UTF8String.from("x"));
        final Mqtt5AuthImpl auth =
                new Mqtt5AuthImpl(Mqtt5AuthReasonCode.CONTINUE_AUTHENTICATION, method, null, reasonString,
                        Mqtt5UserProperties.DEFAULT_NO_USER_PROPERTIES);
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

        final Mqtt5UTF8String reasonString = Mqtt5UTF8String.from("");
        final Mqtt5UTF8String method = requireNonNull(Mqtt5UTF8String.from("x"));
        final Mqtt5AuthImpl auth =
                new Mqtt5AuthImpl(Mqtt5AuthReasonCode.CONTINUE_AUTHENTICATION, method, null, reasonString,
                        Mqtt5UserProperties.DEFAULT_NO_USER_PROPERTIES);
        encode(expected, auth);
    }


    @Test
    void encode_maximumPacketSizeExceeded_throwsEncoderException() {
        final MaximumPacketBuilder maxPacket = new MaximumPacketBuilder().build();
        final Mqtt5UTF8String method = requireNonNull(Mqtt5UTF8String.from("x"));
        final Mqtt5AuthImpl auth = new Mqtt5AuthImpl(Mqtt5AuthReasonCode.CONTINUE_AUTHENTICATION, method, null,
                maxPacket.getReasonStringTooLong(), maxPacket.getMaxPossibleUserProperties());
        encodeNok(auth, EncoderException.class, "variable byte integer size exceeded for remaining length");
    }

    @Test
    void encode_propertyLengthExceedsMax_throwsEncoderException() {
        final MaximumPacketBuilder maxPacket = new MaximumPacketBuilder();
        final Mqtt5UTF8String method = requireNonNull(Mqtt5UTF8String.from("x"));
        final Mqtt5UserProperties tooManyUserProperties = maxPacket
                .getUserProperties((VARIABLE_BYTE_INTEGER_FOUR_BYTES_MAX_VALUE / maxPacket.userPropertyBytes) + 1);

        final Mqtt5AuthImpl auth = new Mqtt5AuthImpl(Mqtt5AuthReasonCode.CONTINUE_AUTHENTICATION, method, null, null,
                tooManyUserProperties);
        encodeNok(auth, EncoderException.class, "variable byte integer size exceeded for property length");
    }


    private void encode(final byte[] expected, final Mqtt5AuthImpl auth) {
        channel.writeOutbound(auth);
        final ByteBuf byteBuf = channel.readOutbound();

        final byte[] actual = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(actual);
        byteBuf.release();

        assertArrayEquals(expected, actual);
    }

    private void encodeNok(
            final Mqtt5AuthImpl auth, final Class<? extends Exception> expectedException, final String reason) {
        final Throwable exception = assertThrows(expectedException, () -> channel.writeOutbound(auth));
        assertTrue(exception.getMessage().contains(reason), () -> "found: " + exception.getMessage());
    }

    private class MaximumPacketBuilder {

        private ImmutableList.Builder<Mqtt5UserProperty> userPropertiesBuilder;
        final Mqtt5UserProperty userProperty = new Mqtt5UserProperty(
                requireNonNull(Mqtt5UTF8String.from("user")),
                requireNonNull(Mqtt5UTF8String.from("property")));

        char[] reasonStringBytes;
        final int maxPropertyLength = Mqtt5DataTypes.MAXIMUM_PACKET_SIZE_LIMIT - 1  // type, reserved
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
            userPropertiesBuilder = new ImmutableList.Builder<>();
            for (int i = 0; i < numberOfUserProperties; i++) {
                userPropertiesBuilder.add(userProperty);
            }
            return this;
        }

        Mqtt5UserProperties getMaxPossibleUserProperties() {
            return Mqtt5UserProperties.of(userPropertiesBuilder.build());
        }

        Mqtt5UserProperties getUserProperties(final int totalCount) {
            final ImmutableList.Builder<Mqtt5UserProperty> builder = new ImmutableList.Builder<>();
            for (int i = 0; i < totalCount; i++) {
                builder.add(userProperty);
            }
            return Mqtt5UserProperties.of(builder.build());
        }

        Mqtt5UTF8String getReasonStringTooLong() {
            return Mqtt5UTF8String.from("r" + new String(reasonStringBytes) + "x");
        }
    }
}