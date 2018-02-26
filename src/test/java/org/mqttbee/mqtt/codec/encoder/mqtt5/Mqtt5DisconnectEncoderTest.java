package org.mqttbee.mqtt.codec.encoder.mqtt5;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mqttbee.api.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import org.mqttbee.mqtt.codec.encoder.AbstractMqtt5EncoderTest;
import org.mqttbee.mqtt.datatypes.MqttUTF8StringImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertyImpl;
import org.mqttbee.mqtt.datatypes.MqttVariableByteInteger;
import org.mqttbee.mqtt.message.disconnect.MqttDisconnect;

import java.util.Arrays;

import static java.util.Objects.requireNonNull;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;
import static org.mqttbee.api.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode.*;
import static org.mqttbee.mqtt.message.disconnect.MqttDisconnect.SESSION_EXPIRY_INTERVAL_FROM_CONNECT;

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

        final MqttDisconnect disconnect =
                new MqttDisconnect(MALFORMED_PACKET, sessionExpiryInterval, serverReference, reasonString,
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

        final MqttDisconnect disconnect =
                new MqttDisconnect(MALFORMED_PACKET, SESSION_EXPIRY_INTERVAL_FROM_CONNECT, null, null,
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
        final MqttDisconnect disconnect =
                new MqttDisconnect(reasonCode, SESSION_EXPIRY_INTERVAL_FROM_CONNECT, null, null,
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

        final MqttDisconnect disconnect =
                new MqttDisconnect(NORMAL_DISCONNECTION, SESSION_EXPIRY_INTERVAL_FROM_CONNECT, null, null,
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
        final MqttDisconnect disconnect =
                new MqttDisconnect(MALFORMED_PACKET, SESSION_EXPIRY_INTERVAL_FROM_CONNECT, null, reasonString,
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
        final MqttDisconnect disconnect =
                new MqttDisconnect(MALFORMED_PACKET, SESSION_EXPIRY_INTERVAL_FROM_CONNECT, serverReference, null,
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
        final MqttDisconnect disconnect = new MqttDisconnect(MALFORMED_PACKET, sessionExpiryInterval, null, null,
                MqttUserPropertiesImpl.NO_USER_PROPERTIES, Mqtt5DisconnectEncoder.PROVIDER);

        encode(expected, disconnect);
    }

    @Test
    void encode_maximumPacketSizeExceededOnSuccess_omitUserProperties() {
        final MaximumPacketBuilder maxPacket = new MaximumPacketBuilder().build();
        final byte[] expected = {
                // fixed header
                //   type, flags
                (byte) 0b1110_0000,
                // remaining length
                0
        };

        final MqttDisconnect disconnect =
                new MqttDisconnect(NORMAL_DISCONNECTION, SESSION_EXPIRY_INTERVAL_FROM_CONNECT, null, null,
                        maxPacket.getUserProperties(maxPacket.getMaxPossibleUserPropertiesCount() + 1),
                        Mqtt5DisconnectEncoder.PROVIDER);

        encode(expected, disconnect);
    }

    @Test
    void encode_maximumPacketSizeExceeded_omitReasonString() {
        final MaximumPacketBuilder maxPacket = new MaximumPacketBuilder().build();
        final MqttUserPropertiesImpl maxUserProperties =
                maxPacket.getUserProperties(maxPacket.getMaxPossibleUserPropertiesCount());
        final MqttUTF8StringImpl reasonString = maxPacket.getReasonStringTooLong();

        final ByteBuf expected = Unpooled.buffer();

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
                        maxUserProperties, Mqtt5DisconnectEncoder.PROVIDER);

        final byte[] expectedBytes = new byte[expected.readableBytes()];
        expected.readBytes(expectedBytes);
        encode(expectedBytes, disconnect);
    }

    @Test
    void encode_maximumPacketSizeExceeded_omitUserProperties() {
        final MaximumPacketBuilder maxPacket = new MaximumPacketBuilder().build();
        final byte[] expected = {
                // fixed header
                //   type, flags
                (byte) 0b1110_0000,
                // remaining length
                1, (byte) 0x82
        };

        final MqttDisconnect disconnect =
                new MqttDisconnect(PROTOCOL_ERROR, SESSION_EXPIRY_INTERVAL_FROM_CONNECT, null, null,
                        maxPacket.getUserProperties(maxPacket.getMaxPossibleUserPropertiesCount() + 1),
                        Mqtt5DisconnectEncoder.PROVIDER);

        encode(expected, disconnect);
    }

    @Test
    void encode_propertyLengthExceededOnSuccess_omitUserProperties() {
        final MaximumPacketBuilder maxPacket = new MaximumPacketBuilder().build();
        final byte[] expected = {
                // fixed header
                //   type, flags
                (byte) 0b1110_0000,
                // remaining length
                0
        };

        final MqttDisconnect disconnect =
                new MqttDisconnect(NORMAL_DISCONNECTION, SESSION_EXPIRY_INTERVAL_FROM_CONNECT, null, null,
                        maxPacket.getUserProperties(
                                (VARIABLE_BYTE_INTEGER_FOUR_BYTES_MAX_VALUE / maxPacket.userPropertyBytes) + 1),
                        Mqtt5DisconnectEncoder.PROVIDER);

        encode(expected, disconnect);
    }

    @Test
    void encode_propertyLengthExceeded_omitUserProperties() {
        final MaximumPacketBuilder maxPacket = new MaximumPacketBuilder().build();
        final byte[] expected = {
                // fixed header
                //   type, flags
                (byte) 0b1110_0000,
                // remaining length
                1, (byte) 0x81
        };

        final MqttDisconnect disconnect =
                new MqttDisconnect(MALFORMED_PACKET, SESSION_EXPIRY_INTERVAL_FROM_CONNECT, null, null,
                        maxPacket.getUserProperties(
                                (VARIABLE_BYTE_INTEGER_FOUR_BYTES_MAX_VALUE / maxPacket.userPropertyBytes) + 1),
                        Mqtt5DisconnectEncoder.PROVIDER);

        encode(expected, disconnect);
    }

    @Test
    void encode_propertyLengthExceeded_omitReasonString() {
        final MaximumPacketBuilder maxPacket = new MaximumPacketBuilder().build();
        final int maxUserPropertiesCount = VARIABLE_BYTE_INTEGER_FOUR_BYTES_MAX_VALUE / maxPacket.userPropertyBytes;
        final MqttUserPropertiesImpl maxUserProperties = maxPacket.getUserProperties(maxUserPropertiesCount);
        final int maxReasonStringLength = VARIABLE_BYTE_INTEGER_FOUR_BYTES_MAX_VALUE % maxPacket.userPropertyBytes;
        final char[] reasonStringBytes = new char[maxReasonStringLength];
        Arrays.fill(reasonStringBytes, 'r');
        final MqttUTF8StringImpl reasonString = MqttUTF8StringImpl.from(new String(reasonStringBytes));

        final ByteBuf expected = Unpooled.buffer();

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
                        maxUserProperties, Mqtt5DisconnectEncoder.PROVIDER);

        final byte[] expectedBytes = new byte[expected.readableBytes()];
        expected.readBytes(expectedBytes);
        encode(expectedBytes, disconnect);
    }

    private void encode(final byte[] expected, final MqttDisconnect disconnect) {
        channel.writeOutbound(disconnect);
        final ByteBuf byteBuf = channel.readOutbound();

        final byte[] actual = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(actual);
        byteBuf.release();

        assertArrayEquals(expected, actual);
    }

    private class MaximumPacketBuilder {

        final MqttUserPropertyImpl userProperty =
                new MqttUserPropertyImpl(requireNonNull(MqttUTF8StringImpl.from("user")),
                        requireNonNull(MqttUTF8StringImpl.from("property")));
        char[] reasonStringBytes;
        final int maxPropertyLength = MqttVariableByteInteger.MAXIMUM_PACKET_SIZE_LIMIT - 1  // type, reserved
                - 4  // remaining length
                - 1  // reason code
                - 4  // properties length
                - 3; // minimum reason string 'r'

        final int userPropertyBytes = 1 // identifier
                + 2 // key length
                + 4 // bytes to encode "user"
                + 2 // value length
                + 8; // bytes to encode "property"

        int maxNumberOfUserProperties;

        MaximumPacketBuilder build() {
            final int reasonStringLength = 1 + (maxPropertyLength % userPropertyBytes);

            reasonStringBytes = new char[reasonStringLength];
            Arrays.fill(reasonStringBytes, 'r');

            maxNumberOfUserProperties = maxPropertyLength / userPropertyBytes;
            return this;
        }

        int getMaxPossibleUserPropertiesCount() {
            return maxNumberOfUserProperties;
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
