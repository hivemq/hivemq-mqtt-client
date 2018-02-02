package org.mqttbee.mqtt5.codec.encoder;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.EncoderException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mqttbee.mqtt5.ChannelAttributes;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;
import org.mqttbee.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.mqtt5.message.Mqtt5UserProperties;
import org.mqttbee.mqtt5.message.Mqtt5UserProperty;
import org.mqttbee.mqtt5.message.puback.Mqtt5PubAckImpl;
import org.mqttbee.mqtt5.message.puback.Mqtt5PubAckInternal;
import org.mqttbee.mqtt5.message.puback.Mqtt5PubAckReasonCode;

import static java.util.Objects.requireNonNull;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;
import static org.mqttbee.mqtt5.codec.Mqtt5DataTypes.encodeVariableByteInteger;

/**
 * @author Christian Hoff
 */
class Mqtt5PubAckEncoderTest {

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

        final Mqtt5UTF8String user = requireNonNull(Mqtt5UTF8String.from("user"));
        final Mqtt5UTF8String property = requireNonNull(Mqtt5UTF8String.from("property"));
        final Mqtt5UTF8String reasonString = Mqtt5UTF8String.from("reason");
        final Mqtt5UserProperties userProperties =
                Mqtt5UserProperties.of(ImmutableList.of(new Mqtt5UserProperty(user, property)));

        final Mqtt5PubAckImpl pubAck = new Mqtt5PubAckImpl(Mqtt5PubAckReasonCode.SUCCESS, reasonString, userProperties);
        final int packetIdentifier = (127 * 256) + 1;
        encode(expected, pubAck, packetIdentifier);
    }

    @Test
    @Disabled("logic to omit reason string missing from encoder. Ticket has been added")
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
        channel.attr(ChannelAttributes.OUTGOING_MAXIMUM_PACKET_SIZE).set((expected.length + 2));

        final Mqtt5UTF8String reasonString = Mqtt5UTF8String.from("reason");
        final Mqtt5UserProperties userProperties = Mqtt5UserProperties.DEFAULT_NO_USER_PROPERTIES;

        final Mqtt5PubAckImpl pubAck =
                new Mqtt5PubAckImpl(Mqtt5PubAckReasonCode.NO_MATCHING_SUBSCRIBERS, reasonString, userProperties);
        final int packetIdentifier = 1;
        encode(expected, pubAck, packetIdentifier);
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

        final Mqtt5UserProperties userProperties = Mqtt5UserProperties.DEFAULT_NO_USER_PROPERTIES;

        final Mqtt5PubAckImpl pubAck = new Mqtt5PubAckImpl(Mqtt5PubAckReasonCode.SUCCESS, null, userProperties);
        final int packetIdentifier = 1;
        encode(expected, pubAck, packetIdentifier);
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

        final Mqtt5UserProperties userProperties = Mqtt5UserProperties.DEFAULT_NO_USER_PROPERTIES;

        final Mqtt5PubAckImpl pubAck = new Mqtt5PubAckImpl(reasonCode, null, userProperties);
        final int packetIdentifier = 1;
        encode(expected, pubAck, packetIdentifier);
    }

    @Test
    @Disabled("logic to omit user properties missing from encoder. Ticket has been added")
    void encode_omitUserPropertyIfMaxAllowedSizeTooSmall() {
        // MQTT v5.0 Spec §3.4.2.2.3
        final byte[] expected = {
                // fixed header
                //   type, reserved
                (byte) 0b0100_0000,
                //   remaining length
                13,
                // variable header
                //   packet identifier
                0, 1,
                //   PUBACK reason code
                0x10,
                //   properties length
                9,
                //   properties
                //     reason string
                0x1F, 0, 6, 'r', 'e', 'a', 's', 'o', 'n',
        };
        channel.attr(ChannelAttributes.OUTGOING_MAXIMUM_PACKET_SIZE).set((expected.length + 2));

        final Mqtt5UTF8String user = requireNonNull(Mqtt5UTF8String.from("user"));
        final Mqtt5UTF8String property = requireNonNull(Mqtt5UTF8String.from("property"));
        final Mqtt5UTF8String reasonString = Mqtt5UTF8String.from("reason");
        final Mqtt5UserProperties userProperties =
                Mqtt5UserProperties.of(ImmutableList.of(new Mqtt5UserProperty(user, property)));

        final Mqtt5PubAckImpl pubAck =
                new Mqtt5PubAckImpl(Mqtt5PubAckReasonCode.NO_MATCHING_SUBSCRIBERS, reasonString, userProperties);
        final int packetIdentifier = 1;
        encode(expected, pubAck, packetIdentifier);
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

        final Mqtt5UTF8String user = requireNonNull(Mqtt5UTF8String.from("user"));
        final Mqtt5UTF8String property = requireNonNull(Mqtt5UTF8String.from("property"));
        final Mqtt5UTF8String reasonString = Mqtt5UTF8String.from("reason");
        final Mqtt5UserProperties userProperties =
                Mqtt5UserProperties.of(ImmutableList.of(new Mqtt5UserProperty(user, property)));

        final Mqtt5PubAckImpl pubAck = new Mqtt5PubAckImpl(Mqtt5PubAckReasonCode.SUCCESS, reasonString, userProperties);
        final int packetIdentifier = 1;
        encode(expected, pubAck, packetIdentifier);
    }

    private void encode(final byte[] expected, final Mqtt5PubAckImpl pubAck, final int packetIdentifier) {
        final Mqtt5PubAckInternal pubAckInternal = new Mqtt5PubAckInternal(pubAck, packetIdentifier);

        channel.writeOutbound(pubAckInternal);
        final ByteBuf byteBuf = channel.readOutbound();

        final byte[] actual = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(actual);

        assertArrayEquals(expected, actual);
    }

    @Test
    void encode_maximumPacketSize() {
        // MQTT v5.0 Spec §3.4.1
        final ByteBuf expected = Unpooled.buffer(Mqtt5DataTypes.MAXIMUM_PACKET_SIZE_LIMIT);
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

        final int maxPropertyLength = Mqtt5DataTypes.MAXIMUM_PACKET_SIZE_LIMIT - 1 // type, reserved
                - 4 // remaining length
                - 2 // packet identifier
                - 1 // reason code
                - 4; // property length

        // property length
        encodeVariableByteInteger(maxPropertyLength, expected);

        final int remainingBytes = maxPropertyLength - 3; // reason string identifier and length
        final Mqtt5UTF8String user = requireNonNull(Mqtt5UTF8String.from("user"));
        final Mqtt5UTF8String property = requireNonNull(Mqtt5UTF8String.from("property"));
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
        final ImmutableList.Builder<Mqtt5UserProperty> userPropertiesBuilder = new ImmutableList.Builder<>();
        final Mqtt5UserProperty userProperty = new Mqtt5UserProperty(user, property);
        for (int i = 0; i < numberOfUserProperties; i++) {
            userPropertiesBuilder.add(userProperty);
            expected.writeBytes(new byte[]{
                    0x26, 0, 4, 'u', 's', 'e', 'r', 0, 8, 'p', 'r', 'o', 'p', 'e', 'r', 't', 'y'
            });
        }

        final Mqtt5UTF8String reasonString = Mqtt5UTF8String.from(reasonStringBuilder.toString());
        final Mqtt5PubAckImpl pubAck = new Mqtt5PubAckImpl(Mqtt5PubAckReasonCode.SUCCESS, reasonString,
                Mqtt5UserProperties.of(userPropertiesBuilder.build()));
        final int packetIdentifier = 1;
        final Mqtt5PubAckInternal pubAckInternal = new Mqtt5PubAckInternal(pubAck, packetIdentifier);

        channel.writeOutbound(pubAckInternal);
        final ByteBuf byteBuf = channel.readOutbound();

        final byte[] actual = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(actual);

        final byte[] expectedBytes = new byte[expected.readableBytes()];
        expected.readBytes(expectedBytes);
        assertArrayEquals(expectedBytes, actual);
    }

    @Test
    void encode_maximumPacketSizeExceeded_throwsEncoderException() {

        final MaximumPacketBuilder maxPacket = new MaximumPacketBuilder().build();

        final Mqtt5PubAckImpl pubAck =
                new Mqtt5PubAckImpl(Mqtt5PubAckReasonCode.SUCCESS, maxPacket.getMaxPaddedReasonString("a"),
                        maxPacket.getMaxPossibleUserProperties());
        final int packetIdentifier = 1;
        final Mqtt5PubAckInternal pubAckInternal = new Mqtt5PubAckInternal(pubAck, packetIdentifier);

        final Throwable exception = assertThrows(EncoderException.class, () -> channel.writeOutbound(pubAckInternal));
        assertTrue(exception.getMessage().contains("variable byte integer size exceeded for remaining length"));
    }

    @Test
    void encode_maximumPropertiesSizeExceeded_throwsEncoderException() {

        final MaximumPacketBuilder maxPacket = new MaximumPacketBuilder().build();

        final Mqtt5PubAckImpl pubAck =
                new Mqtt5PubAckImpl(Mqtt5PubAckReasonCode.SUCCESS, maxPacket.getMaxPaddedReasonString(),
                        maxPacket.getMaxPossibleUserProperties(1));
        final int packetIdentifier = 1;
        final Mqtt5PubAckInternal pubAckInternal = new Mqtt5PubAckInternal(pubAck, packetIdentifier);

        final Throwable exception = assertThrows(EncoderException.class, () -> channel.writeOutbound(pubAckInternal));
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

        final Mqtt5UserProperties userProperties = Mqtt5UserProperties.DEFAULT_NO_USER_PROPERTIES;

        final Mqtt5PubAckImpl pubAck = new Mqtt5PubAckImpl(Mqtt5PubAckReasonCode.SUCCESS, null, userProperties);
        final int packetIdentifier = 1;
        encode(expected, pubAck, packetIdentifier);
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

        final Mqtt5UTF8String user = requireNonNull(Mqtt5UTF8String.from("user"));
        final Mqtt5UTF8String property = requireNonNull(Mqtt5UTF8String.from("property"));
        final Mqtt5UTF8String reasonString = Mqtt5UTF8String.from("reason");
        final Mqtt5UserProperties userProperties =
                Mqtt5UserProperties.of(ImmutableList.of(new Mqtt5UserProperty(user, property)));

        final Mqtt5PubAckImpl pubAck = new Mqtt5PubAckImpl(reasonCode, reasonString, userProperties);
        final int packetIdentifier = 1;
        encode(expected, pubAck, packetIdentifier);
    }

    private class MaximumPacketBuilder {
        private StringBuilder reasonStringBuilder;
        private ImmutableList.Builder<Mqtt5UserProperty> userPropertiesBuilder;
        final Mqtt5UTF8String user = requireNonNull(Mqtt5UTF8String.from("user"));
        final Mqtt5UTF8String property = requireNonNull(Mqtt5UTF8String.from("property"));

        MaximumPacketBuilder build() {
            // MQTT v5.0 Spec §3.4.1
            final int maxPropertyLength = Mqtt5DataTypes.MAXIMUM_PACKET_SIZE_LIMIT - 1 // type, reserved
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
            final Mqtt5UserProperty userProperty = new Mqtt5UserProperty(user, property);
            for (int i = 0; i < numberOfUserProperties; i++) {
                userPropertiesBuilder.add(userProperty);
            }

            return this;
        }

        Mqtt5UTF8String getMaxPaddedReasonString() {
            return getMaxPaddedReasonString("");
        }

        Mqtt5UTF8String getMaxPaddedReasonString(final String withSuffix) {
            return Mqtt5UTF8String.from(reasonStringBuilder.toString() + withSuffix);
        }

        Mqtt5UserProperties getMaxPossibleUserProperties() {
            return getMaxPossibleUserProperties(0);
        }

        Mqtt5UserProperties getMaxPossibleUserProperties(final int withExtraUserProperties) {
            for (int i = 0; i < withExtraUserProperties; i++) {
                userPropertiesBuilder.add(new Mqtt5UserProperty(user, property));
            }
            return Mqtt5UserProperties.of(userPropertiesBuilder.build());
        }
    }
}


