package org.mqttbee.mqtt5.message;

import com.google.common.base.Charsets;
import com.google.common.base.Utf8;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;
import org.mqttbee.mqtt5.exceptions.Mqtt5BinaryDataExceededException;

import java.nio.charset.Charset;
import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * @author Silvio Giebl
 */
public class Mqtt5UTF8StringImplTest {

    @Test
    public void must_not_null_character() {
        assertNull(Mqtt5UTF8StringImpl.from("abc\0def"));

        assertNull(Mqtt5UTF8StringImpl.from(new byte[]{'a', 'b', 'c', '\0', 'd', 'e', 'f'}));
    }

    @Test
    public void must_not_utf16_surrogates() {
        for (char c = '\uD800'; c <= '\uDFFF'; c++) {
            assertNull(Mqtt5UTF8StringImpl.from("abc" + c + "def"));
        }

        for (int b = 0xA0; b <= 0xBF; b++) {
            for (int b2 = 0; b2 < 0xFF; b2++) {
                assertNull(Mqtt5UTF8StringImpl.from(
                        new byte[]{'a', 'b', 'c', (byte) 0xED, (byte) b, (byte) b2, 'd', 'e', 'f'}));
            }
        }
    }

    @Test
    public void should_not_control_characters() {
        for (char c = '\u0001'; c <= '\u001F'; c++) {
            final Mqtt5UTF8StringImpl string = Mqtt5UTF8StringImpl.from("abc" + c + "def");
            assertNotNull(string);
            assertTrue(string.containsShouldNotCharacters());

            final Mqtt5UTF8StringImpl binary =
                    Mqtt5UTF8StringImpl.from(new byte[]{'a', 'b', 'c', (byte) c, 'd', 'e', 'f'});
            assertNotNull(binary);
            assertTrue(binary.containsShouldNotCharacters());
        }
        for (char c = '\u007F'; c <= '\u009F'; c++) {
            final Mqtt5UTF8StringImpl string = Mqtt5UTF8StringImpl.from("abc" + c + "def");
            assertNotNull(string);
            assertTrue(string.containsShouldNotCharacters());
        }

        {
            final Mqtt5UTF8StringImpl binary = Mqtt5UTF8StringImpl.from(new byte[]{'a', 'b', 'c', 0x7F, 'd', 'e', 'f'});
            assertNotNull(binary);
            assertTrue(binary.containsShouldNotCharacters());
        }
        for (int b = 0x80; b <= 0x9F; b++) {
            final Mqtt5UTF8StringImpl binary =
                    Mqtt5UTF8StringImpl.from(new byte[]{'a', 'b', 'c', (byte) 0xC2, (byte) b, 'd', 'e', 'f'});
            assertNotNull(binary);
            assertTrue(binary.containsShouldNotCharacters());
        }
    }

    @Test
    public void should_not_non_characters() {
        for (int c = 0xFFFE; c <= 0x10_FFFE; c += 0x1_0000) {
            for (int i = 0; i < 2; i++) {
                final String nonCharacterString = String.valueOf(Character.toChars(c + i));
                final Mqtt5UTF8StringImpl string = Mqtt5UTF8StringImpl.from("abc" + nonCharacterString + "def");
                assertNotNull(string);
                assertTrue(string.containsShouldNotCharacters());

                final byte[] nonCharacterBinary = nonCharacterString.getBytes(Charsets.UTF_8);
                final byte[] binary = new byte[6 + nonCharacterBinary.length];
                binary[0] = 'a';
                binary[1] = 'b';
                binary[2] = 'c';
                System.arraycopy(nonCharacterBinary, 0, binary, 3, nonCharacterBinary.length);
                binary[3 + nonCharacterBinary.length] = 'd';
                binary[3 + nonCharacterBinary.length + 1] = 'e';
                binary[3 + nonCharacterBinary.length + 2] = 'f';
                final Mqtt5UTF8StringImpl binaryString = Mqtt5UTF8StringImpl.from(binary);
                assertNotNull(binaryString);
                assertTrue(binaryString.containsShouldNotCharacters());
            }
        }
    }

    @Test
    public void no_should_not_characters() {
        final Mqtt5UTF8StringImpl string = Mqtt5UTF8StringImpl.from("abcdef");
        assertNotNull(string);
        assertFalse(string.containsShouldNotCharacters());

        final Mqtt5UTF8StringImpl binary = Mqtt5UTF8StringImpl.from(new byte[]{'a', 'b', 'c', 'd', 'e', 'f'});
        assertNotNull(binary);
        assertFalse(binary.containsShouldNotCharacters());
    }

    @Test
    public void zero_width_no_break_space() {
        final Mqtt5UTF8StringImpl string = Mqtt5UTF8StringImpl.from("abc" + '\uFEFF' + "def");
        assertNotNull(string);
        assertTrue(string.toString().contains("\uFEFF"));

        final byte[] binary = new byte[]{'a', 'b', 'c', (byte) 0xEF, (byte) 0xBB, (byte) 0xBF, 'd', 'e', 'f'};
        final Mqtt5UTF8StringImpl binaryString = Mqtt5UTF8StringImpl.from(binary);
        assertNotNull(binaryString);
        assertArrayEquals(binaryString.toBinary(), binary);
        assertTrue(binaryString.toString().contains("\uFEFF"));
    }

    @Test
    public void from_byteBuf() {
        final String string = "abcdef";

        final ByteBuf byteBuf = Unpooled.buffer();
        Mqtt5DataTypes.encodeBinaryData(string.getBytes(Charset.forName("UTF-8")), byteBuf);
        final Mqtt5UTF8StringImpl mqtt5UTF8String = Mqtt5UTF8StringImpl.from(byteBuf);
        byteBuf.release();

        assertNotNull(mqtt5UTF8String);
        assertEquals(string, mqtt5UTF8String.toString());
    }

    @Test
    public void to_byteBuf() {
        final String string = "abcdef";
        final byte[] expected = {0, 6, 'a', 'b', 'c', 'd', 'e', 'f'};

        final Mqtt5UTF8StringImpl mqtt5UTF8String = Mqtt5UTF8StringImpl.from(string);
        assertNotNull(mqtt5UTF8String);

        final ByteBuf byteBuf = Unpooled.buffer();
        mqtt5UTF8String.to(byteBuf);
        final byte[] actual = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(actual);
        byteBuf.release();

        assertArrayEquals(expected, actual);
    }

    @Test
    public void specification_example() {
        final String string = "A\uD869\uDED4";
        final byte[] expected = {0x41, (byte) 0xF0, (byte) 0xAA, (byte) 0x9B, (byte) 0x94};
        final Mqtt5UTF8StringImpl mqtt5UTF8String = Mqtt5UTF8StringImpl.from(string);
        assertNotNull(mqtt5UTF8String);
        assertArrayEquals(expected, mqtt5UTF8String.toBinary());
        assertEquals(2 + mqtt5UTF8String.toBinary().length, mqtt5UTF8String.encodedLength());
    }

    @Test
    public void encodedLength() {
        final Mqtt5UTF8StringImpl string = Mqtt5UTF8StringImpl.from("abcdef");
        assertNotNull(string);
        assertEquals(2 + Utf8.encodedLength("abcdef"), string.encodedLength());

        final byte[] binary = new byte[]{'a', 'b', 'c', 'd', 'e', 'f'};
        final Mqtt5UTF8StringImpl binaryString = Mqtt5UTF8StringImpl.from(binary);
        assertNotNull(binaryString);
        assertEquals(2 + binary.length, binaryString.encodedLength());
    }

    @Test(expected = Mqtt5BinaryDataExceededException.class)
    public void encodedLength_too_long() {
        final byte[] binary = new byte[65_535 + 1];
        Arrays.fill(binary, (byte) 1);
        final Mqtt5UTF8StringImpl binaryString = Mqtt5UTF8StringImpl.from(binary);
        assertNotNull(binaryString);
        binaryString.encodedLength();
    }

    @Test
    @SuppressWarnings("all")
    public void equals() {
        assertTrue(Mqtt5UTF8StringImpl.from("test").equals(Mqtt5UTF8StringImpl.from("test")));
        assertTrue(Mqtt5UTF8StringImpl.from("test").equals(Mqtt5UTF8StringImpl.from(new byte[]{'t', 'e', 's', 't'})));
        assertTrue(Mqtt5UTF8StringImpl.from(new byte[]{'t', 'e', 's', 't'}).equals(Mqtt5UTF8StringImpl.from("test")));
        assertTrue(Mqtt5UTF8StringImpl.from(new byte[]{'t', 'e', 's', 't'})
                .equals(Mqtt5UTF8StringImpl.from(new byte[]{'t', 'e', 's', 't'})));

        assertFalse(Mqtt5UTF8StringImpl.from("test").equals(Mqtt5UTF8StringImpl.from("test2")));
        assertFalse(
                Mqtt5UTF8StringImpl.from("test").equals(Mqtt5UTF8StringImpl.from(new byte[]{'t', 'e', 's', 't', '2'})));
        assertFalse(Mqtt5UTF8StringImpl.from(new byte[]{'t', 'e', 's', 't'}).equals(Mqtt5UTF8StringImpl.from("test2")));
        assertFalse(Mqtt5UTF8StringImpl.from(new byte[]{'t', 'e', 's', 't'})
                .equals(Mqtt5UTF8StringImpl.from(new byte[]{'t', 'e', 's', 't', '2'})));

        assertFalse(Mqtt5UTF8StringImpl.from("test").equals(null));
        assertFalse(Mqtt5UTF8StringImpl.from(new byte[]{'t', 'e', 's', 't'}).equals(null));
    }

    @Test
    @SuppressWarnings("all")
    public void equals_same() {
        final Mqtt5UTF8StringImpl string = Mqtt5UTF8StringImpl.from("test");
        final Mqtt5UTF8StringImpl binary = Mqtt5UTF8StringImpl.from(new byte[]{'t', 'e', 's', 't'});

        assertTrue(string.equals(string));
        assertTrue(binary.equals(binary));
    }

    @Test
    @SuppressWarnings("all")
    public void equals_converted() {
        final Mqtt5UTF8StringImpl stringAndBinary = Mqtt5UTF8StringImpl.from("test");
        stringAndBinary.toBinary();

        assertTrue(stringAndBinary.equals(Mqtt5UTF8StringImpl.from("test")));
        assertTrue(stringAndBinary.equals(Mqtt5UTF8StringImpl.from(new byte[]{'t', 'e', 's', 't'})));

        assertTrue(Mqtt5UTF8StringImpl.from("test").equals(stringAndBinary));
        assertTrue(Mqtt5UTF8StringImpl.from(new byte[]{'t', 'e', 's', 't'}).equals(stringAndBinary));
    }

    @Test
    public void hashCode_same_as_string() {
        final Mqtt5UTF8StringImpl string = Mqtt5UTF8StringImpl.from("test");
        final Mqtt5UTF8StringImpl binary = Mqtt5UTF8StringImpl.from(new byte[]{'t', 'e', 's', 't'});
        assertNotNull(string);
        assertNotNull(binary);

        assertEquals("test".hashCode(), string.hashCode());
        assertEquals("test".hashCode(), binary.hashCode());
    }

}