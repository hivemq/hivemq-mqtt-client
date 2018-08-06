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

import com.google.common.base.Preconditions;
import com.google.common.base.Utf8;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.api.mqtt.datatypes.MqttUtf8String;
import org.mqttbee.api.mqtt.exceptions.MqttBinaryDataExceededException;
import org.mqttbee.util.Checks;

import javax.annotation.concurrent.Immutable;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * This class lazily en/decodes between UTF-8 and UTF-16 encoding, but performs validation upfront.
 *
 * @author Silvio Giebl
 * @see MqttUtf8String
 */
@Immutable
public class MqttUtf8StringImpl implements MqttUtf8String {

    private static final Charset CHARSET = Charset.forName("UTF-8");
    private static final Pattern SHOULD_NOT_CHARACTERS_PATTERN =
            Pattern.compile("[\\u0001-\\u001F]|[\\u007F-\\u009F]" + // control characters
                    "|[\\uFDD0-\\uFDEF]" +              // non characters
                    "|\\uFFFE|\\uFFFF" +                //   U+FFFE|F
                    "|\\uD83F\\uDFFE|\\uD83F\\uDFFF" +  //  U+1FFFE|F
                    "|\\uD87F\\uDFFE|\\uD87F\\uDFFF" +  //  U+2FFFE|F
                    "|\\uD8BF\\uDFFE|\\uD8BF\\uDFFF" +  //  U+3FFFE|F
                    "|\\uD8FF\\uDFFE|\\uD8FF\\uDFFF" +  //  U+4FFFE|F
                    "|\\uD93F\\uDFFE|\\uD93F\\uDFFF" +  //  U+5FFFE|F
                    "|\\uD97F\\uDFFE|\\uD97F\\uDFFF" +  //  U+6FFFE|F
                    "|\\uD9BF\\uDFFE|\\uD9BF\\uDFFF" +  //  U+7FFFE|F
                    "|\\uD9FF\\uDFFE|\\uD9FF\\uDFFF" +  //  U+8FFFE|F
                    "|\\uDA3F\\uDFFE|\\uDA3F\\uDFFF" +  //  U+9FFFE|F
                    "|\\uDA7F\\uDFFE|\\uDA7F\\uDFFF" +  //  U+AFFFE|F
                    "|\\uDABF\\uDFFE|\\uDABF\\uDFFF" +  //  U+BFFFE|F
                    "|\\uDAFF\\uDFFE|\\uDAFF\\uDFFF" +  //  U+CFFFE|F
                    "|\\uDB3F\\uDFFE|\\uDB3F\\uDFFF" +  //  U+DFFFE|F
                    "|\\uDB7F\\uDFFE|\\uDB7F\\uDFFF" +  //  U+EFFFE|F
                    "|\\uDBBF\\uDFFE|\\uDBBF\\uDFFF" +  //  U+FFFFE|F
                    "|\\uDBFF\\uDFFE|\\uDBFF\\uDFFF");  // U+10FFFE|F
    /**
     * MQTT Protocol name as a UTF-8 encoded String.
     */
    @NotNull
    public static final MqttUtf8StringImpl PROTOCOL_NAME = new MqttUtf8StringImpl(encode("MQTT"));

    /**
     * Validates and decodes a UTF-8 encoded String from the given byte array.
     * <p>
     * Note: the given byte array must not be longer than {@link MqttBinaryData#MAX_LENGTH}.
     *
     * @param binary the byte array with the UTF-8 encoded data to decode from.
     * @return the created UTF-8 encoded String or null if the byte array does not contain a well-formed UTF-8 encoded
     * String.
     */
    @Nullable
    public static MqttUtf8StringImpl from(@NotNull final byte[] binary) {
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
    @NotNull
    public static MqttUtf8StringImpl from(@NotNull final String string) {
        checkUtf8EncodedLength(string);
        checkForbiddenCharacters(string);

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
     * String.
     */
    @Nullable
    public static MqttUtf8StringImpl from(@NotNull final ByteBuf byteBuf) {
        final byte[] binary = MqttBinaryData.decode(byteBuf);
        return (binary == null) ? null : from(binary);
    }

    /**
     * Decodes from the given UTF-8 encoded byte array to a UTF-16 encoded Java string.
     *
     * @param binary the UTF-8 encoded byte array.
     * @return the UTF-16 encoded Java string.
     */
    @NotNull
    private static String decode(@NotNull final byte[] binary) {
        return new String(binary, CHARSET);
    }

    /**
     * Encodes from the given UTF-16 encoded Java string to a UTF-8 encoded byte array.
     *
     * @param string the UTF-16 encoded Java string.
     * @return the UTF-8 encoded byte array.
     */
    @NotNull
    static byte[] encode(@NotNull final String string) {
        return string.getBytes(CHARSET);
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
    static boolean containsMustNotCharacters(@NotNull final byte[] binary) {
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
    static void checkForbiddenCharacters(@NotNull final String string) {
        boolean previousCharIsHighSurrogate = false;
        int unmatchedSurrogateIndex = -1;
        for (int i = 0; (unmatchedSurrogateIndex < 0) && (i < string.length()); i++) {
            final char c = string.charAt(i);
            if (c == 0) {
                throw new IllegalArgumentException("Found null character at index: " + i +
                        ". String must not contain the null character U+0000, as it is forbidden according to the MQTT 5 Specification.");
            }
            if (previousCharIsHighSurrogate != Character.isLowSurrogate(c)) {
                unmatchedSurrogateIndex = i;
            }
            previousCharIsHighSurrogate = Character.isHighSurrogate(c);
        }
        if ((unmatchedSurrogateIndex < 0) && previousCharIsHighSurrogate) {
            unmatchedSurrogateIndex = string.length() - 1;
        }
        if (unmatchedSurrogateIndex >= 0) {
            throw new IllegalArgumentException(
                    "Found unmatched UTF-16 surrogate at index: " + unmatchedSurrogateIndex + ".");
        }
    }

    /**
     * Checks if the given UTF-16 Java string encoded in UTF-8 fits into {@link MqttBinaryData}.
     *
     * @param string the UTF-16 encoded Java string.
     * @throws IllegalArgumentException if the given string encoded in UTF-8 is longer than {@link
     *                                  MqttBinaryData#MAX_LENGTH}.
     */
    static void checkUtf8EncodedLength(@NotNull final String string) {
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
            // have a closer look at the exact length of encoded string
            int utf8Length = Utf8.encodedLength(string);
            Preconditions.checkArgument(utf8Length <= MqttBinaryData.MAX_LENGTH,
                    "String encoded in UTF-8 must not be longer than %s bytes. Actual string length encoded in UTF-8 is %s.",
                    MqttBinaryData.MAX_LENGTH, utf8Length);
        }
    }

    private byte[] binary;
    private String string;
    private int conversions;

    MqttUtf8StringImpl(@NotNull final byte[] binary) {
        this.binary = binary;
    }

    MqttUtf8StringImpl(@NotNull final String string) {
        this.string = string;
    }

    @Override
    public boolean containsShouldNotCharacters() {
        return SHOULD_NOT_CHARACTERS_PATTERN.matcher(toString()).find();
    }

    @NotNull
    @Override
    public ByteBuffer toByteBuffer() {
        return ByteBuffer.wrap(toBinary()).asReadOnlyBuffer();
    }

    /**
     * Returns the UTF-8 encoded representation as a byte array. Converts from the UTF-16 encoded representation if
     * necessary.
     *
     * @return the UTF-8 encoded byte array.
     */
    @NotNull
    byte[] toBinary() {
        if (binary == null) {
            binary = encode(string);
            conversions++;
            if (conversions < 3) {
                string = null;
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
    @NotNull
    @Override
    public String toString() {
        if (string == null) {
            string = decode(binary);
            conversions++;
            if (conversions < 3) {
                binary = null;
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
    public void to(final @Nullable ByteBuf byteBuf) {
        MqttBinaryData.encode(toBinary(), Checks.notNull(byteBuf, "Byte buffer"));
    }

    /**
     * Calculates the byte count of this UTF-8 encoded String according to the MQTT 5 specification.
     *
     * @return the encoded length of this UTF-8 encoded String.
     * @throws MqttBinaryDataExceededException if this UTF-8 encoded String can not be encoded due to byte count
     *                                         restrictions.
     */
    public int encodedLength() {
        final byte[] binary = toBinary();
        return MqttBinaryData.encodedLength(binary);
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
        if (string != null) {
            if ((that.string == null) && (binary != null)) {
                return Arrays.equals(binary, that.binary);
            }
            return string.equals(that.toString());
        } else {
            return Arrays.equals(binary, that.toBinary());
        }
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

}
