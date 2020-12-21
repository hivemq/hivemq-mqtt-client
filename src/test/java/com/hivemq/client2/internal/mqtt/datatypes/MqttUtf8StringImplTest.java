/*
 * Copyright 2018-present HiveMQ and the HiveMQ Community
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hivemq.client2.internal.mqtt.datatypes;

import com.google.common.base.Utf8;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Silvio Giebl
 */
class MqttUtf8StringImplTest {

    private static @NotNull Iterable<Arguments> stringWithGivenLengthProvider(final int length) {
        final List<Arguments> arguments = new ArrayList<>();

        final char[] chars = new char[length];
        Arrays.fill(chars, 'a'); // each 'a' encoded in UTF-8 is one byte
        final String maxStringOfOneByteChars = new String(chars);

        // only characters with a one byte representation in UTF-8, i.e. U+0000 to U+007F
        arguments.add(Arguments.of(maxStringOfOneByteChars, 2 + Utf8.encodedLength(maxStringOfOneByteChars)));

        // include character with two byte representation in UTF-8, i.e. U+0080 to U+07FF
        final String maxStringIncludingTwoByteChar = '\u0080' + maxStringOfOneByteChars.substring(2);
        arguments.add(
                Arguments.of(maxStringIncludingTwoByteChar, 2 + Utf8.encodedLength(maxStringIncludingTwoByteChar)));

        // include character with three byte representation in UTF-8, i.e. U+0800 to U+FFFF
        final String maxStringIncludingThreeByteChar = '\u0800' + maxStringOfOneByteChars.substring(3);
        arguments.add(
                Arguments.of(maxStringIncludingThreeByteChar, 2 + Utf8.encodedLength(maxStringIncludingThreeByteChar)));
        return arguments;
    }

    private static Iterable<Arguments> tooLongStringProvider() {
        return stringWithGivenLengthProvider(MqttBinaryData.MAX_LENGTH + 1);
    }

    @ParameterizedTest(name = "{index}: length={1}, string={0}")
    @MethodSource("tooLongStringProvider")
    void from_stringTooLong_throws(final @NotNull String tooLong, final int expectedLength) {
        final IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> MqttUtf8StringImpl.of(tooLong));
        assertTrue(
                exception.getMessage().contains("must not be longer than"),
                "IllegalArgumentException must give hint that string encoded in UTF-8 exceeds binary limit.");
        assertTrue(
                exception.getMessage().contains(Integer.toString(expectedLength - 2)),
                "IllegalArgumentException contains actual length.");
    }

    @Test
    void from_stringWithNullCharacter_throws() {
        final IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> MqttUtf8StringImpl.of("abc\0def"));
        assertTrue(
                exception.getMessage().contains("null character (U+0000)"),
                "IllegalArgumentException must give hint that string contains a forbidden null character.");
    }

    @ParameterizedTest
    @CsvSource({"abc, def", "abc, ''", "'' ,def", "'', ''"})
    void from_stringWithUtf16Surrogates_throws(final @NotNull String prefix, final @NotNull String postfix) {
        for (char c = '\uD800'; c <= '\uDFFF'; c++) {
            final String stringWithSurrogate = prefix + c + postfix;
            final IllegalArgumentException exception =
                    assertThrows(IllegalArgumentException.class, () -> MqttUtf8StringImpl.of(stringWithSurrogate));
            assertTrue(
                    exception.getMessage().contains("UTF-16 surrogate"),
                    "IllegalArgumentException must give hint that string contains a forbidden UTF-16 surrogate.");
        }
    }

    @Test
    void from_bytesTooLong_returnsNull() {
        final byte[] binary = new byte[MqttBinaryData.MAX_LENGTH + 1];
        Arrays.fill(binary, (byte) 1);
        final MqttUtf8StringImpl binaryString = MqttUtf8StringImpl.of(binary);
        assertNull(binaryString);
    }

    @Test
    void from_bytesWithNullCharacter_returnsNull() {
        assertNull(MqttUtf8StringImpl.of(new byte[]{'a', 'b', 'c', '\0', 'd', 'e', 'f'}));
    }

    @Test
    void from_bytesWithUtf16Surrogates_returnsNull() {
        for (int b = 0xA0; b <= 0xBF; b++) {
            for (int b2 = 0; b2 < 0xFF; b2++) {
                assertNull(MqttUtf8StringImpl.of(
                        new byte[]{'a', 'b', 'c', (byte) 0xED, (byte) b, (byte) b2, 'd', 'e', 'f'}));
            }
        }
    }

    @Test
    void containsShouldNotCharacters_stringWithControlCharacters() {
        for (char c = '\u0001'; c <= '\u001F'; c++) {
            final MqttUtf8StringImpl string = MqttUtf8StringImpl.of("abc" + c + "def");
            assertNotNull(string);
            assertTrue(string.containsShouldNotCharacters());

            final MqttUtf8StringImpl binary = MqttUtf8StringImpl.of(new byte[]{'a', 'b', 'c', (byte) c, 'd', 'e', 'f'});
            assertNotNull(binary);
            assertTrue(binary.containsShouldNotCharacters());
        }
        for (char c = '\u007F'; c <= '\u009F'; c++) {
            final MqttUtf8StringImpl string = MqttUtf8StringImpl.of("abc" + c + "def");
            assertNotNull(string);
            assertTrue(string.containsShouldNotCharacters());
        }

        {
            final MqttUtf8StringImpl binary = MqttUtf8StringImpl.of(new byte[]{'a', 'b', 'c', 0x7F, 'd', 'e', 'f'});
            assertNotNull(binary);
            assertTrue(binary.containsShouldNotCharacters());
        }
        for (int b = 0x80; b <= 0x9F; b++) {
            final MqttUtf8StringImpl binary =
                    MqttUtf8StringImpl.of(new byte[]{'a', 'b', 'c', (byte) 0xC2, (byte) b, 'd', 'e', 'f'});
            assertNotNull(binary);
            assertTrue(binary.containsShouldNotCharacters());
        }
    }

    @Test
    void containsShouldNotCharacters_stringWithNonCharacters() {
        for (int c = 0xFFFE; c <= 0x10_FFFE; c += 0x1_0000) {
            for (int i = 0; i < 2; i++) {
                final String nonCharacterString = String.valueOf(Character.toChars(c + i));
                final MqttUtf8StringImpl string = MqttUtf8StringImpl.of("abc" + nonCharacterString + "def");
                assertNotNull(string);
                assertTrue(string.containsShouldNotCharacters());

                final byte[] nonCharacterBinary = nonCharacterString.getBytes(StandardCharsets.UTF_8);
                final byte[] binary = new byte[6 + nonCharacterBinary.length];
                binary[0] = 'a';
                binary[1] = 'b';
                binary[2] = 'c';
                System.arraycopy(nonCharacterBinary, 0, binary, 3, nonCharacterBinary.length);
                binary[3 + nonCharacterBinary.length] = 'd';
                binary[3 + nonCharacterBinary.length + 1] = 'e';
                binary[3 + nonCharacterBinary.length + 2] = 'f';
                final MqttUtf8StringImpl binaryString = MqttUtf8StringImpl.of(binary);
                assertNotNull(binaryString);
                assertTrue(binaryString.containsShouldNotCharacters());
            }
        }
        for (int c = 0xFDD0; c <= 0xFDEF; c++) {
            final String nonCharacterString = String.valueOf(Character.toChars(c));
            final MqttUtf8StringImpl string = MqttUtf8StringImpl.of("abc" + nonCharacterString + "def");
            assertNotNull(string);
            assertTrue(string.containsShouldNotCharacters());

            final byte[] nonCharacterBinary = nonCharacterString.getBytes(StandardCharsets.UTF_8);
            final byte[] binary = new byte[6 + nonCharacterBinary.length];
            binary[0] = 'a';
            binary[1] = 'b';
            binary[2] = 'c';
            System.arraycopy(nonCharacterBinary, 0, binary, 3, nonCharacterBinary.length);
            binary[3 + nonCharacterBinary.length] = 'd';
            binary[3 + nonCharacterBinary.length + 1] = 'e';
            binary[3 + nonCharacterBinary.length + 2] = 'f';
            final MqttUtf8StringImpl binaryString = MqttUtf8StringImpl.of(binary);
            assertNotNull(binaryString);
            assertTrue(binaryString.containsShouldNotCharacters());
        }
    }

    @Test
    void containsShouldNotCharacters_stringWithoutControlAndNonCharacters() {
        final MqttUtf8StringImpl string = MqttUtf8StringImpl.of("abcdef");
        assertNotNull(string);
        assertFalse(string.containsShouldNotCharacters());

        final MqttUtf8StringImpl binary = MqttUtf8StringImpl.of(new byte[]{'a', 'b', 'c', 'd', 'e', 'f'});
        assertNotNull(binary);
        assertFalse(binary.containsShouldNotCharacters());
    }

    @Test
    void from_stringWithZeroWidthNoBreakSpaceCharacter() {
        final MqttUtf8StringImpl string = MqttUtf8StringImpl.of("abc" + '\uFEFF' + "def");
        assertNotNull(string);
        assertTrue(string.toString().contains("\uFEFF"));

        final byte[] binary = new byte[]{'a', 'b', 'c', (byte) 0xEF, (byte) 0xBB, (byte) 0xBF, 'd', 'e', 'f'};
        final MqttUtf8StringImpl binaryString = MqttUtf8StringImpl.of(binary);
        assertNotNull(binaryString);
        assertArrayEquals(binaryString.toBinary(), binary);
        assertTrue(binaryString.toString().contains("\uFEFF"));
    }

    @Test
    void from_byteBuf() {
        final String string = "abcdef";

        final ByteBuf byteBuf = Unpooled.buffer();
        MqttBinaryData.encode(string.getBytes(StandardCharsets.UTF_8), byteBuf);
        final MqttUtf8StringImpl mqtt5UTF8String = MqttUtf8StringImpl.decode(byteBuf);
        byteBuf.release();

        assertNotNull(mqtt5UTF8String);
        assertEquals(string, mqtt5UTF8String.toString());
    }

    @Test
    void to_byteBuf() {
        final String string = "abcdef";
        final byte[] expected = {0, 6, 'a', 'b', 'c', 'd', 'e', 'f'};

        final MqttUtf8StringImpl mqtt5UTF8String = MqttUtf8StringImpl.of(string);
        assertNotNull(mqtt5UTF8String);

        final ByteBuf byteBuf = Unpooled.buffer();
        mqtt5UTF8String.encode(byteBuf);
        final byte[] actual = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(actual);
        byteBuf.release();

        assertArrayEquals(expected, actual);
    }

    @Test
    void toBinary_specificationExample() {
        final String string = "A\uD869\uDED4";
        final byte[] expected = {0x41, (byte) 0xF0, (byte) 0xAA, (byte) 0x9B, (byte) 0x94};
        final MqttUtf8StringImpl mqtt5UTF8String = MqttUtf8StringImpl.of(string);
        assertNotNull(mqtt5UTF8String);
        assertArrayEquals(expected, mqtt5UTF8String.toBinary());
        assertEquals(2 + mqtt5UTF8String.toBinary().length, mqtt5UTF8String.encodedLength());
    }

    @Test
    void encodedLength_fromBytes() {
        final byte[] binary = new byte[]{'a', 'b', 'c', 'd', 'e', 'f'};
        final MqttUtf8StringImpl binaryString = MqttUtf8StringImpl.of(binary);
        assertNotNull(binaryString);
        assertEquals(2 + binary.length, binaryString.encodedLength());
    }

    @Test
    void encodedLength_fromString() {
        final String simple = "abcdef";
        final int expectedLength = 2 + Utf8.encodedLength(simple);
        final MqttUtf8StringImpl string = MqttUtf8StringImpl.of(simple);
        assertNotNull(string);
        assertEquals(expectedLength, string.encodedLength());

    }

    private static Iterable<Arguments> stringWithMaxLengthProvider() {
        return stringWithGivenLengthProvider(MqttBinaryData.MAX_LENGTH);
    }

    @ParameterizedTest(name = "{index}: length={1}, string={0}")
    @MethodSource("stringWithMaxLengthProvider")
    void encodedLength_fromStringWithMaxLength(final @NotNull String string, final int expectedLength) {
        final MqttUtf8StringImpl utf8String = MqttUtf8StringImpl.of(string);
        assertNotNull(utf8String);
        assertEquals(expectedLength, utf8String.encodedLength());
    }

    @Test
    @SuppressWarnings({"SimplifiableJUnitAssertion", "ConstantConditions"})
    void equals() {
        assertTrue(MqttUtf8StringImpl.of("test").equals(MqttUtf8StringImpl.of("test")));
        assertTrue(MqttUtf8StringImpl.of("test").equals(MqttUtf8StringImpl.of(new byte[]{'t', 'e', 's', 't'})));
        assertTrue(MqttUtf8StringImpl.of(new byte[]{'t', 'e', 's', 't'}).equals(MqttUtf8StringImpl.of("test")));
        assertTrue(MqttUtf8StringImpl.of(new byte[]{'t', 'e', 's', 't'})
                .equals(MqttUtf8StringImpl.of(new byte[]{'t', 'e', 's', 't'})));

        assertFalse(MqttUtf8StringImpl.of("test").equals(MqttUtf8StringImpl.of("test2")));
        assertFalse(MqttUtf8StringImpl.of("test").equals(MqttUtf8StringImpl.of(new byte[]{'t', 'e', 's', 't', '2'})));
        assertFalse(MqttUtf8StringImpl.of(new byte[]{'t', 'e', 's', 't'}).equals(MqttUtf8StringImpl.of("test2")));
        assertFalse(MqttUtf8StringImpl.of(new byte[]{'t', 'e', 's', 't'})
                .equals(MqttUtf8StringImpl.of(new byte[]{'t', 'e', 's', 't', '2'})));

        assertFalse(MqttUtf8StringImpl.of("test").equals(null));
        assertFalse(MqttUtf8StringImpl.of(new byte[]{'t', 'e', 's', 't'}).equals(null));
    }

    @Test
    @SuppressWarnings({"ConstantConditions"})
    void compareTo() {
        assertEquals(0, MqttUtf8StringImpl.of("test").compareTo(MqttUtf8StringImpl.of("test")));
        assertEquals(0, MqttUtf8StringImpl.of("test").compareTo(MqttUtf8StringImpl.of(new byte[]{'t', 'e', 's', 't'})));
        assertEquals(0, MqttUtf8StringImpl.of(new byte[]{'t', 'e', 's', 't'}).compareTo(MqttUtf8StringImpl.of("test")));
        assertEquals(0, MqttUtf8StringImpl.of(new byte[]{'t', 'e', 's', 't'})
                .compareTo(MqttUtf8StringImpl.of(new byte[]{'t', 'e', 's', 't'})));

        assertTrue(MqttUtf8StringImpl.of("test").compareTo(MqttUtf8StringImpl.of("test2")) < 0);
        assertTrue(MqttUtf8StringImpl.of("test2").compareTo(MqttUtf8StringImpl.of("test")) > 0);
        assertTrue(MqttUtf8StringImpl.of("test").compareTo(MqttUtf8StringImpl.of(new byte[]{'t', 'e', 's', 't', '2'})) <
                0);
        assertTrue(MqttUtf8StringImpl.of("test2").compareTo(MqttUtf8StringImpl.of(new byte[]{'t', 'e', 's', 't'})) > 0);
        assertTrue(MqttUtf8StringImpl.of(new byte[]{'t', 'e', 's', 't'}).compareTo(MqttUtf8StringImpl.of("test2")) < 0);
        assertTrue(MqttUtf8StringImpl.of(new byte[]{'t', 'e', 's', 't', '2'}).compareTo(MqttUtf8StringImpl.of("test")) >
                0);
        assertTrue(MqttUtf8StringImpl.of(new byte[]{'t', 'e', 's', 't'})
                .compareTo(MqttUtf8StringImpl.of(new byte[]{'t', 'e', 's', 't', '2'})) < 0);
        assertTrue(MqttUtf8StringImpl.of(new byte[]{'t', 'e', 's', 't', '2'})
                .compareTo(MqttUtf8StringImpl.of(new byte[]{'t', 'e', 's', 't'})) > 0);
    }

    @Test
    @SuppressWarnings({"SimplifiableJUnitAssertion", "ConstantConditions", "EqualsWithItself"})
    void equals_same() {
        final MqttUtf8StringImpl string = MqttUtf8StringImpl.of("test");
        final MqttUtf8StringImpl binary = MqttUtf8StringImpl.of(new byte[]{'t', 'e', 's', 't'});

        assertTrue(string.equals(string));
        assertTrue(binary.equals(binary));
    }

    @Test
    @SuppressWarnings({"ConstantConditions", "EqualsWithItself"})
    void compareTo_same() {
        final MqttUtf8StringImpl string = MqttUtf8StringImpl.of("test");
        final MqttUtf8StringImpl binary = MqttUtf8StringImpl.of(new byte[]{'t', 'e', 's', 't'});

        assertEquals(0, string.compareTo(string));
        assertEquals(0, binary.compareTo(binary));
    }

    @Test
    @SuppressWarnings({"SimplifiableJUnitAssertion", "ConstantConditions"})
    void equals_converted() {
        final MqttUtf8StringImpl stringAndBinary = MqttUtf8StringImpl.of("test");
        stringAndBinary.toBinary();

        assertTrue(stringAndBinary.equals(MqttUtf8StringImpl.of("test")));
        assertTrue(stringAndBinary.equals(MqttUtf8StringImpl.of(new byte[]{'t', 'e', 's', 't'})));

        assertTrue(MqttUtf8StringImpl.of("test").equals(stringAndBinary));
        assertTrue(MqttUtf8StringImpl.of(new byte[]{'t', 'e', 's', 't'}).equals(stringAndBinary));
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    void compareTo_converted() {
        final MqttUtf8StringImpl stringAndBinary = MqttUtf8StringImpl.of("test");
        stringAndBinary.toBinary();

        assertEquals(0, stringAndBinary.compareTo(MqttUtf8StringImpl.of("test")));
        assertEquals(0, stringAndBinary.compareTo(MqttUtf8StringImpl.of(new byte[]{'t', 'e', 's', 't'})));

        assertEquals(0, MqttUtf8StringImpl.of("test").compareTo(stringAndBinary));
        assertEquals(0, MqttUtf8StringImpl.of(new byte[]{'t', 'e', 's', 't'}).compareTo(stringAndBinary));
    }

    @Test
    void hashCode_sameAsString() {
        final MqttUtf8StringImpl string = MqttUtf8StringImpl.of("test");
        final MqttUtf8StringImpl binary = MqttUtf8StringImpl.of(new byte[]{'t', 'e', 's', 't'});
        assertNotNull(string);
        assertNotNull(binary);

        assertEquals("test".hashCode(), string.hashCode());
        assertEquals("test".hashCode(), binary.hashCode());
    }
}