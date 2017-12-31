package org.mqttbee.mqtt5.message;

import io.netty.buffer.ByteBuf;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;

import java.util.regex.Pattern;

/**
 * @author Silvio Giebl
 */
public class Mqtt5Topic extends Mqtt5UTF8String {

    private static final Pattern MUST_NOT_WILDCARD_CHARACTERS_PATTERN = Pattern.compile("[*#]");

    @Nullable
    public static Mqtt5Topic from(@NotNull final byte[] binary) {
        final String string = decode(binary); // TODO: containsMustNotCharacters(byte[]) without decoding
        return (string == null || containsMustNotWildcardCharacters(string)) ? null : new Mqtt5Topic(binary, string);
    }

    @Nullable
    public static Mqtt5Topic from(@NotNull final ByteBuf byteBuf) {
        final byte[] binary = Mqtt5DataTypes.decodeBinaryData(byteBuf);
        return (binary == null) ? null : from(binary);
    }

    @Nullable
    public static Mqtt5Topic from(@NotNull final String string) {
        return (containsMustNotCharacters(string) || containsMustNotWildcardCharacters(string)) ? null :
                new Mqtt5Topic(string);
    }

    private static boolean containsMustNotWildcardCharacters(@NotNull final String string) { // TODO only one regex?
        return MUST_NOT_WILDCARD_CHARACTERS_PATTERN.matcher(string).find();
    }

    private Mqtt5Topic(@NotNull final String string) {
        super(string);
    }

    private Mqtt5Topic(@NotNull final byte[] binary, @NotNull final String string) {
        super(binary, string);
    }

}
