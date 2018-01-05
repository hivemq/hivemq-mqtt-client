package org.mqttbee.mqtt5.message;

import io.netty.buffer.ByteBuf;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;

/**
 * @author Silvio Giebl
 */
public class Mqtt5Topic extends Mqtt5UTF8String {

    public static final byte TOPIC_LEVEL_SEPARATOR = '/';

    @Nullable
    static Mqtt5Topic fromInternal(@NotNull final byte[] binary) {
        return containsMustNotCharacters(binary) ? null : new Mqtt5Topic(binary);
    }

    @Nullable
    public static Mqtt5Topic from(@NotNull final String string) {
        return containsMustNotCharacters(string) ? null : new Mqtt5Topic(string);
    }

    @Nullable
    public static Mqtt5Topic from(@NotNull final ByteBuf byteBuf) {
        final byte[] binary = Mqtt5DataTypes.decodeBinaryData(byteBuf);
        return (binary == null) ? null : fromInternal(binary);
    }

    static boolean containsMustNotCharacters(@NotNull final byte[] binary) {
        return Mqtt5UTF8String.containsMustNotCharacters(binary) || containsWildcardCharacters(binary);
    }

    static boolean containsMustNotCharacters(@NotNull final String string) {
        return Mqtt5UTF8String.containsMustNotCharacters(string) || containsWildcardCharacters(string);
    }

    private static boolean containsWildcardCharacters(@NotNull final byte[] binary) {
        for (final byte b : binary) {
            if (b == Mqtt5TopicFilter.MULTI_LEVEL_WILDCARD || b == Mqtt5TopicFilter.SINGLE_LEVEL_WILDCARD) {
                return true;
            }
        }
        return false;
    }

    static boolean containsWildcardCharacters(@NotNull final String string) {
        for (int i = 0; i < string.length(); i++) {
            final char c = string.charAt(i);
            if (c == Mqtt5TopicFilter.MULTI_LEVEL_WILDCARD || c == Mqtt5TopicFilter.SINGLE_LEVEL_WILDCARD) {
                return true;
            }
        }
        return false;
    }


    private Mqtt5Topic(@NotNull final byte[] binary) {
        super(binary);
    }

    private Mqtt5Topic(@NotNull final String string) {
        super(string);
    }

    @NotNull
    public String[] getLevels() {
        return toString().split(Character.toString((char) TOPIC_LEVEL_SEPARATOR));
    }

}
