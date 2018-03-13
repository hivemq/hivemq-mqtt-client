package org.mqttbee.mqtt.codec.encoder.mqtt5;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mqttbee.api.mqtt.mqtt5.message.publish.puback.Mqtt5PubAckReasonCode;
import org.mqttbee.mqtt.codec.encoder.AbstractMqtt5EncoderTest;
import org.mqttbee.mqtt.datatypes.MqttUTF8StringImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertyImpl;
import org.mqttbee.mqtt.datatypes.MqttVariableByteInteger;
import org.mqttbee.mqtt.message.publish.puback.MqttPubAck;

import java.util.Arrays;

import static java.util.Objects.requireNonNull;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;

/**
 * @author Christian Hoff
 * @author David Katz
 */
class Mqtt5PubAckEncoderTest extends AbstractMqtt5EncoderTest {

    Mqtt5PubAckEncoderTest() {
        super(true);
    }

    @Test
    void encode_simple() {
        // MQTT v5.0 Spec §3.4
        final byte[] expected = {
                // fixed header
                //   type, reserved
                (byte) 0b0100_0000,
                //   remaining length
                30,
                // variable header
                //   packet identifier
                127, 1,
                //   PUBACK reason code
                0x00,
                //   properties length
                26,
                //   properties
                //     reason string
                0x1F, 0, 6, 'r', 'e', 'a', 's', 'o', 'n',
                //     user property
                0x26, 0, 4, 'u', 's', 'e', 'r', 0, 8, 'p', 'r', 'o', 'p', 'e', 'r', 't', 'y'
        };

        final MqttUTF8StringImpl user = requireNonNull(MqttUTF8StringImpl.from("user"));
        final MqttUTF8StringImpl property = requireNonNull(MqttUTF8StringImpl.from("property"));
        final MqttUTF8StringImpl reasonString = MqttUTF8StringImpl.from("reason");
        final MqttUserPropertiesImpl userProperties =
                MqttUserPropertiesImpl.of(ImmutableList.of(new MqttUserPropertyImpl(user, property)));

        final MqttPubAck pubAck =
                new MqttPubAck((127 * 256) + 1, Mqtt5PubAckReasonCode.SUCCESS, reasonString, userProperties,
                        Mqtt5PubAckEncoder.PROVIDER);
        encode(expected, pubAck);
    }

    @Test
    void encode_omitReasonCodeSuccess() {
        // MQTT v5.0 Spec §3.4.2.1
        final byte[] expected = {
                // fixed header
                //   type, reserved
                (byte) 0b0100_0000,
                //   remaining length
                2,
                // variable header
                //   packet identifier
                0, 1
        };

        final MqttUserPropertiesImpl userProperties = MqttUserPropertiesImpl.NO_USER_PROPERTIES;

        final MqttPubAck pubAck =
                new MqttPubAck(1, Mqtt5PubAckReasonCode.SUCCESS, null, userProperties, Mqtt5PubAckEncoder.PROVIDER);
        encode(expected, pubAck);
    }

    @ParameterizedTest
    @EnumSource(value = Mqtt5PubAckReasonCode.class, mode = EXCLUDE, names = {"SUCCESS"})
    void encode_doNotOmitNonSuccessReasonCodes(final Mqtt5PubAckReasonCode reasonCode) {
        // MQTT v5.0 Spec §3.4.2.1
        final byte[] expected = {
                // fixed header
                //   type, reserved
                (byte) 0b0100_0000,
                //   remaining length
                3,
                // variable header
                //   packet identifier
                0, 1,
                //   PUBACK reason code
                (byte) reasonCode.getCode()
        };

        final MqttUserPropertiesImpl userProperties = MqttUserPropertiesImpl.NO_USER_PROPERTIES;

        final MqttPubAck pubAck = new MqttPubAck(1, reasonCode, null, userProperties, Mqtt5PubAckEncoder.PROVIDER);
        encode(expected, pubAck);
    }

    @Test
    void encode_omitReasonStringIfMaxSizeTooSmall() {
        // MQTT v5.0 Spec §3.4.2.2.2
        final byte[] expected = {
                // fixed header
                //   type, reserved
                (byte) 0b0100_0000,
                //   remaining length
                3,
                // variable header
                //   packet identifier
                0, 1,
                //   PUBACK reason code
                0x10
        };
        createServerConnectionData(expected.length + 2);

        final MqttUTF8StringImpl reasonString = MqttUTF8StringImpl.from("reason");
        final MqttUserPropertiesImpl userProperties = MqttUserPropertiesImpl.NO_USER_PROPERTIES;

        final MqttPubAck pubAck =
                new MqttPubAck(1, Mqtt5PubAckReasonCode.NO_MATCHING_SUBSCRIBERS, reasonString, userProperties,
                        Mqtt5PubAckEncoder.PROVIDER);
        encode(expected, pubAck);
    }

    @Test
    void encode_omitUserPropertyIfMaxAllowedSizeTooSmall() {
        // MQTT v5.0 Spec §3.4.2.2.3
        final byte[] expected = {
                // fixed header
                //   type, reserved
                (byte) 0b0100_0000,
                //   remaining length
                3,
                // variable header
                //   packet identifier
                0, 1,
                //   PUBACK reason code
                0x10
        };
        createServerConnectionData(expected.length + 2);

        final MqttUTF8StringImpl user = requireNonNull(MqttUTF8StringImpl.from("user"));
        final MqttUTF8StringImpl property = requireNonNull(MqttUTF8StringImpl.from("property"));
        final MqttUserPropertiesImpl userProperties =
                MqttUserPropertiesImpl.of(ImmutableList.of(new MqttUserPropertyImpl(user, property)));

        final MqttPubAck pubAck = new MqttPubAck(1, Mqtt5PubAckReasonCode.NO_MATCHING_SUBSCRIBERS, null, userProperties,
                Mqtt5PubAckEncoder.PROVIDER);
        encode(expected, pubAck);
    }

    @Test
    void encode_omitReasonStringAndUserPropertyIfMaxAllowedSizeTooSmall() {
        // MQTT v5.0 Spec §3.4.2.2.3
        final byte[] expected = {
                // fixed header
                //   type, reserved
                (byte) 0b0100_0000,
                //   remaining length
                3,
                // variable header
                //   packet identifier
                0, 1,
                //   PUBACK reason code
                0x10
        };
        createServerConnectionData(expected.length + 2);

        final MqttUTF8StringImpl user = requireNonNull(MqttUTF8StringImpl.from("user"));
        final MqttUTF8StringImpl property = requireNonNull(MqttUTF8StringImpl.from("property"));
        final MqttUTF8StringImpl reasonString = MqttUTF8StringImpl.from("reason");
        final MqttUserPropertiesImpl userProperties =
                MqttUserPropertiesImpl.of(ImmutableList.of(new MqttUserPropertyImpl(user, property)));

        final MqttPubAck pubAck =
                new MqttPubAck(1, Mqtt5PubAckReasonCode.NO_MATCHING_SUBSCRIBERS, reasonString, userProperties,
                        Mqtt5PubAckEncoder.PROVIDER);
        encode(expected, pubAck);
    }

    @Test
    void encode_maximumPacketSizeExceededOnSuccess_omitUserProperties() {
        final byte[] expected = {
                // fixed header
                //   type, reserved
                (byte) 0b0100_0000,
                //   remaining length
                2,
                // variable header
                //   packet identifier
                0, 1
        };
        final MaximumPacketBuilder maxPacket = new MaximumPacketBuilder().build();

        final MqttPubAck pubAck =
                new MqttPubAck(1, Mqtt5PubAckReasonCode.SUCCESS, null,
                        maxPacket.getTooManyUserProperties(1), Mqtt5PubAckEncoder.PROVIDER);
        encode(expected, pubAck);
    }

    @Test
    void encode_multipleUserProperties() {
        // MQTT v5.0 Spec §3.4.2.2
        final byte[] expected = {
                // fixed header
                //   type, reserved
                (byte) 0b0100_0000,
                //   remaining length
                30,
                // variable header
                //   packet identifier
                0, 1,
                //   PUBACK reason code
                0x00,
                //   property length
                26,
                //   properties
                //     reason string
                0x1F, 0, 6, 'r', 'e', 'a', 's', 'o', 'n',
                //     user property
                0x26, 0, 4, 'u', 's', 'e', 'r', 0, 8, 'p', 'r', 'o', 'p', 'e', 'r', 't', 'y'
        };

        final MqttUTF8StringImpl user = requireNonNull(MqttUTF8StringImpl.from("user"));
        final MqttUTF8StringImpl property = requireNonNull(MqttUTF8StringImpl.from("property"));
        final MqttUTF8StringImpl reasonString = MqttUTF8StringImpl.from("reason");
        final MqttUserPropertiesImpl userProperties =
                MqttUserPropertiesImpl.of(ImmutableList.of(new MqttUserPropertyImpl(user, property)));

        final MqttPubAck pubAck = new MqttPubAck(1, Mqtt5PubAckReasonCode.SUCCESS, reasonString, userProperties,
                Mqtt5PubAckEncoder.PROVIDER);
        encode(expected, pubAck);
    }

    @Test
    void encode_maximumPacketSize() {
        // MQTT v5.0 Spec §3.4.1
        final ByteBuf expected = Unpooled.buffer(MqttVariableByteInteger.MAXIMUM_PACKET_SIZE_LIMIT);
        expected.writeBytes(new byte[]{
                // fixed header
                //   type, reserved
                (byte) 0b0100_0000,
                //   remaining length
                (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0x7F,
                // variable header
                //   packet identifier
                0, 1,
                //   PUBACK reason code
                0x00
        });

        final int maxPropertyLength = MqttVariableByteInteger.MAXIMUM_PACKET_SIZE_LIMIT - 1 // type, reserved
                - 4 // remaining length
                - 2 // packet identifier
                - 1 // reason code
                - 4; // property length

        // property length
        MqttVariableByteInteger.encode(maxPropertyLength, expected);

        final int remainingBytes = maxPropertyLength - 3; // reason string identifier and length
        final MqttUTF8StringImpl user = requireNonNull(MqttUTF8StringImpl.from("user"));
        final MqttUTF8StringImpl property = requireNonNull(MqttUTF8StringImpl.from("property"));
        final int userPropertyBytes = 1 // identifier
                + 2 // key length
                + 4 // bytes to encode "user"
                + 2 // value length
                + 8; // bytes to encode "property"
        final int reasonStringBytes = remainingBytes % userPropertyBytes;
        // reason string
        expected.writeByte(0x1F);
        expected.writeByte(0);
        expected.writeByte(reasonStringBytes);

        final StringBuilder reasonStringBuilder = new StringBuilder();
        for (int i = 0; i < reasonStringBytes; i++) {
            reasonStringBuilder.append(i);
            expected.writeByte(Character.forDigit(i, 10));
        }

        final int numberOfUserProperties = remainingBytes / userPropertyBytes;
        final ImmutableList.Builder<MqttUserPropertyImpl> userPropertiesBuilder = new ImmutableList.Builder<>();
        final MqttUserPropertyImpl userProperty = new MqttUserPropertyImpl(user, property);
        for (int i = 0; i < numberOfUserProperties; i++) {
            userPropertiesBuilder.add(userProperty);
            expected.writeBytes(new byte[]{
                    0x26, 0, 4, 'u', 's', 'e', 'r', 0, 8, 'p', 'r', 'o', 'p', 'e', 'r', 't', 'y'
            });
        }

        final MqttUTF8StringImpl reasonString = MqttUTF8StringImpl.from(reasonStringBuilder.toString());
        final MqttPubAck pubAck = new MqttPubAck(1, Mqtt5PubAckReasonCode.SUCCESS, reasonString,
                MqttUserPropertiesImpl.of(userPropertiesBuilder.build()), Mqtt5PubAckEncoder.PROVIDER);

        final byte[] expectedBytes = new byte[expected.readableBytes()];
        expected.readBytes(expectedBytes);
        encode(expectedBytes, pubAck);
        expected.release();
    }

    @Test
    void encode_maximumPacketSizeExceeded_omitUserProperties() {
        final byte[] expected = {
                // fixed header
                //   type, reserved
                (byte) 0b0100_0000,
                //   remaining length
                2,
                // variable header
                //   packet identifier
                0, 1
        };

        final MaximumPacketBuilder maxPacket = new MaximumPacketBuilder().build();

        final MqttPubAck pubAck =
                new MqttPubAck(1, Mqtt5PubAckReasonCode.SUCCESS, null, maxPacket.getTooManyUserProperties(1),
                        Mqtt5PubAckEncoder.PROVIDER);


        encode(expected, pubAck);
    }

    @Test
    void encode_maximumPacketSizeExceeded_omitReasonString() {
        final MaximumPacketBuilder maxPacket = new MaximumPacketBuilder().build();
        final int userPropertyCount = maxPacket.getMaxUserPropertiesCount();
        final MqttUserPropertiesImpl maxUserProperties = getUserProperties(userPropertyCount);

        final ByteBuf expected = Unpooled.buffer(5 + 268435447, 5 + 268435447);

        // fixed header
        // type, reserved
        expected.writeByte(0b0100_0000);
        // remaining length (2 + 1 + 4 + (userPropertyBytes * maxPossibleUserPropertiesCount) = 268435447
        expected.writeByte(0xf7);
        expected.writeByte(0xff);
        expected.writeByte(0xff);
        expected.writeByte(0x7f);
        // packet identifier
        expected.writeByte(0);
        expected.writeByte(1);
        // reason code
        expected.writeByte(0x00);
        // properties length = 268435440
        expected.writeByte(0xf0);
        expected.writeByte(0xff);
        expected.writeByte(0xff);
        expected.writeByte(0x7f);
        // user properties
        maxUserProperties.encode(expected);

        final MqttPubAck pubAck =
                new MqttPubAck(1, Mqtt5PubAckReasonCode.SUCCESS, maxPacket.getReasonStringTooLong("a"),
                        maxPacket.getMaxPossibleUserProperties(), Mqtt5PubAckEncoder.PROVIDER);

        encode(expected.array(), pubAck);
        expected.release();
    }

    @Test
    void encode_omitReasonCodeAndPropertyLength() {
        // MQTT v5.0 Spec §3.4.2.1
        final byte[] expected = {
                // fixed header
                //   type, reserved
                (byte) 0b0100_0000,
                //   remaining length
                2,
                // variable header
                //   packet identifier
                0, 1
        };

        final MqttUserPropertiesImpl userProperties = MqttUserPropertiesImpl.NO_USER_PROPERTIES;

        final MqttPubAck pubAck =
                new MqttPubAck(1, Mqtt5PubAckReasonCode.SUCCESS, null, userProperties, Mqtt5PubAckEncoder.PROVIDER);
        encode(expected, pubAck);
    }

    @ParameterizedTest
    @EnumSource(Mqtt5PubAckReasonCode.class)
    void encode_reasonCodes(final Mqtt5PubAckReasonCode reasonCode) {
        // MQTT v5.0 Spec §3.4.2.1
        final byte[] expected = {
                // fixed header
                //   type, reserved
                (byte) 0b0100_0000,
                //   remaining length
                30,
                // variable header
                //   packet identifier
                0, 1,
                //   PUBACK reason code
                (byte) reasonCode.getCode(),
                //   properties length
                26,
                //   properties
                //     reason string
                0x1F, 0, 6, 'r', 'e', 'a', 's', 'o', 'n',
                //     user property
                0x26, 0, 4, 'u', 's', 'e', 'r', 0, 8, 'p', 'r', 'o', 'p', 'e', 'r', 't', 'y'
        };

        final MqttUTF8StringImpl user = requireNonNull(MqttUTF8StringImpl.from("user"));
        final MqttUTF8StringImpl property = requireNonNull(MqttUTF8StringImpl.from("property"));
        final MqttUTF8StringImpl reasonString = MqttUTF8StringImpl.from("reason");
        final MqttUserPropertiesImpl userProperties =
                MqttUserPropertiesImpl.of(ImmutableList.of(new MqttUserPropertyImpl(user, property)));

        final MqttPubAck pubAck =
                new MqttPubAck(1, reasonCode, reasonString, userProperties, Mqtt5PubAckEncoder.PROVIDER);
        encode(expected, pubAck);
    }

    private void encode(final byte[] expected, final MqttPubAck pubAck) {
        channel.writeOutbound(pubAck);
        final ByteBuf byteBuf = channel.readOutbound();

        final byte[] actual = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(actual);
        byteBuf.release();

        assertArrayEquals(expected, actual);
    }

    private class MaximumPacketBuilder {
        private ImmutableList.Builder<MqttUserPropertyImpl> userPropertiesBuilder;
        final MqttUTF8StringImpl user = requireNonNull(MqttUTF8StringImpl.from("user"));
        final MqttUTF8StringImpl property = requireNonNull(MqttUTF8StringImpl.from("property"));
        int maxUserPropertyCount;
        char[] reasonStringBytes;
        final int userPropertyByteCount = 1 // identifier
                + 2  // key length
                + 4  // bytes to encode "user"
                + 2  // value length
                + 8; // bytes to encode "property"

        MaximumPacketBuilder build() {
            // MQTT v5.0 Spec §3.4.1
            final int maxPropertyLength = MqttVariableByteInteger.MAXIMUM_PACKET_SIZE_LIMIT - 1 // type, reserved
                    - 4  // remaining length
                    - 2  // packet identifier
                    - 1  // reason code
                    - 4  // property length
                    - 3; // minimum reason string


            final int reasonStringLength = maxPropertyLength % userPropertyByteCount;

            reasonStringBytes = new char[reasonStringLength];
            Arrays.fill(reasonStringBytes, 'r');

            maxUserPropertyCount = maxPropertyLength / userPropertyByteCount;
            userPropertiesBuilder = new ImmutableList.Builder<>();
            final MqttUserPropertyImpl userProperty = new MqttUserPropertyImpl(user, property);
            for (int i = 0; i < maxUserPropertyCount; i++) {
                userPropertiesBuilder.add(userProperty);
            }

            return this;
        }

        MqttUTF8StringImpl getReasonStringTooLong() {
            return getReasonStringTooLong("");
        }

        MqttUTF8StringImpl getReasonStringTooLong(final String withSuffix) {
            return MqttUTF8StringImpl.from(new String(reasonStringBytes) + withSuffix);
        }

        MqttUserPropertiesImpl getMaxPossibleUserProperties() {
            return getTooManyUserProperties(0);
        }

        MqttUserPropertiesImpl getTooManyUserProperties(final int withExtraUserProperties) {
            for (int i = 0; i < withExtraUserProperties; i++) {
                userPropertiesBuilder.add(new MqttUserPropertyImpl(user, property));
            }
            return MqttUserPropertiesImpl.of(userPropertiesBuilder.build());
        }

        int getMaxUserPropertiesCount() {
            return maxUserPropertyCount;
        }
    }

    private MqttUserPropertiesImpl getUserProperties(final int totalCount) {
        final MqttUserPropertyImpl userProperty =
                new MqttUserPropertyImpl(requireNonNull(MqttUTF8StringImpl.from("user")),
                        requireNonNull(MqttUTF8StringImpl.from("property")));
        final ImmutableList.Builder<MqttUserPropertyImpl> builder = new ImmutableList.Builder<>();
        for (int i = 0; i < totalCount; i++) {
            builder.add(userProperty);
        }
        return MqttUserPropertiesImpl.of(builder.build());
    }
}


