package org.mqttbee.mqtt5.codec.decoder;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt5.message.Mqtt5Auth;
import org.mqttbee.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.mqtt5.message.Mqtt5UserProperty;
import org.mqttbee.mqtt5.message.auth.Mqtt5AuthReasonCode;

import static org.junit.Assert.*;

/**
 * @author Silvio Giebl
 */
public class Mqtt5AuthDecoderTest {

    private EmbeddedChannel channel;

    @Before
    public void setUp() {
        channel = new EmbeddedChannel(new Mqtt5Decoder(new Mqtt5AuthTestMessageDecoders()));
    }

    @After
    public void tearDown() throws Exception {
        channel.close();
    }

    @Test
    public void test_example() throws Exception {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                (byte) 0b1111_0000,
                //   remaining length
                66,
                // variable header
                //   reason code (continue)
                0x18,
                //   properties
                64,
                //     auth method
                0x15, 0, 8, 'G', 'S', '2', '-', 'K', 'R', 'B', '5',
                //     auth data
                0x16, 0, 10, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
                //     reason string
                0x1F, 0, 8, 'c', 'o', 'n', 't', 'i', 'n', 'u', 'e',
                //     user properties
                0x26, 0, 4, 't', 'e', 's', 't', 0, 5, 'v', 'a', 'l', 'u', 'e',
                0x26, 0, 4, 't', 'e', 's', 't', 0, 6, 'v', 'a', 'l', 'u', 'e', '2',
        };

        final ByteBuf byteBuf = channel.alloc().buffer();
        byteBuf.writeBytes(encoded);
        channel.writeInbound(byteBuf);
        final Mqtt5Auth auth = channel.readInbound();

        assertNotNull(auth);

        assertEquals(Mqtt5AuthReasonCode.CONTINUE_AUTHENTICATION, auth.getReasonCode());
        assertEquals("GS2-KRB5", auth.getMethod().toString());
        assertTrue(auth.getData().isPresent());
        assertArrayEquals(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, auth.getData().get());
        assertTrue(auth.getReasonString().isPresent());
        assertEquals("continue", auth.getReasonString().get().toString());

        final ImmutableList<Mqtt5UserProperty> userProperties = auth.getUserProperties();
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

    private static class Mqtt5AuthTestMessageDecoders implements Mqtt5MessageDecoders {
        @Nullable
        @Override
        public Mqtt5MessageDecoder get(final int code) {
            if (code == Mqtt5MessageType.AUTH.getCode()) {
                return new Mqtt5AuthDecoder();
            }
            return null;
        }
    }

}