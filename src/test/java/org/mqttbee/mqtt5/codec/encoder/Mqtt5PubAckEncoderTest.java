package org.mqttbee.mqtt5.codec.encoder;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mqttbee.mqtt5.ChannelAttributes;
import org.mqttbee.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.mqtt5.message.Mqtt5UserProperty;
import org.mqttbee.mqtt5.message.puback.Mqtt5PubAckImpl;
import org.mqttbee.mqtt5.message.puback.Mqtt5PubAckInternal;
import org.mqttbee.mqtt5.message.puback.Mqtt5PubAckReasonCode;

import static java.util.Objects.requireNonNull;
import static org.junit.Assert.assertArrayEquals;

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
        final ImmutableList<Mqtt5UserProperty> userProperties =
                ImmutableList.of(new Mqtt5UserProperty(user, property));

        final Mqtt5PubAckImpl pubAck =
                new Mqtt5PubAckImpl(Mqtt5PubAckReasonCode.SUCCESS, reasonString, userProperties);
        final int packetIdentifier = (127 * 256) + 1;
        final Mqtt5PubAckInternal pubAckInternal =
                new Mqtt5PubAckInternal(pubAck, packetIdentifier);

        channel.writeOutbound(pubAckInternal);
        final ByteBuf byteBuf = channel.readOutbound();

        final byte[] actual = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(actual);

        assertArrayEquals(expected, actual);
    }

    @Test
    void encode_omitReasonString() {
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
        channel.attr(ChannelAttributes.OUTGOING_MAXIMUM_PACKET_SIZE).set((long) (expected.length + 2));

        final Mqtt5UTF8String reasonString = Mqtt5UTF8String.from("reason");
        final ImmutableList<Mqtt5UserProperty> userProperties =
                ImmutableList.of();

        final Mqtt5PubAckImpl pubAck =
                new Mqtt5PubAckImpl(Mqtt5PubAckReasonCode.NO_MATCHING_SUBSCRIBERS, reasonString, userProperties);
        final int packetIdentifier = 1;
        final Mqtt5PubAckInternal pubAckInternal =
                new Mqtt5PubAckInternal(pubAck, packetIdentifier);

        channel.writeOutbound(pubAckInternal);
        final ByteBuf byteBuf = channel.readOutbound();

        final byte[] actual = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(actual);

        assertArrayEquals(expected, actual);
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

        final ImmutableList<Mqtt5UserProperty> userProperties =
                ImmutableList.of();

        final Mqtt5PubAckImpl pubAck =
                new Mqtt5PubAckImpl(Mqtt5PubAckReasonCode.SUCCESS, null, userProperties);
        final int packetIdentifier = 1;
        final Mqtt5PubAckInternal pubAckInternal =
                new Mqtt5PubAckInternal(pubAck, packetIdentifier);

        channel.writeOutbound(pubAckInternal);
        final ByteBuf byteBuf = channel.readOutbound();

        final byte[] actual = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(actual);

        assertArrayEquals(expected, actual);
    }

    @Test
    void encode_omitUserProperty() {
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
        channel.attr(ChannelAttributes.OUTGOING_MAXIMUM_PACKET_SIZE).set((long) (expected.length + 2));

        final Mqtt5UTF8String user = requireNonNull(Mqtt5UTF8String.from("user"));
        final Mqtt5UTF8String property = requireNonNull(Mqtt5UTF8String.from("property"));
        final Mqtt5UTF8String reasonString = Mqtt5UTF8String.from("reason");
        final ImmutableList<Mqtt5UserProperty> userProperties =
                ImmutableList.of(new Mqtt5UserProperty(user, property));

        final Mqtt5PubAckImpl pubAck =
                new Mqtt5PubAckImpl(Mqtt5PubAckReasonCode.NO_MATCHING_SUBSCRIBERS, reasonString, userProperties);
        final int packetIdentifier = 1;
        final Mqtt5PubAckInternal pubAckInternal =
                new Mqtt5PubAckInternal(pubAck, packetIdentifier);

        channel.writeOutbound(pubAckInternal);
        final ByteBuf byteBuf = channel.readOutbound();

        final byte[] actual = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(actual);

        assertArrayEquals(expected, actual);
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
        final ImmutableList<Mqtt5UserProperty> userProperties =
                ImmutableList.of(new Mqtt5UserProperty(user, property));

        final Mqtt5PubAckImpl pubAck =
                new Mqtt5PubAckImpl(Mqtt5PubAckReasonCode.SUCCESS, reasonString, userProperties);
        final int packetIdentifier = 1;
        final Mqtt5PubAckInternal pubAckInternal =
                new Mqtt5PubAckInternal(pubAck, packetIdentifier);

        channel.writeOutbound(pubAckInternal);
        final ByteBuf byteBuf = channel.readOutbound();

        final byte[] actual = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(actual);

        assertArrayEquals(expected, actual);
    }

    @Test
    void encode_settingNullReasonCode_throwsIllegalArgument() {
        // MQTT v5.0 Spec §3.4.2.1
        final Mqtt5UTF8String user = requireNonNull(Mqtt5UTF8String.from("user"));
        final Mqtt5UTF8String property = requireNonNull(Mqtt5UTF8String.from("property"));
        final Mqtt5UTF8String reasonString = Mqtt5UTF8String.from("reason");
        final ImmutableList<Mqtt5UserProperty> userProperties =
                ImmutableList.of(new Mqtt5UserProperty(user, property));

        final Throwable exception = Assertions.assertThrows(IllegalArgumentException.class, () ->
                new Mqtt5PubAckImpl(null, reasonString, userProperties)
        );
        Assertions.assertTrue(exception.getMessage().contains("@NotNull parameter 'reasonCode'"));
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

        final ImmutableList<Mqtt5UserProperty> userProperties =
                ImmutableList.of();

        final Mqtt5PubAckImpl pubAck =
                new Mqtt5PubAckImpl(Mqtt5PubAckReasonCode.SUCCESS, null, userProperties);
        final int packetIdentifier = 1;
        final Mqtt5PubAckInternal pubAckInternal =
                new Mqtt5PubAckInternal(pubAck, packetIdentifier);

        channel.writeOutbound(pubAckInternal);
        final ByteBuf byteBuf = channel.readOutbound();

        final byte[] actual = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(actual);

        assertArrayEquals(expected, actual);
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
        final ImmutableList<Mqtt5UserProperty> userProperties =
                ImmutableList.of(new Mqtt5UserProperty(user, property));

        final Mqtt5PubAckImpl pubAck =
                new Mqtt5PubAckImpl(reasonCode, reasonString, userProperties);
        final int packetIdentifier = 1;
        final Mqtt5PubAckInternal pubAckInternal =
                new Mqtt5PubAckInternal(pubAck, packetIdentifier);

        channel.writeOutbound(pubAckInternal);
        final ByteBuf byteBuf = channel.readOutbound();

        final byte[] actual = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(actual);

        assertArrayEquals(expected, actual);
    }

}


