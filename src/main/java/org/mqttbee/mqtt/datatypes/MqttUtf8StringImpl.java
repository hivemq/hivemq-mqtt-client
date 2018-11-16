/*
 * Copyright 2018 The MQTT Bee project
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
 *
 */

package org.mqttbee.mqtt.datatypes;

import com.google.common.base.Utf8;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.api.mqtt.datatypes.MqttUtf8String;

import javax.annotation.concurrent.Immutable;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * This class lazily en/decodes between UTF-8 and UTF-16 encoding, but performs validation upfront.
 *
 * @author Silvio Giebl
 * @see MqttUtf8String
 */
@Immutable
public class MqttUtf8StringImpl implements MqttUtf8String {

    /**
     * MQTT Protocol name as a UTF-8 encoded String.
     */
    public static final @NotNull MqttUtf8StringImpl PROTOCOL_NAME =
            new MqttUtf8StringImpl("MQTT".getBytes(StandardCharsets.UTF_8));

    /**
     * Validates and decodes a UTF-8 encoded String from the given byte array.
     * <p>
     * Note: the given byte array must not be longer than {@link MqttBinaryData#MAX_LENGTH}.
     *
     * @param binary the byte array with the UTF-8 encoded data to decode from.
     * @return the created UTF-8 encoded String or null if the byte array does not contain a well-formed UTF-8 encoded
     *         String.
     */
    public static @Nullable MqttUtf8StringImpl from(final @NotNull byte[] binary) {
        return (!MqttBinaryData.isInRange(binary) || containsMustNotCharacters(binary)) ? null :
                new MqttUtf8StringImpl(binary);
    }

    /**
     * Validates and creates a UTF-8 encoded String from the given string.
     * <p>
     * Note: The given string
     * <ul>encoded in UTF-8 must not be longer than {@link MqttBinaryData#MAX_LENGTH}</ul>
     * <ul>and must not contain the null character U+0000 and UTF-16 surrogates, as these are forbidden according to
     * the MQTT 5 Specification.</ul>
     *
     * @param string the UTF-16 encoded Java string.
     * @return the created UTF-8 encoded String or null if the string is not a valid UTF-8 encoded String.
     * @throws IllegalArgumentException if the given string encoded in UTF-8 is longer than {@link
     *                                  MqttBinaryData#MAX_LENGTH} or contains forbidden characters.
     */
    public static @NotNull MqttUtf8StringImpl from(final @NotNull String string) {
        return from(string, "UTF-8 encoded string");
    }

    public static @NotNull MqttUtf8StringImpl from(final @NotNull String string, final @NotNull String name) {
        checkLength(string, name);
        checkForbiddenCharacters(string, name);
        return new MqttUtf8StringImpl(string);
    }

    /**
     * Validates and decodes a UTF-8 encoded String from the given byte buffer at the current reader index.
     * <p>
     * In case of a wrong encoding the reader index of the byte buffer will be in an undefined state after the method
     * returns.
     * <p>
     * Note: the first two bytes are interpreted as the length of the binary data to read. Thus the length is limited to
     * {@link MqttBinaryData#MAX_LENGTH}.
     *
     * @param byteBuf the byte buffer with the UTF-8 encoded data to decode from.
     * @return the created UTF-8 encoded String or null if the byte buffer does not contain a well-formed UTF-8 encoded
     *         String.
     */
    public static @Nullable MqttUtf8StringImpl from(final @NotNull ByteBuf byteBuf) {
        final byte[] binary = MqttBinaryData.decode(byteBuf);
        return (binary == null) ? null : from(binary);
    }

    /**
     * Checks whether the given UTF-8 encoded byte array contains characters a UTF-8 encoded String must not according
     * to the MQTT 5 specification.
     * <p>
     * These characters are the null character U+0000 and UTF-16 surrogates.
     *
     * @param binary the UTF-8 encoded byte array.
     * @return whether the binary data contains characters a UTF-8 encoded String must not.
     */
    static boolean containsMustNotCharacters(final @NotNull byte[] binary) {
        if (!Utf8.isWellFormed(binary)) {
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
     * Checks whether the given UTF-16 encoded Java string contains the null character U+0000 or unmatched UTF-16
     * surrogates (U+D800 to U+DFFF), as these are forbidden in the UTF-8 encoded string according to the MQTT 5
     * specification.
     *
     * @param string the UTF-16 encoded Java string.
     * @throws IllegalArgumentException if the given string contains forbidden characters.
     */
    static void checkForbiddenCharacters(final @NotNull String string, final @NotNull String name) {
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
     * Checks if the given UTF-16 Java string encoded in UTF-8 fits into {@link MqttBinaryData}.
     *
     * @param string the UTF-16 encoded Java string.
     * @throws IllegalArgumentException if the given string encoded in UTF-8 is longer than {@link
     *                                  MqttBinaryData#MAX_LENGTH}.
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
            final int utf8Length = Utf8.encodedLength(string);
            if (utf8Length > MqttBinaryData.MAX_LENGTH) {
                throw new IllegalArgumentException(
                        name + " [" + string.substring(0, 10) + "...] must not be longer than " +
                                MqttBinaryData.MAX_LENGTH + " bytes, but was " + utf8Length + " bytes.");
            }
        }
    }

    private @Nullable byte[] binary;
    private @Nullable String string;
    private int conversions;

    MqttUtf8StringImpl(final @NotNull byte[] binary) {
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
    @NotNull byte[] toBinary() {
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
     * Encodes this UTF-8 encoded String to the given byte buffer at the current writer index according to the MQTT 5
     * specification. Converts from the UTF-16 encoded to the UTF-8 encoded representation if necessary.
     * <p>
     * This method does not check if this UTF-8 encoded String can not be encoded due to byte count restrictions. This
     * check is performed with the method {@link #encodedLength()} which is generally called before this method.
     *
     * @param byteBuf the byte buffer to encode to.
     */
    public void to(final @NotNull ByteBuf byteBuf) {
        MqttBinaryData.encode(toBinary(), byteBuf);
    }

    /**
     * Calculates the byte count of this UTF-8 encoded String according to the MQTT 5 specification.
     *
     * @return the encoded length of this UTF-8 encoded String.
     */
    public int encodedLength() {
        return MqttBinaryData.encodedLength(toBinary());
    }

    @Override
    public boolean equals(final Object o) {
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
