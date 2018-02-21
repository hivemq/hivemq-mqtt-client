package org.mqttbee.mqtt.codec.encoder.mqtt5;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.EncoderException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mqttbee.api.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import org.mqttbee.mqtt.codec.encoder.AbstractMqtt5EncoderTest;
import org.mqttbee.mqtt.datatypes.MqttUTF8StringImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertyImpl;
import org.mqttbee.mqtt.datatypes.MqttVariableByteInteger;
import org.mqttbee.mqtt.message.disconnect.MqttDisconnectImpl;

import java.util.Arrays;

import static java.util.Objects.requireNonNull;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;
import static org.mqttbee.api.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode.MALFORMED_PACKET;
import static org.mqttbee.api.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode.NORMAL_DISCONNECTION;
import static org.mqttbee.mqtt.message.disconnect.MqttDisconnectImpl.SESSION_EXPIRY_INTERVAL_FROM_CONNECT;

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
        final MqttUTF8StringImpl reasonString = MqttUTF8StringImpl.from("reason");
        final MqttUTF8StringImpl serverReference = MqttUTF8StringImpl.from("server");
        final long sessionExpiryInterval = 1;
        final MqttUTF8StringImpl test = requireNonNull(MqttUTF8StringImpl.from("test"));
        final MqttUTF8StringImpl value = requireNonNull(MqttUTF8StringImpl.from("value"));
        final MqttUTF8StringImpl value2 = requireNonNull(MqttUTF8StringImpl.from("value2"));
        final MqttUserPropertyImpl userProperty1 = new MqttUserPropertyImpl(test, value);
        final MqttUserPropertyImpl userProperty2 = new MqttUserPropertyImpl(test, value2);
        final MqttUserPropertiesImpl userProperties =
                MqttUserPropertiesImpl.of(ImmutableList.of(userProperty1, userProperty2));

        final MqttDisconnectImpl disconnect =
                new MqttDisconnectImpl(MALFORMED_PACKET, sessionExpiryInterval, serverReference, reasonString,
                        userProperties, Mqtt5DisconnectEncoder.PROVIDER);

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

        final MqttDisconnectImpl disconnect =
                new MqttDisconnectImpl(MALFORMED_PACKET, SESSION_EXPIRY_INTERVAL_FROM_CONNECT, null, null,
                        MqttUserPropertiesImpl.NO_USER_PROPERTIES, Mqtt5DisconnectEncoder.PROVIDER);
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
        final MqttDisconnectImpl disconnect =
                new MqttDisconnectImpl(reasonCode, SESSION_EXPIRY_INTERVAL_FROM_CONNECT, null, null,
                        MqttUserPropertiesImpl.NO_USER_PROPERTIES, Mqtt5DisconnectEncoder.PROVIDER);
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

        final MqttDisconnectImpl disconnect =
                new MqttDisconnectImpl(NORMAL_DISCONNECTION, SESSION_EXPIRY_INTERVAL_FROM_CONNECT, null, null,
                        MqttUserPropertiesImpl.NO_USER_PROPERTIES, Mqtt5DisconnectEncoder.PROVIDER);
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
        final MqttUTF8StringImpl reasonString = MqttUTF8StringImpl.from("reason");
        final MqttDisconnectImpl disconnect =
                new MqttDisconnectImpl(MALFORMED_PACKET, SESSION_EXPIRY_INTERVAL_FROM_CONNECT, null, reasonString,
                        MqttUserPropertiesImpl.NO_USER_PROPERTIES, Mqtt5DisconnectEncoder.PROVIDER);

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
        final MqttUTF8StringImpl serverReference = MqttUTF8StringImpl.from("server");
        final MqttDisconnectImpl disconnect =
                new MqttDisconnectImpl(MALFORMED_PACKET, SESSION_EXPIRY_INTERVAL_FROM_CONNECT, serverReference, null,
                        MqttUserPropertiesImpl.NO_USER_PROPERTIES, Mqtt5DisconnectEncoder.PROVIDER);

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
        final MqttDisconnectImpl disconnect =
                new MqttDisconnectImpl(MALFORMED_PACKET, sessionExpiryInterval, null, null,
                        MqttUserPropertiesImpl.NO_USER_PROPERTIES, Mqtt5DisconnectEncoder.PROVIDER);

        encode(expected, disconnect);
    }

    @Test
    @Disabled("transform to encode_maximumPacketSizeExceeded_omitUserPropertiesAndReasonString")
    void encode_maximumPacketSizeExceeded_throwsEncoderException() {
        final MaximumPacketBuilder maxPacket = new MaximumPacketBuilder().build();

        final MqttDisconnectImpl disconnect =
                new MqttDisconnectImpl(MALFORMED_PACKET, SESSION_EXPIRY_INTERVAL_FROM_CONNECT, null,
                        maxPacket.getReasonStringTooLong(), maxPacket.getMaxPossibleUserProperties(),
                        Mqtt5DisconnectEncoder.PROVIDER);

        final Throwable exception = assertThrows(EncoderException.class, () -> channel.writeOutbound(disconnect));
        assertTrue(exception.getMessage().contains("variable byte integer size exceeded for remaining length"));
    }

    @Test
    @Disabled("transform to encode_propertyLengthExceeded_omitUserPropertiesAndReasonString")
    void encode_propertyLengthExceedsMax_throwsEncoderException() {
        final MaximumPacketBuilder maxPacket = new MaximumPacketBuilder().build();

        final MqttDisconnectImpl disconnect =
                new MqttDisconnectImpl(MALFORMED_PACKET, SESSION_EXPIRY_INTERVAL_FROM_CONNECT, null, null,
                        maxPacket.getUserProperties(
                                (VARIABLE_BYTE_INTEGER_FOUR_BYTES_MAX_VALUE / maxPacket.userPropertyBytes) + 1),
                        Mqtt5DisconnectEncoder.PROVIDER);

        final Throwable exception = assertThrows(EncoderException.class, () -> channel.writeOutbound(disconnect));
        assertTrue(exception.getMessage().contains("variable byte integer size exceeded for property length"));
    }

    private void encode(final byte[] expected, final MqttDisconnectImpl disconnect) {
        channel.writeOutbound(disconnect);
        final ByteBuf byteBuf = channel.readOutbound();

        final byte[] actual = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(actual);
        byteBuf.release();

        assertArrayEquals(expected, actual);
    }

    private class MaximumPacketBuilder {

        private ImmutableList.Builder<MqttUserPropertyImpl> userPropertiesBuilder;
        final MqttUserPropertyImpl userProperty =
                new MqttUserPropertyImpl(requireNonNull(MqttUTF8StringImpl.from("user")),
                        requireNonNull(MqttUTF8StringImpl.from("property")));
        char[] reasonStringBytes;
        final int maxPropertyLength = MqttVariableByteInteger.MAXIMUM_PACKET_SIZE_LIMIT - 1  // type, reserved
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

        MqttUserPropertiesImpl getMaxPossibleUserProperties() {
            return MqttUserPropertiesImpl.of(userPropertiesBuilder.build());
        }

        MqttUserPropertiesImpl getUserProperties(final int totalCount) {
            final ImmutableList.Builder<MqttUserPropertyImpl> builder = new ImmutableList.Builder<>();
            for (int i = 0; i < totalCount; i++) {
                builder.add(userProperty);
            }
            return MqttUserPropertiesImpl.of(builder.build());
        }

        MqttUTF8StringImpl getReasonStringTooLong() {
            return MqttUTF8StringImpl.from(new String(reasonStringBytes) + "x");
        }
    }
}
