package org.mqttbee.mqtt5.message;

import com.google.common.base.Utf8;
import io.netty.buffer.ByteBuf;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;
import org.mqttbee.mqtt5.exceptions.Mqtt5BinaryDataExceededException;
import org.mqttbee.mqtt5.message.util.ByteBufUtil;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * This class lazily en/decodes between UTF-8 and UTF-16 encoding, but performs validation upfront.
 *
 * @author Silvio Giebl
 * @see Mqtt5UTF8String
 */
public class Mqtt5UTF8StringImpl implements Mqtt5UTF8String {

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
    public static final Mqtt5UTF8StringImpl PROTOCOL_NAME = new Mqtt5UTF8StringImpl(encode("MQTT"));

    /**
     * Validates and decodes a UTF-8 encoded String from the given byte array.
     *
     * @param binary the byte array with the UTF-8 encoded data to decode from.
     * @return the created UTF-8 encoded String or null if the byte array does not contain a well-formed UTF-8 encoded
     * String.
     */
    @Nullable
    public static Mqtt5UTF8StringImpl from(@NotNull final byte[] binary) {
        return containsMustNotCharacters(binary) ? null : new Mqtt5UTF8StringImpl(binary);
    }

    /**
     * Validates and creates a UTF-8 encoded String from the given string.
     *
     * @param string the UTF-16 encoded Java string.
     * @return the created UTF-8 encoded String or null if the string is not a valid UTF-8 encoded String.
     */
    @Nullable
    public static Mqtt5UTF8StringImpl from(@NotNull final String string) {
        return containsMustNotCharacters(string) ? null : new Mqtt5UTF8StringImpl(string);
    }

    /**
     * Validates and decodes a UTF-8 encoded String from the given byte buffer at the current reader index.
     * <p>
     * In case of a wrong encoding the reader index of the byte buffer will be in an undefined state after the method
     * returns.
     *
     * @param byteBuf the byte buffer with the UTF-8 encoded data to decode from.
     * @return the created UTF-8 encoded String or null if the byte buffer does not contain a well-formed UTF-8 encoded
     * String.
     */
    @Nullable
    public static Mqtt5UTF8StringImpl from(@NotNull final ByteBuf byteBuf) {
        final byte[] binary = Mqtt5DataTypes.decodeBinaryData(byteBuf);
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
     * Checks whether the given UTF-16 encoded Java string contains characters a UTF-8 encoded String must not according
     * to the MQTT 5 specification.
     * <p>
     * These characters are the null character U+0000 and UTF-16 surrogates.
     *
     * @param string the UTF-16 encoded Java string
     * @return whether the string contains characters a UTF-8 encoded String must not.
     */
    static boolean containsMustNotCharacters(@NotNull final String string) {
        boolean highSurrogate = false;
        for (int i = 0; i < string.length(); i++) {
            final char c = string.charAt(i);
            if (c == 0) {
                return true;
            }
            if (highSurrogate == !Character.isLowSurrogate(c)) {
                return true;
            }
            highSurrogate = Character.isHighSurrogate(c);
        }
        return highSurrogate;
    }


    private byte[] binary;
    private String string;
    private int conversions;

    Mqtt5UTF8StringImpl(@NotNull final byte[] binary) {
        this.binary = binary;
    }

    Mqtt5UTF8StringImpl(@NotNull final String string) {
        this.string = string;
    }

    @Override
    public boolean containsShouldNotCharacters() {
        return SHOULD_NOT_CHARACTERS_PATTERN.matcher(toString()).find();
    }

    @NotNull
    @Override
    public ByteBuf toByteBuf() {
        return ByteBufUtil.wrapReadOnly(toBinary());
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
    public void to(@NotNull final ByteBuf byteBuf) {
        Mqtt5DataTypes.encodeBinaryData(toBinary(), byteBuf);
    }

    /**
     * Calculates the byte count of this UTF-8 encoded String according to the MQTT 5 specification.
     *
     * @return the encoded length of this UTF-8 encoded String.
     * @throws Mqtt5BinaryDataExceededException if this UTF-8 encoded String can not be encoded due to byte count
     *                                          restrictions.
     */
    public int encodedLength() {
        final byte[] binary = toBinary();
        if (!Mqtt5DataTypes.isInBinaryDataRange(binary)) {
            throw new Mqtt5BinaryDataExceededException("UTF-8 encoded String"); // TODO
        }
        return Mqtt5DataTypes.encodedBinaryDataLength(binary);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Mqtt5UTF8StringImpl)) {
            return false;
        }
        final Mqtt5UTF8StringImpl that = (Mqtt5UTF8StringImpl) o;
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
