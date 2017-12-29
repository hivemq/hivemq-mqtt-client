package org.mqttbee.mqtt5.codec;

import io.netty.buffer.ByteBuf;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;

import java.nio.charset.Charset;
import java.util.regex.Pattern;

/**
 * @author Silvio Giebl
 */
public class Mqtt5DataTypes {

    public static final int VARIABLE_BYTE_INTEGER_NOT_ENOUGH_BYTES = -1;
    public static final int VARIABLE_BYTE_INTEGER_TOO_LARGE = -2;
    public static final int VARIABLE_BYTE_INTEGER_NOT_MINIMUM_BYTES = -3;
    private static final byte VARIABLE_BYTE_INTEGER_CONTINUATION_BIT_MASK = (byte) 0x80;
    private static final byte VARIABLE_BYTE_INTEGER_VALUE_MASK = (byte) 0x7f;
    private static final byte VARIABLE_BYTE_INTEGER_MAX_SHIFT = (byte) (7 * 3);
    private static final int VARIABLE_BYTE_INTEGER_MAX_VALUE = (1 << (7 * 4)) - 1;
    public static final Charset UTF8_STRING_CHARSET = Charset.forName("UTF-8");
    private static final Pattern UTF8_STRING_MUST_NOT_CHARACTERS_PATTERN = Pattern.compile("\\u0000|[\\uD800-\\uDFFF]");
    private static final Pattern UTF8_STRING_SHOULD_NOT_CHARACTERS_PATTERN =
            Pattern.compile("[\\u0001-\\u001F]|[\\u007F-\\u009F]|[\\uFDD0-\\uFDEF]" +
                    "|\\uFFFE|\\uFFFF" +               //   U+FFFE|F
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

    private Mqtt5DataTypes() {
    }

    public static int decodeVariableByteInteger(@NotNull final ByteBuf byteBuf) {
        byte encodedByte;
        int value = 0;
        byte shift = 0;

        do {
            if (shift > VARIABLE_BYTE_INTEGER_MAX_SHIFT) {
                return VARIABLE_BYTE_INTEGER_TOO_LARGE;
            }
            if (!byteBuf.isReadable()) {
                return VARIABLE_BYTE_INTEGER_NOT_ENOUGH_BYTES;
            }
            encodedByte = byteBuf.readByte();
            final int encodedByteValue = encodedByte & VARIABLE_BYTE_INTEGER_VALUE_MASK;
            if (shift > 0 && encodedByteValue == 0) {
                return VARIABLE_BYTE_INTEGER_NOT_MINIMUM_BYTES;
            }
            value += encodedByteValue << shift;
            shift += 7;
        } while ((encodedByte & VARIABLE_BYTE_INTEGER_CONTINUATION_BIT_MASK) != 0);

        return value;
    }

    public static boolean encodeVariableByteInteger(int value, @NotNull final ByteBuf byteBuf) {
        if (value > VARIABLE_BYTE_INTEGER_MAX_VALUE) {
            return false;
        }

        do {
            int encodedByte = value & VARIABLE_BYTE_INTEGER_VALUE_MASK;
            value >>>= 7;
            if (value > 0) {
                encodedByte |= VARIABLE_BYTE_INTEGER_CONTINUATION_BIT_MASK;
            }
            byteBuf.writeByte(encodedByte);
        } while (value > 0);

        return true;
    }

    @Nullable
    public static String decodeUTF8String(@NotNull final ByteBuf byteBuf) {
        final byte[] bytes = decodeBinaryData(byteBuf);
        if (bytes == null) {
            return null;
        }
        final String string = new String(bytes, UTF8_STRING_CHARSET);
        if (checkUTF8StringMustNotCharacters(string)) {
            return null;
        }
        return string;
    }

    public static boolean encodeUTF8String(@NotNull final String string, @NotNull final ByteBuf byteBuf) {
        if (checkUTF8StringMustNotCharacters(string)) {
            return false;
        }
        encodeBinaryData(string.getBytes(UTF8_STRING_CHARSET), byteBuf);
        return true;
    }

    public static boolean checkUTF8StringMustNotCharacters(@NotNull final String string) {
        return UTF8_STRING_MUST_NOT_CHARACTERS_PATTERN.matcher(string).find();
    }

    public static boolean checkUTF8StringShouldNotCharacters(@NotNull final String string) {
        return UTF8_STRING_SHOULD_NOT_CHARACTERS_PATTERN.matcher(string).find();
    }

    @Nullable
    public static byte[] decodeBinaryData(@NotNull final ByteBuf byteBuf) {
        if (byteBuf.readableBytes() < 2) {
            return null;
        }
        final int length = byteBuf.readUnsignedShort();
        if (byteBuf.readableBytes() < length) {
            return null;
        }
        final byte[] value = new byte[length];
        byteBuf.readBytes(value);
        return value;
    }

    public static void encodeBinaryData(@NotNull final byte[] value, @NotNull final ByteBuf byteBuf) {
        byteBuf.writeShort(value.length);
        byteBuf.writeBytes(value);
    }

    public static boolean skipBinaryData(@NotNull final ByteBuf byteBuf) {
        if (byteBuf.readableBytes() < 2) {
            return false;
        }
        final int length = byteBuf.readUnsignedShort();
        if (byteBuf.readableBytes() < length) {
            return false;
        }
        byteBuf.skipBytes(length);
        return true;
    }

}
