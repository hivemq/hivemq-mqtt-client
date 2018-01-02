package org.mqttbee.mqtt5.message;

import com.google.common.base.Utf8;
import io.netty.buffer.ByteBuf;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;

import java.nio.charset.Charset;
import java.util.regex.Pattern;

/**
 * @author Silvio Giebl
 */
public class Mqtt5UTF8String {

    private static final Charset CHARSET = Charset.forName("UTF-8");
    private static final Pattern SHOULD_NOT_CHARACTERS_PATTERN =
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
    @NotNull
    public static final Mqtt5UTF8String PROTOCOL_NAME = new Mqtt5UTF8String(encode("MQTT"));

    @Nullable
    public static Mqtt5UTF8String from(@NotNull final byte[] binary) {
        return containsMustNotCharacters(binary) ? null : new Mqtt5UTF8String(binary);
    }

    @Nullable
    public static Mqtt5UTF8String from(@NotNull final ByteBuf byteBuf) {
        final byte[] binary = Mqtt5DataTypes.decodeBinaryData(byteBuf);
        return (binary == null) ? null : from(binary);
    }

    @Nullable
    public static Mqtt5UTF8String from(@NotNull final String string) {
        return containsMustNotCharacters(string) ? null : new Mqtt5UTF8String(string);
    }

    @NotNull
    private static String decode(@NotNull final byte[] binary) {
        return new String(binary, CHARSET);
    }

    @NotNull
    static byte[] encode(@NotNull final String string) {
        return string.getBytes(CHARSET);
    }

    static boolean containsMustNotCharacters(@NotNull final byte[] binary) {
        if (!Utf8.isWellFormed(binary)) {
            return false;
        }
        for (final byte b : binary) {
            if (b == 0) {
                return false;
            }
        }
        return true;
    }

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

    byte[] binary;
    String string;

    Mqtt5UTF8String(@NotNull final String string) {
        this.string = string;
    }

    Mqtt5UTF8String(@NotNull final byte[] binary) {
        this.binary = binary;
    }

    public boolean containsShouldNotCharacters() {
        return SHOULD_NOT_CHARACTERS_PATTERN.matcher(string).find();
    }

    @NotNull
    public byte[] toBinary() {
        if (binary == null) {
            binary = encode(string);
        }
        return binary;
    }

    @Override
    @NotNull
    public String toString() {
        if (string == null) {
            string = decode(binary);
        }
        return string;
    }

    public void to(@NotNull final ByteBuf byteBuf) {
        Mqtt5DataTypes.encodeBinaryData(toBinary(), byteBuf);
    }

    public int encodedLength() {
        return Mqtt5DataTypes.encodedBinaryDataLength(toBinary());
    }

}
