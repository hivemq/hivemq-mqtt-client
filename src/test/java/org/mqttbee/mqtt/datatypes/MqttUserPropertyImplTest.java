package org.mqttbee.mqtt.datatypes;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Silvio Giebl
 */
public class MqttUserPropertyImplTest {

    @Test
    public void test_decode() {
        final byte[] binary = {0, 4, 'n', 'a', 'm', 'e', 0, 5, 'v', 'a', 'l', 'u', 'e'};
        final ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeBytes(binary);
        final MqttUserPropertyImpl userProperty = MqttUserPropertyImpl.decode(byteBuf);
        byteBuf.release();
        assertNotNull(userProperty);
        assertEquals("name", userProperty.getName().toString());
        assertEquals("value", userProperty.getValue().toString());
    }

    @Test
    public void test_decode_malformed_name() {
        final byte[] binary = {0, 4, 'n', 'a', 'm', 0, 5, 'v', 'a', 'l', 'u', 'e'};
        final ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeBytes(binary);
        final MqttUserPropertyImpl userProperty = MqttUserPropertyImpl.decode(byteBuf);
        byteBuf.release();
        assertNull(userProperty);
    }

    @Test
    public void test_decode_malformed_value() {
        final byte[] binary = {0, 4, 'n', 'a', 'm', 'e', 0, 5, 'v', 'a', 'l', 'u'};
        final ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeBytes(binary);
        final MqttUserPropertyImpl userProperty = MqttUserPropertyImpl.decode(byteBuf);
        byteBuf.release();
        assertNull(userProperty);
    }

    @Test
    public void test_equals() {
        EqualsVerifier.forClass(MqttUserPropertyImpl.class)
                .withNonnullFields("name", "value")
                .suppress(Warning.STRICT_INHERITANCE)
                .verify();
    }

}