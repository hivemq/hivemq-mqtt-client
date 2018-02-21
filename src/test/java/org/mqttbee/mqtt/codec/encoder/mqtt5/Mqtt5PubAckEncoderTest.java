package org.mqttbee.mqtt.codec.encoder.mqtt5;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.EncoderException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mqttbee.api.mqtt5.message.publish.puback.Mqtt5PubAckReasonCode;
import org.mqttbee.mqtt.codec.encoder.AbstractMqtt5EncoderTest;
import org.mqttbee.mqtt.datatypes.MqttUTF8StringImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertyImpl;
import org.mqttbee.mqtt.datatypes.MqttVariableByteInteger;
import org.mqttbee.mqtt.message.publish.puback.MqttPubAckImpl;

import static java.util.Objects.requireNonNull;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;

/**
 * @author Christian Hoff
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

        final MqttPubAckImpl pubAck =
                new MqttPubAckImpl((127 * 256) + 1, Mqtt5PubAckReasonCode.SUCCESS, reasonString, userProperties,
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

        final MqttPubAckImpl pubAck =
                new MqttPubAckImpl(1, Mqtt5PubAckReasonCode.SUCCESS, null, userProperties, Mqtt5PubAckEncoder.PROVIDER);
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

        final MqttPubAckImpl pubAck =
                new MqttPubAckImpl(1, reasonCode, null, userProperties, Mqtt5PubAckEncoder.PROVIDER);
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

        final MqttPubAckImpl pubAck =
                new MqttPubAckImpl(1, Mqtt5PubAckReasonCode.NO_MATCHING_SUBSCRIBERS, reasonString, userProperties,
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

        final MqttPubAckImpl pubAck =
                new MqttPubAckImpl(1, Mqtt5PubAckReasonCode.NO_MATCHING_SUBSCRIBERS, null, userProperties,
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

        final MqttPubAckImpl pubAck =
                new MqttPubAckImpl(1, Mqtt5PubAckReasonCode.NO_MATCHING_SUBSCRIBERS, reasonString, userProperties,
                        Mqtt5PubAckEncoder.PROVIDER);
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

        final MqttPubAckImpl pubAck = new MqttPubAckImpl(1, Mqtt5PubAckReasonCode.SUCCESS, reasonString, userProperties,
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
        final MqttPubAckImpl pubAck = new MqttPubAckImpl(1, Mqtt5PubAckReasonCode.SUCCESS, reasonString,
                MqttUserPropertiesImpl.of(userPropertiesBuilder.build()), Mqtt5PubAckEncoder.PROVIDER);

        final byte[] expectedBytes = new byte[expected.readableBytes()];
        expected.readBytes(expectedBytes);

        encode(expectedBytes, pubAck);

        expected.release();
    }

    @Test
    @Disabled("transform to encode_maximumPacketSizeExceeded_omitUserPropertiesAndReasonString")
    void encode_maximumPacketSizeExceeded_throwsEncoderException() {

        final MaximumPacketBuilder maxPacket = new MaximumPacketBuilder().build();

        final MqttPubAckImpl pubAck =
                new MqttPubAckImpl(1, Mqtt5PubAckReasonCode.SUCCESS, maxPacket.getMaxPaddedReasonString("a"),
                        maxPacket.getMaxPossibleUserProperties(), Mqtt5PubAckEncoder.PROVIDER);

        final Throwable exception = assertThrows(EncoderException.class, () -> channel.writeOutbound(pubAck));
        assertTrue(exception.getMessage().contains("variable byte integer size exceeded for remaining length"));
    }

    @Test
    @Disabled("transform to encode_propertyLengthExceeded_omitUserPropertiesAndReasonString")
    void encode_maximumPropertiesSizeExceeded_throwsEncoderException() {

        final MaximumPacketBuilder maxPacket = new MaximumPacketBuilder().build();

        final MqttPubAckImpl pubAck =
                new MqttPubAckImpl(1, Mqtt5PubAckReasonCode.SUCCESS, maxPacket.getMaxPaddedReasonString(),
                        maxPacket.getMaxPossibleUserProperties(1), Mqtt5PubAckEncoder.PROVIDER);

        final Throwable exception = assertThrows(EncoderException.class, () -> channel.writeOutbound(pubAck));
        assertTrue(exception.getMessage().contains("variable byte integer size exceeded for property length"));
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

        final MqttPubAckImpl pubAck =
                new MqttPubAckImpl(1, Mqtt5PubAckReasonCode.SUCCESS, null, userProperties, Mqtt5PubAckEncoder.PROVIDER);
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

        final MqttPubAckImpl pubAck =
                new MqttPubAckImpl(1, reasonCode, reasonString, userProperties, Mqtt5PubAckEncoder.PROVIDER);
        encode(expected, pubAck);
    }

    private void encode(final byte[] expected, final MqttPubAckImpl pubAck) {
        channel.writeOutbound(pubAck);
        final ByteBuf byteBuf = channel.readOutbound();

        final byte[] actual = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(actual);
        byteBuf.release();

        assertArrayEquals(expected, actual);
    }

    private class MaximumPacketBuilder {
        private StringBuilder reasonStringBuilder;
        private ImmutableList.Builder<MqttUserPropertyImpl> userPropertiesBuilder;
        final MqttUTF8StringImpl user = requireNonNull(MqttUTF8StringImpl.from("user"));
        final MqttUTF8StringImpl property = requireNonNull(MqttUTF8StringImpl.from("property"));

        MaximumPacketBuilder build() {
            // MQTT v5.0 Spec §3.4.1
            final int maxPropertyLength = MqttVariableByteInteger.MAXIMUM_PACKET_SIZE_LIMIT - 1 // type, reserved
                    - 4 // remaining length
                    - 2 // packet identifier
                    - 1 // reason code
                    - 4; // property length

            final int remainingBytes = maxPropertyLength - 3; // reason string identifier and length
            final int userPropertyBytes = 1 // identifier
                    + 2 // key length
                    + 4 // bytes to encode "user"
                    + 2 // value length
                    + 8; // bytes to encode "property"
            final int reasonStringBytes = remainingBytes % userPropertyBytes;

            reasonStringBuilder = new StringBuilder();
            for (int i = 0; i < reasonStringBytes; i++) {
                reasonStringBuilder.append(i);
            }

            final int numberOfUserProperties = remainingBytes / userPropertyBytes;
            userPropertiesBuilder = new ImmutableList.Builder<>();
            final MqttUserPropertyImpl userProperty = new MqttUserPropertyImpl(user, property);
            for (int i = 0; i < numberOfUserProperties; i++) {
                userPropertiesBuilder.add(userProperty);
            }

            return this;
        }

        MqttUTF8StringImpl getMaxPaddedReasonString() {
            return getMaxPaddedReasonString("");
        }

        MqttUTF8StringImpl getMaxPaddedReasonString(final String withSuffix) {
            return MqttUTF8StringImpl.from(reasonStringBuilder.toString() + withSuffix);
        }

        MqttUserPropertiesImpl getMaxPossibleUserProperties() {
            return getMaxPossibleUserProperties(0);
        }

        MqttUserPropertiesImpl getMaxPossibleUserProperties(final int withExtraUserProperties) {
            for (int i = 0; i < withExtraUserProperties; i++) {
                userPropertiesBuilder.add(new MqttUserPropertyImpl(user, property));
            }
            return MqttUserPropertiesImpl.of(userPropertiesBuilder.build());
        }
    }
}


