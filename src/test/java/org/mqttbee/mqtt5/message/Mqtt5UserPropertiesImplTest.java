package org.mqttbee.mqtt5.message;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Silvio Giebl
 */
class Mqtt5UserPropertiesImplTest {

    @Test
    void test_of() {
        final Mqtt5UTF8StringImpl name = requireNonNull(Mqtt5UTF8StringImpl.from("name"));
        final Mqtt5UTF8StringImpl value = requireNonNull(Mqtt5UTF8StringImpl.from("value"));
        final Mqtt5UserPropertyImpl userProperty = new Mqtt5UserPropertyImpl(name, value);
        final ImmutableList<Mqtt5UserPropertyImpl> userPropertiesList = ImmutableList.of(userProperty);
        final Mqtt5UserPropertiesImpl userProperties = Mqtt5UserPropertiesImpl.of(userPropertiesList);
        assertSame(userPropertiesList, userProperties.asList());
    }

    @Test
    void test_build_not_null() {
        final ImmutableList.Builder<Mqtt5UserPropertyImpl> builder = ImmutableList.builder();
        final Mqtt5UTF8StringImpl name = requireNonNull(Mqtt5UTF8StringImpl.from("name"));
        final Mqtt5UTF8StringImpl value = requireNonNull(Mqtt5UTF8StringImpl.from("value"));
        builder.add(new Mqtt5UserPropertyImpl(name, value));
        final Mqtt5UserPropertiesImpl userProperties = Mqtt5UserPropertiesImpl.build(builder);
        final ImmutableList<Mqtt5UserPropertyImpl> userPropertiesList = userProperties.asList();
        assertEquals(1, userPropertiesList.size());
        assertEquals(name, userPropertiesList.get(0).getName());
        assertEquals(value, userPropertiesList.get(0).getValue());
    }

    @Test
    void test_build_null() {
        final Mqtt5UserPropertiesImpl userProperties = Mqtt5UserPropertiesImpl.build(null);
        assertEquals(Mqtt5UserPropertiesImpl.NO_USER_PROPERTIES, userProperties);
        assertEquals(0, userProperties.asList().size());
    }

    @Test
    void test_encode() {
        final byte[] expected = {
                Mqtt5Property.USER_PROPERTY, 0, 4, 'n', 'a', 'm', 'e', 0, 5, 'v', 'a', 'l', 'u', 'e',
                Mqtt5Property.USER_PROPERTY, 0, 4, 'n', 'a', 'm', 'e', 0, 4, 't', 'e', 's', 't'
        };
        final Mqtt5UTF8StringImpl name = requireNonNull(Mqtt5UTF8StringImpl.from("name"));
        final Mqtt5UTF8StringImpl value = requireNonNull(Mqtt5UTF8StringImpl.from("value"));
        final Mqtt5UTF8StringImpl value2 = requireNonNull(Mqtt5UTF8StringImpl.from("test"));
        final Mqtt5UserPropertyImpl userProperty1 = new Mqtt5UserPropertyImpl(name, value);
        final Mqtt5UserPropertyImpl userProperty2 = new Mqtt5UserPropertyImpl(name, value2);
        final Mqtt5UserPropertiesImpl userProperties =
                Mqtt5UserPropertiesImpl.of(ImmutableList.of(userProperty1, userProperty2));

        final ByteBuf byteBuf = Unpooled.buffer();
        userProperties.encode(byteBuf);
        final byte[] actual = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(actual);
        byteBuf.release();

        assertArrayEquals(expected, actual);
    }

    @Test
    void test_encodedLength() {
        final Mqtt5UTF8StringImpl name = requireNonNull(Mqtt5UTF8StringImpl.from("name"));
        final Mqtt5UTF8StringImpl value = requireNonNull(Mqtt5UTF8StringImpl.from("value"));
        final Mqtt5UTF8StringImpl value2 = requireNonNull(Mqtt5UTF8StringImpl.from("test"));
        final Mqtt5UserPropertyImpl userProperty1 = new Mqtt5UserPropertyImpl(name, value);
        final Mqtt5UserPropertyImpl userProperty2 = new Mqtt5UserPropertyImpl(name, value2);
        final Mqtt5UserPropertiesImpl userProperties =
                Mqtt5UserPropertiesImpl.of(ImmutableList.of(userProperty1, userProperty2));

        assertEquals(27, userProperties.encodedLength());
    }

}