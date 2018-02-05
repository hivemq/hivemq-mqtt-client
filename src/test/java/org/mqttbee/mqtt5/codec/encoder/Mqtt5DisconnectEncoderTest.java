package org.mqttbee.mqtt5.codec.encoder;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.EncoderException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;
import org.mqttbee.mqtt5.message.Mqtt5UTF8StringImpl;
import org.mqttbee.mqtt5.message.Mqtt5UserPropertiesImpl;
import org.mqttbee.mqtt5.message.Mqtt5UserPropertyImpl;
import org.mqttbee.mqtt5.message.disconnect.Mqtt5DisconnectImpl;
import org.mqttbee.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;

import java.util.Arrays;

import static java.util.Objects.requireNonNull;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;
import static org.mqttbee.mqtt5.message.disconnect.Mqtt5DisconnectImpl.SESSION_EXPIRY_INTERVAL_FROM_CONNECT;
import static org.mqttbee.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode.MALFORMED_PACKET;
import static org.mqttbee.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode.NORMAL_DISCONNECTION;

/**
 * @author David Katz
 */
class Mqtt5DisconnectEncoderTest extends AbstractMqtt5EncoderTest {

    private static final int VARIABLE_BYTE_INTEGER_VALUE_BITS = 7;
    private static final int VARIABLE_BYTE_INTEGER_FOUR_BYTES_MAX_VALUE =
            (1 << (VARIABLE_BYTE_INTEGER_VALUE_BITS * 4)) - 1;

    Mqtt5DisconnectEncoderTest() {
        super(true);
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
        final Mqtt5UTF8StringImpl reasonString = Mqtt5UTF8StringImpl.from("reason");
        final Mqtt5UTF8StringImpl serverReference = Mqtt5UTF8StringImpl.from("server");
        final long sessionExpiryInterval = 1;
        final Mqtt5UTF8StringImpl test = requireNonNull(Mqtt5UTF8StringImpl.from("test"));
        final Mqtt5UTF8StringImpl value = requireNonNull(Mqtt5UTF8StringImpl.from("value"));
        final Mqtt5UTF8StringImpl value2 = requireNonNull(Mqtt5UTF8StringImpl.from("value2"));
        final Mqtt5UserPropertyImpl userProperty1 = new Mqtt5UserPropertyImpl(test, value);
        final Mqtt5UserPropertyImpl userProperty2 = new Mqtt5UserPropertyImpl(test, value2);
        final Mqtt5UserPropertiesImpl userProperties =
                Mqtt5UserPropertiesImpl.of(ImmutableList.of(userProperty1, userProperty2));

        final Mqtt5DisconnectImpl disconnect =
                new Mqtt5DisconnectImpl(MALFORMED_PACKET, sessionExpiryInterval, serverReference, reasonString,
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

        final Mqtt5DisconnectImpl disconnect =
                new Mqtt5DisconnectImpl(MALFORMED_PACKET, SESSION_EXPIRY_INTERVAL_FROM_CONNECT, null, null,
                        Mqtt5UserPropertiesImpl.NO_USER_PROPERTIES);
        encode(expected, disconnect);
    }

    @ParameterizedTest
    @EnumSource(value = Mqtt5DisconnectReasonCode.class, mode = EXCLUDE, names = "NORMAL_DISCONNECTION")
    void encode_allReasonCodes(final Mqtt5DisconnectReasonCode reasonCode) {
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
        final Mqtt5DisconnectImpl disconnect =
                new Mqtt5DisconnectImpl(reasonCode, SESSION_EXPIRY_INTERVAL_FROM_CONNECT, null, null,
                        Mqtt5UserPropertiesImpl.NO_USER_PROPERTIES);
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

        final Mqtt5DisconnectImpl disconnect =
                new Mqtt5DisconnectImpl(NORMAL_DISCONNECTION, SESSION_EXPIRY_INTERVAL_FROM_CONNECT, null, null,
                        Mqtt5UserPropertiesImpl.NO_USER_PROPERTIES);
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
        final Mqtt5UTF8StringImpl reasonString = Mqtt5UTF8StringImpl.from("reason");
        final Mqtt5DisconnectImpl disconnect =
                new Mqtt5DisconnectImpl(MALFORMED_PACKET, SESSION_EXPIRY_INTERVAL_FROM_CONNECT, null, reasonString,
                        Mqtt5UserPropertiesImpl.NO_USER_PROPERTIES);

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
        final Mqtt5UTF8StringImpl serverReference = Mqtt5UTF8StringImpl.from("server");
        final Mqtt5DisconnectImpl disconnect =
                new Mqtt5DisconnectImpl(MALFORMED_PACKET, SESSION_EXPIRY_INTERVAL_FROM_CONNECT, serverReference, null,
                        Mqtt5UserPropertiesImpl.NO_USER_PROPERTIES);

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
        final Mqtt5DisconnectImpl disconnect =
                new Mqtt5DisconnectImpl(MALFORMED_PACKET, sessionExpiryInterval, null, null,
                        Mqtt5UserPropertiesImpl.NO_USER_PROPERTIES);

        encode(expected, disconnect);
    }

    @Test
    @Disabled
    void encode_maximumPacketSizeExceeded_throwsEncoderException() {
        final MaximumPacketBuilder maxPacket = new MaximumPacketBuilder().build();

        final Mqtt5DisconnectImpl disconnect =
                new Mqtt5DisconnectImpl(MALFORMED_PACKET, SESSION_EXPIRY_INTERVAL_FROM_CONNECT, null,
                        maxPacket.getReasonStringTooLong(), maxPacket.getMaxPossibleUserProperties());

        final Throwable exception = assertThrows(EncoderException.class, () -> channel.writeOutbound(disconnect));
        assertTrue(exception.getMessage().contains("variable byte integer size exceeded for remaining length"));
    }

    @Test
    @Disabled
    void encode_propertyLengthExceedsMax_throwsEncoderException() {
        final MaximumPacketBuilder maxPacket = new MaximumPacketBuilder().build();

        final Mqtt5DisconnectImpl disconnect =
                new Mqtt5DisconnectImpl(MALFORMED_PACKET, SESSION_EXPIRY_INTERVAL_FROM_CONNECT, null, null,
                        maxPacket.getUserProperties(
                                (VARIABLE_BYTE_INTEGER_FOUR_BYTES_MAX_VALUE / maxPacket.userPropertyBytes) + 1));

        final Throwable exception = assertThrows(EncoderException.class, () -> channel.writeOutbound(disconnect));
        assertTrue(exception.getMessage().contains("variable byte integer size exceeded for property length"));
    }

    private void encode(final byte[] expected, final Mqtt5DisconnectImpl disconnect) {
        channel.writeOutbound(disconnect);
        final ByteBuf byteBuf = channel.readOutbound();

        final byte[] actual = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(actual);
        byteBuf.release();

        assertArrayEquals(expected, actual);
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
                - 4; // reason string 'r'

        final int userPropertyBytes = 1 // identifier
                + 2 // key length
                + 4 // bytes to encode "user"
                + 2 // value length
                + 8; // bytes to encode "property"

        MaximumPacketBuilder build() {
            final int reasonStringLength = 1 + (maxPropertyLength % userPropertyBytes);

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
            return Mqtt5UTF8StringImpl.from(new String(reasonStringBytes) + "x");
        }
    }
}
