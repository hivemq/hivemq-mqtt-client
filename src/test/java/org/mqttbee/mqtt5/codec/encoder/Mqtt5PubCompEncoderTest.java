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
import org.mqttbee.mqtt5.message.Mqtt5UserProperty;
import org.mqttbee.mqtt5.message.pubcomp.Mqtt5PubCompImpl;
import org.mqttbee.mqtt5.message.pubcomp.Mqtt5PubCompInternal;
import org.mqttbee.mqtt5.message.pubcomp.Mqtt5PubCompReasonCode;

import static java.util.Objects.requireNonNull;
import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;
import static org.mqttbee.mqtt5.message.pubcomp.Mqtt5PubCompReasonCode.SUCCESS;

/**
 * @author David Katz
 */
class Mqtt5PubCompEncoderTest {

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
        final byte[] expected = {
                // fixed header
                //   type, flags
                0b0111_0000,
                //   remaining length
                3,
                // variable header
                //   packet identifier
                0, 5,
                // reason code
                (byte) 0x92
        };

        final Mqtt5PubCompReasonCode reasonCode = Mqtt5PubCompReasonCode.PACKET_IDENTIFIER_NOT_FOUND;
        final Mqtt5UTF8String reasonString = null;
        final ImmutableList<Mqtt5UserProperty> userProperties = ImmutableList.of();
        final Mqtt5PubCompImpl pubRec = new Mqtt5PubCompImpl(reasonCode, reasonString, userProperties);

        encode(expected, pubRec, 5);
    }

    @Test
    void encode_reasonCodeOmittedWhenSuccessWithoutProperties() {
        final byte[] expected = {
                // fixed header
                //   type, flags
                0b0111_0000,
                //   remaining length
                2,
                // variable header
                //   packet identifier
                0, 5
        };

        final Mqtt5PubCompImpl pubRec = new Mqtt5PubCompImpl(SUCCESS, null, ImmutableList.of());

        encode(expected, pubRec, 5);
    }

    @ParameterizedTest
    @EnumSource(value = Mqtt5PubCompReasonCode.class, mode = EXCLUDE, names = {"SUCCESS"})
    void encode_reasonCodes(final Mqtt5PubCompReasonCode reasonCode) {
        final byte[] expected = {
                // fixed header
                //   type, flags
                0b0111_0000,
                //   remaining length
                3,
                // variable header
                //   packet identifier
                6, 5,
                //   reason code placeholder
                (byte) 0xFF
        };

        expected[4] = (byte) reasonCode.getCode();
        final Mqtt5PubCompImpl pubRec = new Mqtt5PubCompImpl(reasonCode, null, ImmutableList.of());

        encode(expected, pubRec, 0x0605);
    }

    @Test
    void encode_reasonString() {
        final byte[] expected = {
                // fixed header
                //   type, flags
                0b0111_0000,
                //   remaining length
                13,
                // variable header
                //   packet identifier
                0, 9,
                //   reason code
                (byte) 0x92,
                //   properties
                9,
                // reason string
                0x1F, 0, 6, 'r', 'e', 'a', 's', 'o', 'n'
        };

        final Mqtt5PubCompReasonCode reasonCode = Mqtt5PubCompReasonCode.PACKET_IDENTIFIER_NOT_FOUND;
        final Mqtt5UTF8String reasonString = Mqtt5UTF8String.from("reason");
        final ImmutableList<Mqtt5UserProperty> userProperties = ImmutableList.of();
        final Mqtt5PubCompImpl pubRec = new Mqtt5PubCompImpl(reasonCode, reasonString, userProperties);

        encode(expected, pubRec, 9);
    }

    @Test
    void encode_userProperty() {
        final byte[] expected = {
                // fixed header
                //   type, flags
                0b0111_0000,
                //   remaining length
                17,
                // variable header
                //   packet identifier
                0, 5,
                //   reason code
                (byte) 0x92,
                //   properties
                13,
                // user Property
                0x26, 0, 3, 'k', 'e', 'y', 0, 5, 'v', 'a', 'l', 'u', 'e'
        };

        final Mqtt5PubCompReasonCode reasonCode = Mqtt5PubCompReasonCode.PACKET_IDENTIFIER_NOT_FOUND;
        final ImmutableList<Mqtt5UserProperty> userProperties = ImmutableList
                .of(new Mqtt5UserProperty(requireNonNull(Mqtt5UTF8String.from("key")),
                        requireNonNull(Mqtt5UTF8String.from("value"))));
        final Mqtt5PubCompImpl pubRec = new Mqtt5PubCompImpl(reasonCode, null, userProperties);

        encode(expected, pubRec, 5);
    }

    @Test
    void encode_maximumPacketSizeExceeded_throwsEncoderException() {
        final MaximumPacketBuilder maxPacket = new MaximumPacketBuilder().build();
        final Mqtt5PubCompImpl pubRec = new Mqtt5PubCompImpl(SUCCESS, maxPacket.getMaxPaddedReasonString("a"),
                maxPacket.getMaxPossibleUserProperties());

        final int packetIdentifier = 1;
        final Mqtt5PubCompInternal pubRecInternal = new Mqtt5PubCompInternal(pubRec, packetIdentifier);

        final Throwable exception = assertThrows(EncoderException.class, () -> channel.writeOutbound(pubRecInternal));
        assertTrue(exception.getMessage().contains("variable byte integer size exceeded for remaining length"));
    }

    @Test
    void encode_propertyLengthExceedsMax_throwsEncoderException() {
        final MaximumPacketBuilder maxPacket = new MaximumPacketBuilder().build();
        final Mqtt5PubCompImpl pubRec = new Mqtt5PubCompImpl(SUCCESS, maxPacket.getMaxPaddedReasonString(),
                maxPacket.getMaxPossibleUserProperties(1));

        final int packetIdentifier = 1;
        final Mqtt5PubCompInternal pubRecInternal = new Mqtt5PubCompInternal(pubRec, packetIdentifier);

        final Throwable exception = assertThrows(EncoderException.class, () -> channel.writeOutbound(pubRecInternal));
        assertTrue(exception.getMessage().contains("variable byte integer size exceeded for property length"));
    }


    private void encode(final byte[] expected, final Mqtt5PubCompImpl pubRec, final int packetIdentifier) {
        final Mqtt5PubCompInternal pubRecInternal = new Mqtt5PubCompInternal(pubRec, packetIdentifier);
        assertEquals(packetIdentifier, pubRecInternal.getPacketIdentifier());
        encodeInternal(expected, pubRecInternal);
    }

    private void encodeInternal(final byte[] expected, final Mqtt5PubCompInternal pubRecInternal) {
        channel.writeOutbound(pubRecInternal);
        final ByteBuf byteBuf = channel.readOutbound();

        final byte[] actual = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(actual);
        assertArrayEquals(expected, actual);
    }

    private class MaximumPacketBuilder {

        private StringBuilder reasonStringBuilder;
        private ImmutableList.Builder<Mqtt5UserProperty> userPropertiesBuilder;
        final Mqtt5UTF8String user = requireNonNull(Mqtt5UTF8String.from("user"));
        final Mqtt5UTF8String property = requireNonNull(Mqtt5UTF8String.from("property"));

        MaximumPacketBuilder build() {
            final int maxPropertyLength = Mqtt5DataTypes.MAXIMUM_PACKET_SIZE_LIMIT - 1  // type, reserved
                    - 4  // remaining length
                    - 4  // property length
                    - 2  // packet identifier
                    - 1; // reason code

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

        ImmutableList<Mqtt5UserProperty> getMaxPossibleUserProperties() {
            return getMaxPossibleUserProperties(0);
        }

        ImmutableList<Mqtt5UserProperty> getMaxPossibleUserProperties(final int withExtraUserProperties) {
            for (int i = 0; i < withExtraUserProperties; i++) {
                userPropertiesBuilder.add(new Mqtt5UserProperty(user, property));
            }
            return userPropertiesBuilder.build();
        }
    }
}
