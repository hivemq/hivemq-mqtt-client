package org.mqttbee.mqtt5.codec.decoder;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt5.message.Mqtt5Disconnect;
import org.mqttbee.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.mqtt5.message.Mqtt5UserProperty;
import org.mqttbee.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Silvio Giebl
 */
public class Mqtt5DisconnectDecoderTest {

    private EmbeddedChannel channel;

    @Before
    public void setUp() {
        channel = new EmbeddedChannel(new Mqtt5Decoder(new Mqtt5DisconnectTestMessageDecoders()));
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
                (byte) 0b1110_0000,
                //   remaining length
                58,
                // variable header
                //   reason code (normal disconnection)
                0x00,
                //   properties
                56,
                //     session expiry interval
                0x11, 0, 0, 0, 10,
                //     reason string
                0x1F, 0, 7, 's', 'u', 'c', 'c', 'e', 's', 's',
                //     user properties
                0x26, 0, 4, 't', 'e', 's', 't', 0, 5, 'v', 'a', 'l', 'u', 'e',
                0x26, 0, 4, 't', 'e', 's', 't', 0, 6, 'v', 'a', 'l', 'u', 'e', '2',
                //     server reference
                0x1C, 0, 9, 'r', 'e', 'f', 'e', 'r', 'e', 'n', 'c', 'e'
        };

        final ByteBuf byteBuf = channel.alloc().buffer();
        byteBuf.writeBytes(encoded);
        channel.writeInbound(byteBuf);
        final Mqtt5Disconnect disconnect = channel.readInbound();

        assertNotNull(disconnect);

        assertEquals(Mqtt5DisconnectReasonCode.NORMAL_DISCONNECTION, disconnect.getReasonCode());
        assertTrue(disconnect.getSessionExpiryInterval().isPresent());
        assertEquals(10, (long) disconnect.getSessionExpiryInterval().get());
        assertTrue(disconnect.getReasonString().isPresent());
        assertEquals("success", disconnect.getReasonString().get().toString());
        assertTrue(disconnect.getServerReference().isPresent());
        assertEquals("reference", disconnect.getServerReference().get().toString());

        final ImmutableList<Mqtt5UserProperty> userProperties = disconnect.getUserProperties();
        assertEquals(2, userProperties.size());
        final Mqtt5UTF8String test = Mqtt5UTF8String.from("test");
        final Mqtt5UTF8String value = Mqtt5UTF8String.from("value");
        final Mqtt5UTF8String value2 = Mqtt5UTF8String.from("value2");
        assertNotNull(test);
        assertNotNull(value);
        assertNotNull(value2);
        assertTrue(userProperties.contains(new Mqtt5UserProperty(test, value)));
        assertTrue(userProperties.contains(new Mqtt5UserProperty(test, value2)));

    }

    private static class Mqtt5DisconnectTestMessageDecoders implements Mqtt5MessageDecoders {
        @Nullable
        @Override
        public Mqtt5MessageDecoder get(final int code) {
            if (code == Mqtt5MessageType.DISCONNECT.getCode()) {
                return new Mqtt5DisconnectDecoder();
            }
            return null;
        }
    }

}