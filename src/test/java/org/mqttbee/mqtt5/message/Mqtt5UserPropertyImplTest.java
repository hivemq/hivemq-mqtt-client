package org.mqttbee.mqtt5.message;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Silvio Giebl
 */
public class Mqtt5UserPropertyImplTest {

    @Test
    public void test_decode() throws Exception {
        final byte[] binary = {0, 4, 'n', 'a', 'm', 'e', 0, 5, 'v', 'a', 'l', 'u', 'e'};
        final ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeBytes(binary);
        final Mqtt5UserPropertyImpl userProperty = Mqtt5UserPropertyImpl.decode(byteBuf);
        byteBuf.release();
        assertNotNull(userProperty);
        assertEquals("name", userProperty.getName().toString());
        assertEquals("value", userProperty.getValue().toString());
    }

    @Test
    public void test_decode_malformed_name() throws Exception {
        final byte[] binary = {0, 4, 'n', 'a', 'm', 0, 5, 'v', 'a', 'l', 'u', 'e'};
        final ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeBytes(binary);
        final Mqtt5UserPropertyImpl userProperty = Mqtt5UserPropertyImpl.decode(byteBuf);
        byteBuf.release();
        assertNull(userProperty);
    }

    @Test
    public void test_decode_malformed_value() throws Exception {
        final byte[] binary = {0, 4, 'n', 'a', 'm', 'e', 0, 5, 'v', 'a', 'l', 'u'};
        final ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeBytes(binary);
        final Mqtt5UserPropertyImpl userProperty = Mqtt5UserPropertyImpl.decode(byteBuf);
        byteBuf.release();
        assertNull(userProperty);
    }

    @Test
    public void test_equals() {
        EqualsVerifier.forClass(Mqtt5UserPropertyImpl.class).withNonnullFields("name", "value")
                .suppress(Warning.STRICT_INHERITANCE).verify();
    }

}