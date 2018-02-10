package org.mqttbee.mqtt5.codec.encoder;

import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Bytes;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.EncoderException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mqttbee.api.mqtt5.message.auth.Mqtt5AuthReasonCode;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;
import org.mqttbee.mqtt5.message.Mqtt5UTF8StringImpl;
import org.mqttbee.mqtt5.message.Mqtt5UserPropertiesImpl;
import org.mqttbee.mqtt5.message.Mqtt5UserPropertyImpl;
import org.mqttbee.mqtt5.message.auth.Mqtt5AuthImpl;

import java.util.Arrays;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author David Katz
 */
class Mqtt5AuthEncoderTest extends AbstractMqtt5EncoderTest {

    private static final int VARIABLE_BYTE_INTEGER_VALUE_BITS = 7;
    private static final int VARIABLE_BYTE_INTEGER_FOUR_BYTES_MAX_VALUE =
            (1 << (VARIABLE_BYTE_INTEGER_VALUE_BITS * 4)) - 1;

    Mqtt5AuthEncoderTest() {
        super(true);
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

        final Mqtt5UTF8StringImpl reasonString = Mqtt5UTF8StringImpl.from("continue");
        final Mqtt5UTF8StringImpl test = requireNonNull(Mqtt5UTF8StringImpl.from("test"));
        final Mqtt5UTF8StringImpl test2 = requireNonNull(Mqtt5UTF8StringImpl.from("test2"));
        final Mqtt5UTF8StringImpl value = requireNonNull(Mqtt5UTF8StringImpl.from("value"));
        final Mqtt5UTF8StringImpl value2 = requireNonNull(Mqtt5UTF8StringImpl.from("value2"));
        final Mqtt5UserPropertiesImpl userProperties = Mqtt5UserPropertiesImpl.of(ImmutableList
                .of(new Mqtt5UserPropertyImpl(test, value), new Mqtt5UserPropertyImpl(test, value2),
                        new Mqtt5UserPropertyImpl(test2, value)));

        final Mqtt5UTF8StringImpl method = requireNonNull(Mqtt5UTF8StringImpl.from("GS2-KRB5"));
        final Mqtt5AuthImpl auth =
                new Mqtt5AuthImpl(Mqtt5AuthReasonCode.CONTINUE_AUTHENTICATION, method, data, reasonString,
                        userProperties, Mqtt5AuthEncoder.PROVIDER);
        encode(expected, auth);
    }

    @ParameterizedTest
    @EnumSource(value = Mqtt5AuthReasonCode.class)
    void encode_simple_reasonCodes(final Mqtt5AuthReasonCode reasonCode) {
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

        final Mqtt5UTF8StringImpl method = requireNonNull(Mqtt5UTF8StringImpl.from("x"));
        final Mqtt5AuthImpl auth =
                new Mqtt5AuthImpl(reasonCode, method, null, null, Mqtt5UserPropertiesImpl.NO_USER_PROPERTIES,
                        Mqtt5AuthEncoder.PROVIDER);
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

        final byte[] data = new byte[]{1};
        final Mqtt5UTF8StringImpl method = requireNonNull(Mqtt5UTF8StringImpl.from("x"));
        final Mqtt5AuthImpl auth = new Mqtt5AuthImpl(Mqtt5AuthReasonCode.CONTINUE_AUTHENTICATION, method, data, null,
                Mqtt5UserPropertiesImpl.NO_USER_PROPERTIES, Mqtt5AuthEncoder.PROVIDER);
        encode(expected, auth);
    }

    @Test
    @Disabled("auth data will be validated in the builder, remove this test")
    void encode_authenticationDataTooLarge_throwsEncoderException() {
        final byte[] data = new byte[65536];
        final Mqtt5UTF8StringImpl method = requireNonNull(Mqtt5UTF8StringImpl.from("x"));
        final Mqtt5AuthImpl auth = new Mqtt5AuthImpl(Mqtt5AuthReasonCode.CONTINUE_AUTHENTICATION, method, data, null,
                Mqtt5UserPropertiesImpl.NO_USER_PROPERTIES, Mqtt5AuthEncoder.PROVIDER);
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

        final Mqtt5UTF8StringImpl reasonString = Mqtt5UTF8StringImpl.from("reason");
        final Mqtt5UTF8StringImpl method = requireNonNull(Mqtt5UTF8StringImpl.from("x"));
        final Mqtt5AuthImpl auth =
                new Mqtt5AuthImpl(Mqtt5AuthReasonCode.CONTINUE_AUTHENTICATION, method, null, reasonString,
                        Mqtt5UserPropertiesImpl.NO_USER_PROPERTIES, Mqtt5AuthEncoder.PROVIDER);
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

        final Mqtt5UTF8StringImpl reasonString = Mqtt5UTF8StringImpl.from("");
        final Mqtt5UTF8StringImpl method = requireNonNull(Mqtt5UTF8StringImpl.from("x"));
        final Mqtt5AuthImpl auth =
                new Mqtt5AuthImpl(Mqtt5AuthReasonCode.CONTINUE_AUTHENTICATION, method, null, reasonString,
                        Mqtt5UserPropertiesImpl.NO_USER_PROPERTIES, Mqtt5AuthEncoder.PROVIDER);
        encode(expected, auth);
    }

    @Test
    void encode_maximumPacketSizeExceededByReasonString_omitReasonString_keepUserProperties() {
        final MaximumPacketBuilder maxPacket = new MaximumPacketBuilder().build();

        final Mqtt5AuthImpl auth =
                new Mqtt5AuthImpl(Mqtt5AuthReasonCode.CONTINUE_AUTHENTICATION, maxPacket.getMethod(), null,
                        maxPacket.getReasonStringTooLong(), maxPacket.getMaxPossibleUserProperties(),
                        Mqtt5AuthEncoder.PROVIDER);

        encode(maxPacket.getWithOmittedReasonString(), auth);
    }

    @Test
    void encode_maximumPacketSizeExceededByUserProperties_omitUserPropertiesAndReasonString() {
        final MaximumPacketBuilder maxPacket = new MaximumPacketBuilder();
        final Mqtt5UserPropertiesImpl tooManyUserProperties = maxPacket
                .getUserProperties((VARIABLE_BYTE_INTEGER_FOUR_BYTES_MAX_VALUE / maxPacket.userPropertyBytes) + 1);

        final Mqtt5AuthImpl auth =
                new Mqtt5AuthImpl(Mqtt5AuthReasonCode.CONTINUE_AUTHENTICATION, maxPacket.getMethod(), null, null,
                        tooManyUserProperties, Mqtt5AuthEncoder.PROVIDER);

        encode(maxPacket.getWithOmittedUserPropertiesAndReasonString(), auth);
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

        private ImmutableList.Builder<Mqtt5UserPropertyImpl> userPropertiesBuilder;
        final Mqtt5UserPropertyImpl userProperty =
                new Mqtt5UserPropertyImpl(requireNonNull(Mqtt5UTF8StringImpl.from("user")),
                        requireNonNull(Mqtt5UTF8StringImpl.from("property")));

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

        Mqtt5UserPropertiesImpl getMaxPossibleUserProperties() {
            return Mqtt5UserPropertiesImpl.of(userPropertiesBuilder.build());
        }

        Mqtt5UserPropertiesImpl getUserProperties(final int totalCount) {
            final ImmutableList.Builder<Mqtt5UserPropertyImpl> builder = new ImmutableList.Builder<>();
            for (int i = 0; i < totalCount; i++) {
                builder.add(userProperty);
            }
            return Mqtt5UserPropertiesImpl.of(builder.build());
        }

        Mqtt5UTF8StringImpl getReasonStringTooLong() {
            return Mqtt5UTF8StringImpl.from("r" + new String(reasonStringBytes) + "x");
        }

        Mqtt5UTF8StringImpl getMethod() {
            return requireNonNull(Mqtt5UTF8StringImpl.from("x"));
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

            byte[] encoded = {
                    // fixed header
                    //   type, flags
                    (byte) 0b1111_0000,
                    //   remaining length
                    remainingLength1, remainingLength2, remainingLength3, remainingLength4,
                    // variable header
                    //   reason code (continue)
                    0x18,
                    //   properties
                    propertyLength1, propertyLength2, propertyLength3, propertyLength4,
                    //     auth method
                    0x15, 0, 1, 'x'
            };
            final byte[] userPropertyByteArray = {
                    0x26, 0, 4, 'u', 's', 'e', 'r', 0, 8, 'p', 'r', 'o', 'p', 'e', 'r', 't', 'y'
            };
            final byte[][] properties = new byte[userPropertyCount][];
            Arrays.fill(properties, userPropertyByteArray);
            encoded = Bytes.concat(encoded, Bytes.concat(properties));
            return encoded;
        }
    }
}