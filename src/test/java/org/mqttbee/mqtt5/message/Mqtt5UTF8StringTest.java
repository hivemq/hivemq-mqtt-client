package org.mqttbee.mqtt5.message;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Silvio Giebl
 */
public class Mqtt5UTF8StringTest {

    @Test
    public void test_specification_example() {
        final String string = "A\uD869\uDED4";
        final byte[] expected = {0x0, 0x5, 0x41, (byte) 0xF0, (byte) 0xAA, (byte) 0x9B, (byte) 0x94};

        final Mqtt5UTF8String mqtt5UTF8String = Mqtt5UTF8String.from(string);

        assertNotNull(mqtt5UTF8String);

        final ByteBufAllocator allocator = new PooledByteBufAllocator();
        final ByteBuf byteBuf = allocator.buffer();
        mqtt5UTF8String.to(byteBuf);
        final byte[] actual = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(actual);

        assertArrayEquals(expected, actual);
    }

}