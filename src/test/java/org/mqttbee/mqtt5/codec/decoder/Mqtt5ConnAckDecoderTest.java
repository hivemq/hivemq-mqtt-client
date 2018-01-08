package org.mqttbee.mqtt5.codec.decoder;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt5.message.Mqtt5ConnAck;
import org.mqttbee.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.mqtt5.message.Mqtt5UserProperty;
import org.mqttbee.mqtt5.message.connack.Mqtt5ConnAckReasonCode;

import static org.junit.Assert.*;

/**
 * @author Silvio Giebl
 */
public class Mqtt5ConnAckDecoderTest {

    private EmbeddedChannel channel;

    @Before
    public void setUp() {
        channel = new EmbeddedChannel(new Mqtt5Decoder(new Mqtt5ConnAckTestMessageDecoders()));
    }

    @After
    public void tearDown() throws Exception {
        channel.close();
    }

    @Test
    public void test_example() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0010_0000,
                //   remaining length
                122,
                // variable header
                //   connack flags
                0b0000_0001,
                //   reason code (success)
                0x00,
                //   properties
                119,
                //     session expiry interval
                0x11, 0, 0, 0, 10,
                //     receive maximum
                0x21, 0, 100,
                //     maximum qos
                0x24, 1,
                //     retain available
                0x25, 0,
                //     maximum packet size
                0x27, 0, 0, 0, 100,
                //     assigned client identifier
                0x12, 0, 4, 't', 'e', 's', 't',
                //     topic alias maximum
                0x22, 0, 5,
                //     reason string
                0x1F, 0, 7, 's', 'u', 'c', 'c', 'e', 's', 's',
                //     user properties
                0x26, 0, 4, 't', 'e', 's', 't', 0, 5, 'v', 'a', 'l', 'u', 'e',
                0x26, 0, 4, 't', 'e', 's', 't', 0, 6, 'v', 'a', 'l', 'u', 'e', '2',
                //     wildcard subscription available
                0x28, 0,
                //     subscription identifiers available
                0x29, 1,
                //     shared subscription available
                0x2A, 0,
                //     server keep alive
                0x13, 0, 10,
                //     response information
                0x1A, 0, 8, 'r', 'e', 's', 'p', 'o', 'n', 's', 'e',
                //     server reference
                0x1C, 0, 6, 's', 'e', 'r', 'v', 'e', 'r',
                //     auth method
                0x15, 0, 8, 'G', 'S', '2', '-', 'K', 'R', 'B', '5',
                //     auth data
                0x16, 0, 10, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10
        };

        final ByteBuf byteBuf = channel.alloc().buffer();
        byteBuf.writeBytes(encoded);
        channel.writeInbound(byteBuf);
        final Mqtt5ConnAck connAck = channel.readInbound();

        assertNotNull(connAck);

        assertEquals(Mqtt5ConnAckReasonCode.SUCCESS, connAck.getReasonCode());
        assertTrue(connAck.getSessionExpiryInterval().isPresent());
        assertEquals(10, (long) connAck.getSessionExpiryInterval().get());
        assertTrue(connAck.getAssignedClientIdentifier().isPresent());
        assertEquals("test", connAck.getAssignedClientIdentifier().get().toString());
        assertTrue(connAck.getReasonString().isPresent());
        assertEquals("success", connAck.getReasonString().get().toString());
        assertTrue(connAck.getServerKeepAlive().isPresent());
        assertEquals(10, (long) connAck.getServerKeepAlive().get());
        assertTrue(connAck.getResponseInformation().isPresent());
        assertEquals("response", connAck.getResponseInformation().get().toString());
        assertTrue(connAck.getServerReference().isPresent());
        assertEquals("server", connAck.getServerReference().get().toString());

        final ImmutableList<Mqtt5UserProperty> userProperties = connAck.getUserProperties();
        assertEquals(2, userProperties.size());
        final Mqtt5UTF8String test = Mqtt5UTF8String.from("test");
        final Mqtt5UTF8String value = Mqtt5UTF8String.from("value");
        final Mqtt5UTF8String value2 = Mqtt5UTF8String.from("value2");
        assertNotNull(test);
        assertNotNull(value);
        assertNotNull(value2);
        assertTrue(userProperties.contains(new Mqtt5UserProperty(test, value)));
        assertTrue(userProperties.contains(new Mqtt5UserProperty(test, value2)));

        final Mqtt5ConnAck.Restrictions restrictions = connAck.getRestrictions();
        assertEquals(100, restrictions.getReceiveMaximum());
        assertEquals(1, restrictions.getMaximumQoS());
        assertEquals(false, restrictions.isRetainAvailable());
        assertEquals(100, restrictions.getMaximumPacketSize());
        assertEquals(5, restrictions.getTopicAliasMaximum());
        assertEquals(false, restrictions.isWildcardSubscriptionAvailable());
        assertEquals(true, restrictions.isSubscriptionIdentifierAvailable());
        assertEquals(false, restrictions.isSharedSubscriptionAvailable());

        assertTrue(connAck.getAuth().isPresent());
        final Mqtt5ConnAck.Auth auth = connAck.getAuth().get();
    }

    private static class Mqtt5ConnAckTestMessageDecoders implements Mqtt5MessageDecoders {
        @Nullable
        @Override
        public Mqtt5MessageDecoder get(final int code) {
            if (code == Mqtt5MessageType.CONNACK.getCode()) {
                return new Mqtt5ConnAckDecoder();
            }
            return null;
        }
    }

}