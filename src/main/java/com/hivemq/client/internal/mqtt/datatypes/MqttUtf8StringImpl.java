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

package com.hivemq.client.internal.mqtt.datatypes;

import com.hivemq.client.internal.util.Checks;
import com.hivemq.client.internal.util.Utf8Util;
import com.hivemq.client.mqtt.datatypes.MqttUtf8String;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * This class lazily en/decodes between UTF-8 and UTF-16 encoding, but performs validation upfront.
 *
 * @author Silvio Giebl
 * @see MqttUtf8String
 */
@Unmodifiable
public class MqttUtf8StringImpl implements MqttUtf8String {

    /**
     * MQTT protocol name as a UTF-8 encoded string.
     */
    public static final @NotNull MqttUtf8StringImpl PROTOCOL_NAME =
            new MqttUtf8StringImpl("MQTT".getBytes(StandardCharsets.UTF_8));

    /**
     * Validates and creates an UTF-8 encoded string of the given UTF-16 encoded Java string.
     * <p>
     * The given string
     * <ul>
     * <li>must not be longer than {@value MqttBinaryData#MAX_LENGTH} bytes in UTF-8 encoding,</li>
     * <li>must not contain the null character (U+0000) and</li>
     * <li>must not contain unmatched UTF-16 surrogates.</li>
     * </ul>
     *
     * @param string the UTF-16 encoded Java string.
     * @return the created UTF-8 encoded string.
     * @throws IllegalArgumentException if the string is not a valid UTF-8 encoded string.
     */
    @Contract("null -> fail")
    public static @NotNull MqttUtf8StringImpl of(final @Nullable String string) {
        return of(string, "UTF-8 encoded string");
    }

    /**
     * Same function as {@link #of(String)}, but allows specifying a name to use in error messages.
     *
     * @param string see {@link #of(String)}.
     * @param name   specific name used in error messages.
     * @return see {@link #of(String)}.
     * @throws IllegalArgumentException see {@link #of(String)}.
     * @see #of(String)
     */
    @Contract("null, _ -> fail")
    public static @NotNull MqttUtf8StringImpl of(final @Nullable String string, final @NotNull String name) {
        Checks.notNull(string, name);
        checkLength(string, name);
        checkWellFormed(string, name);
        return new MqttUtf8StringImpl(string);
    }

    /**
     * Validates and creates a UTF-8 encoded string of the given byte array with UTF-8 encoded data.
     * <p>
     * The given byte array
     * <ul>
     * <li>must not be longer than {@value MqttBinaryData#MAX_LENGTH},</li>
     * <li>must not contain the null character (U+0000) and</li>
     * <li>must be well-formed UTF-8 as defined by the Unicode specification, so
     * <ul>
     * <li>must not contain encodings of UTF-16 surrogates (U+D800..U+DFFF) and</li>
     * <li>must not contain non-shortest form encodings.</li>
     * </ul>
     * </li>
     * </ul>
     *
     * @param binary the byte array with the UTF-8 encoded data.
     * @return the created UTF-8 encoded string or <code>null</code> if the byte array does not represent a valid UTF-8
     *         encoded String.
     */
    public static @Nullable MqttUtf8StringImpl of(final byte @NotNull [] binary) {
        return (!MqttBinaryData.isInRange(binary) || isWellFormed(binary)) ? null : new MqttUtf8StringImpl(binary);
    }

    /**
     * Validates and decodes a UTF-8 encoded string from the given byte buffer at the current reader index.
     * <p>
     * In case of a wrong encoding the reader index of the byte buffer will be in an undefined state after the method
     * returns.
     * <p>
     * Note: the first two bytes are interpreted as the length of the binary data to read. Thus the length is limited to
     * {@value MqttBinaryData#MAX_LENGTH}.
     *
     * @param byteBuf the byte buffer with the UTF-8 encoded data to decode from.
     * @return the created UTF-8 encoded string or <code>null</code> if the byte buffer does not contain a valid UTF-8
     *         encoded string.
     */
    public static @Nullable MqttUtf8StringImpl decode(final @NotNull ByteBuf byteBuf) {
        final byte[] binary = MqttBinaryData.decode(byteBuf);
        return (binary == null) ? null : of(binary);
    }

    /**
     * Checks if the given byte array with UTF-8 encoded data represents a well-formed UTF-8 encoded string according to
     * the MQTT specification, so
     * <ul>
     * <li>must not contain the null character (U+0000) and</li>
     * <li>must be well-formed UTF-8 as defined by the Unicode specification, so
     * <ul>
     * <li>must not contain encodings of UTF-16 surrogates (U+D800..U+DFFF) and</li>
     * <li>must not contain non-shortest form encodings.</li>
     * </ul>
     * </li>
     * </ul>
     *
     * @param binary the byte array with UTF-8 encoded data.
     * @return whether the byte array represents a well-formed UTF-8 encoded string.
     */
    static boolean isWellFormed(final byte @NotNull [] binary) {
        if (Utf8Util.isWellFormed(binary) != 0) {
            return true;
        }
        for (final byte b : binary) {
            if (b == 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the given UTF-16 encoded Java string is a well-formed UTF-8 encoded string according to the MQTT
     * specification, so
     * <ul>
     * <li>must not contain the null character (U+0000) and</li>
     * <li>must not contain unmatched UTF-16 surrogates.</li>
     * </ul>
     *
     * @param string the UTF-16 encoded Java string.
     * @param name   specific name used in error messages.
     * @throws IllegalArgumentException if the string is not a well-formed UTF-8 encoded string.
     */
    static void checkWellFormed(final @NotNull String string, final @NotNull String name) {
        boolean previousCharIsHighSurrogate = false;
        for (int i = 0; i < string.length(); i++) {
            final char c = string.charAt(i);
            if (c == 0) {
                throw new IllegalArgumentException(
                        name + " [" + string + "] must not contain null character (U+0000), found at index " + i + ".");
            }
            if (previousCharIsHighSurrogate != Character.isLowSurrogate(c)) {
                throw new IllegalArgumentException(
                        name + " [" + string + "] must not contain unmatched UTF-16 surrogate, found at index " + i +
                                ".");
            }
            previousCharIsHighSurrogate = Character.isHighSurrogate(c);
        }
        if (previousCharIsHighSurrogate) {
            throw new IllegalArgumentException(
                    name + " [" + string + "] must not contain unmatched UTF-16 surrogate, found at index " +
                            (string.length() - 1) + ".");
        }
    }

    /**
     * Checks if the given UTF-16 encoded Java string is not longer than {@value MqttBinaryData#MAX_LENGTH} bytes in
     * UTF-8 encoding.
     *
     * @param string the UTF-16 encoded Java string.
     * @param name   specific name used in error messages.
     * @throws IllegalArgumentException if the string is longer than {@value MqttBinaryData#MAX_LENGTH} bytes in UTF-8
     *                                  encoding.
     */
    static void checkLength(final @NotNull String string, final @NotNull String name) {
        // Perform a quick check on string length, since calculating the exact number of bytes used in UTF-8 encoding
        // might be an expensive operation.
        // Note: Java strings are represented in UTF-16 (fixed-width 16-bit chars for code points U+0000 to U+FFFF).
        //       Supplementary characters (code points greater than U+FFFF) are represented as a pair of char values
        //       from the high-surrogate and low-surrogate ranges, hence their representation needs 4 bytes.
        //       The UTF-8 representation uses a variable number of 1 to 3 bytes to represent code points from U+0000 to
        //       U+FFFF. Representation of supplementary characters in UTF-8 also needs 4 bytes.
        //       String.length() returns the number of 16-bit char values.
        //       Hence, string.length() * 3 is an upper bound of the number of bytes needed to represent a Java string
        //       (UTF-16) in UTF-8.
        if (string.length() * 3 > MqttBinaryData.MAX_LENGTH) {
            final int utf8Length = Utf8Util.encodedLength(string);
            if (utf8Length > MqttBinaryData.MAX_LENGTH) {
                throw new IllegalArgumentException(
                        name + " [" + string.substring(0, 10) + "...] must not be longer than " +
                                MqttBinaryData.MAX_LENGTH + " bytes, but was " + utf8Length + " bytes.");
            }
        }
    }

    private byte @Nullable [] binary;
    private @Nullable String string;
    private int conversions;

    MqttUtf8StringImpl(final byte @NotNull [] binary) {
        this.binary = binary;
    }

    MqttUtf8StringImpl(final @NotNull String string) {
        this.string = string;
    }

    @Override
    public boolean containsShouldNotCharacters() {
        final String string = toString();
        boolean highSurrogate = false;
        for (int i = 0; i < string.length(); i++) {
            final char c = string.charAt(i);
            if (Character.isISOControl(c)) { // control characters
                return true;
            }
            if (!highSurrogate) {
                if ((c >= '\uD83F')) {
                    if ((c >= '\uFDD0') && (c <= '\uFDEF')) { // non characters
                        return true;
                    }
                    if ((c & 0xFFFE) == 0xFFFE) { // U+FFFE|F
                        return true;
                    }
                    if ((c == '\uD83F') || //       U+1FFFE|F
                            (c == '\uD87F') || //   U+2FFFE|F
                            (c == '\uD8BF') || //   U+3FFFE|F
                            (c == '\uD8FF') || //   U+4FFFE|F
                            (c == '\uD93F') || //   U+5FFFE|F
                            (c == '\uD97F') || //   U+6FFFE|F
                            (c == '\uD9BF') || //   U+7FFFE|F
                            (c == '\uD9FF') || //   U+8FFFE|F
                            (c == '\uDA3F') || //   U+9FFFE|F
                            (c == '\uDA7F') || //   U+AFFFE|F
                            (c == '\uDABF') || //   U+BFFFE|F
                            (c == '\uDAFF') || //   U+CFFFE|F
                            (c == '\uDB3F') || //   U+DFFFE|F
                            (c == '\uDB7F') || //   U+EFFFE|F
                            (c == '\uDBBF') || //   U+FFFFE|F
                            (c == '\uDBFF')) { //  U+10FFFE|F
                        highSurrogate = true;
                    }
                }
            } else {
                if ((c & 0xDFFE) == 0xDFFE) { // U+DFFE|F low surrogate for U+...FFFE|F
                    return true;
                }
                highSurrogate = false;
            }
        }
        return false;
    }

    @Override
    public @NotNull ByteBuffer toByteBuffer() {
        return ByteBuffer.wrap(toBinary()).asReadOnlyBuffer();
    }

    /**
     * Returns the UTF-8 encoded representation as a byte array. Converts from the UTF-16 encoded representation if
     * necessary.
     *
     * @return the UTF-8 encoded byte array.
     */
    byte @NotNull [] toBinary() {
        byte[] binary = this.binary;
        if (binary == null) {
            final String string = this.string;
            if (string == null) {
                return toBinary();
            }
            binary = string.getBytes(StandardCharsets.UTF_8);
            this.binary = binary;
            conversions++;
            if (conversions < 3) {
                this.string = null;
            }
        }
        return binary;
    }

    /**
     * Returns the UTF-16 encoded representation as a Java string. Converts from the UTF-8 encoded representation if
     * necessary.
     *
     * @return the UTF-16 encoded string.
     */
    @Override
    public @NotNull String toString() {
        String string = this.string;
        if (string == null) {
            final byte[] binary = this.binary;
            if (binary == null) {
                return toString();
            }
            string = new String(binary, StandardCharsets.UTF_8);
            this.string = string;
            conversions++;
            if (conversions < 3) {
                this.binary = null;
            }
        }
        return string;
    }

    /**
     * Encodes this UTF-8 encoded string to the given byte buffer at the current writer index according to the MQTT
     * specification.
     * <p>
     * Converts from the UTF-16 encoded to the UTF-8 encoded representation if necessary.
     *
     * @param byteBuf the byte buffer to encode to.
     */
    public void encode(final @NotNull ByteBuf byteBuf) {
        MqttBinaryData.encode(toBinary(), byteBuf);
    }

    /**
     * Calculates the byte count of this UTF-8 encoded string according to the MQTT specification.
     * <p>
     * Converts from the UTF-16 encoded to the UTF-8 encoded representation if necessary.
     *
     * @return the encoded length of this UTF-8 encoded string.
     */
    public int encodedLength() {
        return MqttBinaryData.encodedLength(toBinary());
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MqttUtf8StringImpl)) {
            return false;
        }
        final MqttUtf8StringImpl that = (MqttUtf8StringImpl) o;
        final String string = this.string;
        final String thatString = that.string;
        if ((string != null) && (thatString != null)) {
            return string.equals(thatString);
        }
        final byte[] binary = this.binary;
        final byte[] thatBinary = that.binary;
        if ((binary != null) && (thatBinary != null)) {
            return Arrays.equals(binary, thatBinary);
        }
        return toString().equals(that.toString());
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public int compareTo(final @NotNull MqttUtf8String that) {
        return toString().compareTo(that.toString());
    }
}
