package org.mqttbee.mqtt5.codec.encoder;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Before;
import org.junit.Test;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.message.Mqtt5ClientIdentifier;
import org.mqttbee.mqtt5.message.Mqtt5QoS;
import org.mqttbee.mqtt5.message.Mqtt5Topic;
import org.mqttbee.mqtt5.message.Mqtt5UserProperty;
import org.mqttbee.mqtt5.message.connect.Mqtt5ConnectImpl;
import org.mqttbee.mqtt5.message.publish.Mqtt5WillPublishImpl;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Silvio Giebl
 */
public class Mqtt5ConnectEncoderTest {

    private EmbeddedChannel channel;

    @Before
    public void setUp() {
        channel = new EmbeddedChannel(new Mqtt5Encoder(new Mqtt5ConnectTestEncoders()));
    }

    @Test
    public void test_example() {
        final byte[] expected = {
                // fixed header
                //   type, flags
                0b0001_0000,
                // remaining length
                68,
                // variable header
                //   protocol name
                0b0000_0000, 0b0000_0100, 0b0100_1101, 0b0101_0001, 0b0101_0100, 0b0101_0100,
                //   protocol version
                0b0000_0101,
                //   connect flags
                (byte) 0b1100_1110,
                //   keep alive
                0b0000_0000, 0b0000_1010,
                //   properties
                0b0000_0101,
                //     session expiry interval
                0b0001_0001, 0b0000_0000, 0b0000_0000, 0b0000_0000, 0b0000_1010,
                // payload
                //   client identifier
                0b0000_0000, 0b0000_0100, 't', 'e', 's', 't',
                //   will properties
                0b0000_1010,
                //     message expiry interval
                0b0000_0010, 0b0000_0000, 0b0000_0000, 0b0000_0000, 0b0000_1010,
                //     will delay interval
                0b0001_1000, 0b0000_0000, 0b0000_0000, 0b0000_0000, 0b0000_0101,
                //   will topic
                0b0000_0000, 0b0000_0101, 't', 'o', 'p', 'i', 'c',
                //   will payload
                0b0000_0000, 0b0000_1010, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
                //   username
                0b0000_0000, 0b0000_1000, 'u', 's', 'e', 'r', 'n', 'a', 'm', 'e',
                //   password
                0b0000_0000, 0b0000_0100, 1, 5, 6, 3
        };

        final Mqtt5ClientIdentifier clientIdentifier = Mqtt5ClientIdentifier.from("test");
        assertNotNull(clientIdentifier);

        final Mqtt5ClientIdentifier username = Mqtt5ClientIdentifier.from("username");
        assertNotNull(username);
        final byte[] password = {1, 5, 6, 3};
        final Mqtt5ConnectImpl.AuthImpl auth = new Mqtt5ConnectImpl.AuthImpl(username, password, null, null);

        final Mqtt5Topic willTopic = Mqtt5Topic.from("topic");
        assertNotNull(willTopic);
        final byte[] willPayload = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        final Mqtt5QoS willQoS = Mqtt5QoS.AT_LEAST_ONCE;
        final ImmutableList<Mqtt5UserProperty> willUserProperties = ImmutableList.of();
        final Mqtt5WillPublishImpl willPublish = new Mqtt5WillPublishImpl(
                willTopic, willPayload, willQoS, false, 10, null, null,
                null, null, willUserProperties, 5);

        final ImmutableList<Mqtt5UserProperty> userProperties = ImmutableList.of();

        final Mqtt5ConnectImpl connect = new Mqtt5ConnectImpl(
                clientIdentifier, 10, true, 10, false, true,
                Mqtt5ConnectImpl.RestrictionsImpl.DEFAULT, auth, willPublish, userProperties);

        channel.writeOutbound(connect);
        final ByteBuf byteBuf = (ByteBuf) channel.readOutbound();

        final byte[] actual = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(actual);

        assertArrayEquals(expected, actual);
    }

    private static class Mqtt5ConnectTestEncoders extends Mqtt5TestMessageEncoders {
        @NotNull
        @Override
        public Mqtt5MessageEncoder<Mqtt5ConnectImpl> getConnectEncoder() {
            return new Mqtt5ConnectEncoder();
        }
    }

}