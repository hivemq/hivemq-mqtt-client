package org.mqttbee.mqtt5.message;

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

    @NotNull
    public static final Mqtt5UTF8String PROTOCOL_NAME = new Mqtt5UTF8String(encodeUnsafe("MQTT"));
    private static final Charset CHARSET = Charset.forName("UTF-8");
    private static final Pattern MUST_NOT_CHARACTERS_PATTERN = Pattern.compile("\\u0000|[\\uD800-\\uDFFF]");
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

    @Nullable
    public static Mqtt5UTF8String from(@NotNull final byte[] binary) {
        final String string = decode(binary); // TODO: containsMustNotCharacters(byte[]) without decoding
        return (string == null) ? null : new Mqtt5UTF8String(binary, string);
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

    @Nullable
    static String decode(@NotNull final byte[] binary) {
        final String string = decodeUnsafe(binary);
        return containsMustNotCharacters(string) ? null : string;
    }

    @NotNull
    private static String decodeUnsafe(@NotNull final byte[] binary) {
        return new String(binary, CHARSET);
    }

    @NotNull
    static byte[] encodeUnsafe(@NotNull final String string) {
        return string.getBytes(CHARSET);
    }

    static boolean containsMustNotCharacters(@NotNull final String string) {
        return MUST_NOT_CHARACTERS_PATTERN.matcher(string).find();
    }

    private byte[] binary;
    private String string;

    Mqtt5UTF8String(@NotNull final String string) {
        this.string = string;
    }

    Mqtt5UTF8String(@NotNull final byte[] binary) {
        this.binary = binary;
    }

    Mqtt5UTF8String(@NotNull final byte[] binary, @NotNull final String string) {
        this.binary = binary;
        this.string = string;
    }

    public boolean containsShouldNotCharacters() {
        return SHOULD_NOT_CHARACTERS_PATTERN.matcher(string).find();
    }

    @NotNull
    public byte[] toBinary() {
        if (binary == null) {
            binary = encodeUnsafe(string);
        }
        return binary;
    }

    @Override
    @NotNull
    public String toString() {
        if (string == null) {
            string = decodeUnsafe(binary);
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
