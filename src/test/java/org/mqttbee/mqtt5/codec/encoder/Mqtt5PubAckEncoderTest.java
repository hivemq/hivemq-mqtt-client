package org.mqttbee.mqtt5.codec.encoder;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import io.netty.channel.embedded.EmbeddedChannel;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Suite;
import org.mqttbee.mqtt5.ChannelAttributes;
import org.mqttbee.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.mqtt5.message.Mqtt5UserProperty;
import org.mqttbee.mqtt5.message.puback.Mqtt5PubAckImpl;
import org.mqttbee.mqtt5.message.puback.Mqtt5PubAckInternal;
import org.mqttbee.mqtt5.message.puback.Mqtt5PubAckReasonCode;

import java.util.Arrays;
import java.util.Collection;

import static java.util.Objects.requireNonNull;
import static org.junit.Assert.assertArrayEquals;

/**
 * @author Christian Hoff
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({Mqtt5PubAckEncoderTest.SingleTests.class, Mqtt5PubAckEncoderTest.ReasonCodeParamTests.class})
public class Mqtt5PubAckEncoderTest {

    public static class TestBase {
        EmbeddedChannel channel;

        @Rule
        public ExpectedException thrown = ExpectedException.none();

        @Before
        public void setUp() {
            channel = new EmbeddedChannel(new Mqtt5Encoder());
        }

        @After
        public void tearDown() {
            channel.close();
        }
    }

    public static class SingleTests extends TestBase {
        @Test
        public void encode_simple() {
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
        public void encode_omitReasonString() {
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
        public void encode_omitReasonCodeSuccess() {
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
        public void encode_omitUserProperty() {
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
        public void encode_multipleUserProperties() {
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
        public void encode_settingNullReasonCode_throwsIllegalArgument() {
            // MQTT v5.0 Spec §3.4.2.1
            final Mqtt5UTF8String user = requireNonNull(Mqtt5UTF8String.from("user"));
            final Mqtt5UTF8String property = requireNonNull(Mqtt5UTF8String.from("property"));
            final Mqtt5UTF8String reasonString = Mqtt5UTF8String.from("reason");
            final ImmutableList<Mqtt5UserProperty> userProperties =
                    ImmutableList.of(new Mqtt5UserProperty(user, property));

            thrown.expect(IllegalArgumentException.class);
            thrown.expectMessage(CoreMatchers.containsString("@NotNull parameter 'reasonCode'"));
            new Mqtt5PubAckImpl(null, reasonString, userProperties);
        }

        @Test
        public void encode_omitReasonCodeAndPropertyLength() {
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
    }

    @RunWith(Parameterized.class)
    public static class ReasonCodeParamTests extends TestBase {
        @Parameterized.Parameter
        public Mqtt5PubAckReasonCode inputReasonCode;

        @Parameterized.Parameter(value = 1)
        public byte expectedReasonCode;

        @Parameterized.Parameters(name = "{index}: {0} -> {1}")
        public static Collection<Object[]> data() {
            return Arrays.asList(new Object[][]{
                    {Mqtt5PubAckReasonCode.SUCCESS, (byte) 0x00},
                    {Mqtt5PubAckReasonCode.NO_MATCHING_SUBSCRIBERS, (byte) 0x10},
                    {Mqtt5PubAckReasonCode.UNSPECIFIED_ERROR, (byte) 0x80},
                    {Mqtt5PubAckReasonCode.IMPLEMENTATION_SPECIFIC_ERROR, (byte) 0x83},
                    {Mqtt5PubAckReasonCode.NOT_AUTHORIZED, (byte) 0x87},
                    {Mqtt5PubAckReasonCode.TOPIC_NAME_INVALID, (byte) 0x90},
                    {Mqtt5PubAckReasonCode.PACKET_IDENTIFIER_IN_USE, (byte) 0x91},
                    {Mqtt5PubAckReasonCode.QUOTA_EXCEEDED, (byte) 0x97},
                    {Mqtt5PubAckReasonCode.PAYLOAD_FORMAT_INVALID, (byte) 0x99}
            });
        }

        @Test
        public void encode_reasonCodes() {
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
                    expectedReasonCode,
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
                    new Mqtt5PubAckImpl(inputReasonCode, reasonString, userProperties);
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
}

