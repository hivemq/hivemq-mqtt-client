package org.mqttbee.mqtt5.message;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;

import java.nio.charset.Charset;

import static org.junit.Assert.*;

/**
 * @author Silvio Giebl
 */
public class Mqtt5UTF8StringTest {

    @Test
    public void test_must_not_null_character() {
        final String string = "abc\0def";
        assertNull(Mqtt5UTF8String.from(string));
    }

    @Test
    public void test_must_not_utf16_surrogates() {
        for (char c = '\uD800'; c <= '\uDFFF'; c++) {
            final String string = "abc" + c + "def";
            assertNull(Mqtt5UTF8String.from(string));
        }
    }

    @Test
    public void test_should_not_control_characters() {
        for (char c = '\u0001'; c <= '\u001F'; c++) {
            final String string = "abc" + c + "def";
            final Mqtt5UTF8String mqtt5UTF8String = Mqtt5UTF8String.from(string);
            assertNotNull(mqtt5UTF8String);
            assertTrue(mqtt5UTF8String.containsShouldNotCharacters());
        }
        for (char c = '\u007F'; c <= '\u009F'; c++) {
            final String string = "abc" + c + "def";
            final Mqtt5UTF8String mqtt5UTF8String = Mqtt5UTF8String.from(string);
            assertNotNull(mqtt5UTF8String);
            assertTrue(mqtt5UTF8String.containsShouldNotCharacters());
        }
    }

    @Test
    public void test_should_not_non_characters() {
        for (int c = 0xFFFE; c <= 0x10_FFFE; c += 0x1_0000) {
            final String string = "abc" + String.valueOf(Character.toChars(c)) + "def";
            final Mqtt5UTF8String mqtt5UTF8String = Mqtt5UTF8String.from(string);
            assertNotNull(mqtt5UTF8String);
            assertTrue(mqtt5UTF8String.containsShouldNotCharacters());

            final String string2 = "abc" + String.valueOf(Character.toChars(c + 1)) + "def";
            final Mqtt5UTF8String mqtt5UTF8String2 = Mqtt5UTF8String.from(string2);
            assertNotNull(mqtt5UTF8String2);
            assertTrue(mqtt5UTF8String2.containsShouldNotCharacters());
        }
    }

    @Test
    public void test_no_should_not_characters() {
        final String string = "abcdef";
        final Mqtt5UTF8String mqtt5UTF8String = Mqtt5UTF8String.from(string);
        assertNotNull(mqtt5UTF8String);
        assertFalse(mqtt5UTF8String.containsShouldNotCharacters());
    }

    @Test
    public void test_zero_width_no_break_space() {
        final byte[] binary = {'a', 'b', 'c', (byte) 0xEF, (byte) 0xBB, (byte) 0xBF, 'd', 'e', 'f'};
        final ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeByte(0).writeByte(9).writeBytes(binary);
        final Mqtt5UTF8String mqtt5UTF8String = Mqtt5UTF8String.from(byteBuf);
        assertNotNull(mqtt5UTF8String);
        assertArrayEquals(mqtt5UTF8String.toBinary(), binary);
        assertTrue(mqtt5UTF8String.toString().contains("\uFEFF"));
    }

    @Test
    public void test_from_byteBuf() {
        final String string = "abcdef";

        final ByteBuf byteBuf = Unpooled.buffer();
        Mqtt5DataTypes.encodeBinaryData(string.getBytes(Charset.forName("UTF-8")), byteBuf);
        final Mqtt5UTF8String mqtt5UTF8String = Mqtt5UTF8String.from(byteBuf);
        byteBuf.release();

        assertNotNull(mqtt5UTF8String);
        assertEquals(string, mqtt5UTF8String.toString());
    }

    @Test
    public void test_to_byteBuf() {
        final String string = "abcdef";
        final byte[] expected = {0, 6, 'a', 'b', 'c', 'd', 'e', 'f'};

        final Mqtt5UTF8String mqtt5UTF8String = Mqtt5UTF8String.from(string);
        assertNotNull(mqtt5UTF8String);

        final ByteBuf byteBuf = Unpooled.buffer();
        mqtt5UTF8String.to(byteBuf);
        final byte[] actual = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(actual);
        byteBuf.release();

        assertArrayEquals(expected, actual);
    }

    @Test
    public void test_specification_example() {
        final String string = "A\uD869\uDED4";
        final byte[] expected = {0x41, (byte) 0xF0, (byte) 0xAA, (byte) 0x9B, (byte) 0x94};
        final Mqtt5UTF8String mqtt5UTF8String = Mqtt5UTF8String.from(string);
        assertNotNull(mqtt5UTF8String);
        assertArrayEquals(expected, mqtt5UTF8String.toBinary());
        assertEquals(2 + mqtt5UTF8String.toBinary().length, mqtt5UTF8String.encodedLength());
    }

}