package org.mqttbee.mqtt5.codec.encoder;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mqttbee.mqtt5.message.*;
import org.mqttbee.mqtt5.message.auth.Mqtt5ExtendedAuthImpl;
import org.mqttbee.mqtt5.message.connect.Mqtt5ConnectImpl;
import org.mqttbee.mqtt5.message.publish.Mqtt5PayloadFormatIndicator;
import org.mqttbee.mqtt5.message.publish.Mqtt5WillPublishImpl;

import static java.util.Objects.requireNonNull;
import static org.junit.Assert.assertArrayEquals;

/**
 * @author Silvio Giebl
 */
public class Mqtt5ConnectEncoderTest {

    private EmbeddedChannel channel;

    @Before
    public void setUp() {
        channel = new EmbeddedChannel(new Mqtt5Encoder());
    }

    @After
    public void tearDown() throws Exception {
        channel.close();
    }

    @Test
    public void test_example() {
        final byte[] expected = {
                // fixed header
                //   type, flags
                0b0001_0000,
                // remaining length (223)
                (byte) (128 + 95), 1,
                // variable header
                //   protocol name
                0, 4, 'M', 'Q', 'T', 'T',
                //   protocol version
                5,
                //   connect flags
                (byte) 0b1110_1110,
                //   keep alive
                0, 10,
                //   properties
                88,
                //     session expiry interval
                0x11, 0, 0, 0, 10,
                //     request response information
                0x19, 1,
                //     request problem information
                0x17, 0,
                //     auth method
                0x15, 0, 8, 'G', 'S', '2', '-', 'K', 'R', 'B', '5',
                //     auth data
                0x16, 0, 10, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
                //     receive maximum
                0x21, 0, 5,
                //     topic alias maximum
                0x22, 0, 10,
                //     maximum packet size
                0x27, 0, 0, 0, 100,
                //     user properties
                0x26, 0, 4, 't', 'e', 's', 't', 0, 5, 'v', 'a', 'l', 'u', 'e',
                0x26, 0, 4, 't', 'e', 's', 't', 0, 6, 'v', 'a', 'l', 'u', 'e', '2',
                0x26, 0, 5, 't', 'e', 's', 't', '2', 0, 5, 'v', 'a', 'l', 'u', 'e',
                // payload
                //   client identifier
                0, 4, 't', 'e', 's', 't',
                //   will properties
                82,
                //     message expiry interval
                0x02, 0, 0, 0, 10,
                //     payload format indicator
                0x01, 1,
                //     content type
                0x03, 0, 4, 't', 'e', 'x', 't',
                //     response topic
                0x08, 0, 8, 'r', 'e', 's', 'p', 'o', 'n', 's', 'e',
                //     correlation data
                0x09, 0, 5, 5, 4, 3, 2, 1,
                //     user property
                0x26, 0, 4, 't', 'e', 's', 't', 0, 5, 'v', 'a', 'l', 'u', 'e',
                0x26, 0, 4, 't', 'e', 's', 't', 0, 6, 'v', 'a', 'l', 'u', 'e', '2',
                0x26, 0, 5, 't', 'e', 's', 't', '2', 0, 5, 'v', 'a', 'l', 'u', 'e',
                //     will delay interval
                24, 0, 0, 0, 5,
                //   will topic
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   will payload
                0, 10, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
                //   username
                0, 8, 'u', 's', 'e', 'r', 'n', 'a', 'm', 'e',
                //   password
                0, 4, 1, 5, 6, 3
        };

        final Mqtt5ClientIdentifier clientIdentifier = requireNonNull(Mqtt5ClientIdentifier.from("test"));

        final Mqtt5ClientIdentifier username = requireNonNull(Mqtt5ClientIdentifier.from("username"));
        final byte[] password = {1, 5, 6, 3};
        final Mqtt5ConnectImpl.SimpleAuthImpl simpleAuth = new Mqtt5ConnectImpl.SimpleAuthImpl(username, password);

        final Mqtt5UTF8String authMethod = requireNonNull(Mqtt5UTF8String.from("GS2-KRB5"));
        final byte[] authData = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        final Mqtt5ExtendedAuthImpl extendedAuth = new Mqtt5ExtendedAuthImpl(authMethod, authData);

        final Mqtt5UTF8String test = requireNonNull(Mqtt5UTF8String.from("test"));
        final Mqtt5UTF8String test2 = requireNonNull(Mqtt5UTF8String.from("test2"));
        final Mqtt5UTF8String value = requireNonNull(Mqtt5UTF8String.from("value"));
        final Mqtt5UTF8String value2 = requireNonNull(Mqtt5UTF8String.from("value2"));
        final Mqtt5UserProperty userProperty1 = new Mqtt5UserProperty(test, value);
        final Mqtt5UserProperty userProperty2 = new Mqtt5UserProperty(test, value2);
        final Mqtt5UserProperty userProperty3 = new Mqtt5UserProperty(test2, value);
        final ImmutableList<Mqtt5UserProperty> userProperties =
                ImmutableList.of(userProperty1, userProperty2, userProperty3);

        final Mqtt5Topic willTopic = requireNonNull(Mqtt5Topic.from("topic"));
        final byte[] willPayload = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        final Mqtt5QoS willQoS = Mqtt5QoS.AT_LEAST_ONCE;
        final Mqtt5UTF8String willContentType = requireNonNull(Mqtt5UTF8String.from("text"));
        final Mqtt5Topic willResponseTopic = requireNonNull(Mqtt5Topic.from("response"));
        final byte[] willCorrelationData = {5, 4, 3, 2, 1};
        final Mqtt5WillPublishImpl willPublish = new Mqtt5WillPublishImpl(
                willTopic, willPayload, willQoS, true, 10, Mqtt5PayloadFormatIndicator.UTF_8, willContentType,
                willResponseTopic, willCorrelationData, userProperties, 5);

        final Mqtt5ConnectImpl.RestrictionsImpl restrictions = new Mqtt5ConnectImpl.RestrictionsImpl(5, 10, 100);

        final Mqtt5ConnectImpl connect = new Mqtt5ConnectImpl(
                clientIdentifier, 10, true, 10, true, false,
                restrictions, simpleAuth, extendedAuth, willPublish, userProperties);

        channel.writeOutbound(connect);
        final ByteBuf byteBuf = channel.readOutbound();

        final byte[] actual = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(actual);

        assertArrayEquals(expected, actual);
    }

    // password binary data exceeded
    // will payload binary data exceeded
    // remaining length variable byte integer exceeded
    // auth data binary data exceeded
    // property length variable byte integer exceeded
    // correlation data binary data exceeded

}