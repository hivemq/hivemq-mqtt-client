package org.mqttbee.mqtt5.message;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Silvio Giebl
 */
public class Mqtt5UserPropertyTest {

    @Test
    public void test_decode() throws Exception {
        final byte[] binary = {0, 4, 'n', 'a', 'm', 'e', 0, 5, 'v', 'a', 'l', 'u', 'e'};
        final ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeBytes(binary);
        final Mqtt5UserProperty userProperty = Mqtt5UserProperty.decode(byteBuf);
        byteBuf.release();
        assertNotNull(userProperty);
        assertEquals("name", userProperty.getName().toString());
        assertEquals("value", userProperty.getValue().toString());
    }

    @Test
    public void test_decode_malformed() throws Exception {
        final byte[] binary = {0, 4, 'n', 'a', 'm', 0, 5, 'v', 'a', 'l', 'u', 'e'};
        final ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeBytes(binary);
        final Mqtt5UserProperty userProperty = Mqtt5UserProperty.decode(byteBuf);
        byteBuf.release();
        assertNull(userProperty);
    }

    @Test
    public void test_encode() throws Exception {
        final byte[] expected = {
                Mqtt5Property.USER_PROPERTY, 0, 4, 'n', 'a', 'm', 'e', 0, 5, 'v', 'a', 'l', 'u', 'e',
                Mqtt5Property.USER_PROPERTY, 0, 4, 'n', 'a', 'm', 'e', 0, 4, 't', 'e', 's', 't'};
        final Mqtt5UTF8String name = Mqtt5UTF8String.from("name");
        final Mqtt5UTF8String value = Mqtt5UTF8String.from("value");
        final Mqtt5UTF8String value2 = Mqtt5UTF8String.from("test");
        assertNotNull(name);
        assertNotNull(value);
        assertNotNull(value2);
        final Mqtt5UserProperty userProperty1 = new Mqtt5UserProperty(name, value);
        final Mqtt5UserProperty userProperty2 = new Mqtt5UserProperty(name, value2);
        final ImmutableList<Mqtt5UserProperty> userProperties = ImmutableList.of(userProperty1, userProperty2);

        final ByteBuf byteBuf = Unpooled.buffer();
        Mqtt5UserProperty.encode(userProperties, byteBuf);
        final byte[] actual = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(actual);
        byteBuf.release();

        assertArrayEquals(expected, actual);
    }

    @Test
    public void test_encodedLength() throws Exception {
        final Mqtt5UTF8String name = Mqtt5UTF8String.from("name");
        final Mqtt5UTF8String value = Mqtt5UTF8String.from("value");
        final Mqtt5UTF8String value2 = Mqtt5UTF8String.from("test");
        assertNotNull(name);
        assertNotNull(value);
        assertNotNull(value2);
        final Mqtt5UserProperty userProperty1 = new Mqtt5UserProperty(name, value);
        final Mqtt5UserProperty userProperty2 = new Mqtt5UserProperty(name, value2);
        final ImmutableList<Mqtt5UserProperty> userProperties = ImmutableList.of(userProperty1, userProperty2);

        assertEquals(27, Mqtt5UserProperty.encodedLength(userProperties));
    }

    @Test
    public void test_build_not_null() {
        final ImmutableList.Builder<Mqtt5UserProperty> builder = ImmutableList.builder();
        final Mqtt5UTF8String name = Mqtt5UTF8String.from("name");
        final Mqtt5UTF8String value = Mqtt5UTF8String.from("value");
        assertNotNull(name);
        assertNotNull(value);
        builder.add(new Mqtt5UserProperty(name, value));
        final ImmutableList<Mqtt5UserProperty> userProperties = Mqtt5UserProperty.build(builder);
        assertEquals(1, userProperties.size());
        assertEquals(name, userProperties.get(0).getName());
        assertEquals(value, userProperties.get(0).getValue());
    }

    @Test
    public void test_build_null() {
        final ImmutableList<Mqtt5UserProperty> userProperties = Mqtt5UserProperty.build(null);
        assertEquals(Mqtt5UserProperty.DEFAULT_NO_USER_PROPERTIES, userProperties);
        assertEquals(0, userProperties.size());
    }

}