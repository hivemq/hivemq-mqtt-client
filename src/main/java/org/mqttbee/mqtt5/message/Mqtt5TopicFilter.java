package org.mqttbee.mqtt5.message;

import io.netty.buffer.ByteBuf;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;

/**
 * @author Silvio Giebl
 */
public class Mqtt5TopicFilter extends Mqtt5UTF8String {

    public static final byte MULTI_LEVEL_WILDCARD = '#';
    public static final byte SINGLE_LEVEL_WILDCARD = '+';
    private static final String SHARE_PREFIX = "$share";

    @Nullable
    public static Mqtt5TopicFilter from(@NotNull final byte[] binary) {
        return containsMustNotCharacters(binary) ? null : new Mqtt5TopicFilter(binary); // FIXME validate wildcards
    }

    @Nullable
    public static Mqtt5TopicFilter from(@NotNull final ByteBuf byteBuf) {
        final byte[] binary = Mqtt5DataTypes.decodeBinaryData(byteBuf);
        return (binary == null) ? null : from(binary);
    }

    @Nullable
    public static Mqtt5TopicFilter from(@NotNull final String string) { // FIXME validate wildcards
        return containsMustNotCharacters(string) ? null : new Mqtt5TopicFilter(string);
    }

    static boolean containsWildcardCharacters(@NotNull final byte[] binary) {
        for (final byte b : binary) {
            if (b == MULTI_LEVEL_WILDCARD || b == SINGLE_LEVEL_WILDCARD) {
                return true;
            }
        }
        return false;
    }

    static boolean containsWildcardCharacters(@NotNull final String string) {
        for (int i = 0; i < string.length(); i++) {
            final char c = string.charAt(i);
            if (c == MULTI_LEVEL_WILDCARD || c == SINGLE_LEVEL_WILDCARD) {
                return true;
            }
        }
        return false;
    }

    static boolean isShared(@NotNull final byte[] binary) {
        return (binary[0] == SHARE_PREFIX.charAt(0)) && (binary[1] == SHARE_PREFIX.charAt(1)) &&
                (binary[2] == SHARE_PREFIX.charAt(2)) && (binary[3] == SHARE_PREFIX.charAt(3)) &&
                (binary[4] == SHARE_PREFIX.charAt(4)) && (binary[5] == SHARE_PREFIX.charAt(5));
    }

    static boolean isShared(@NotNull final String string) {
        return string.startsWith(SHARE_PREFIX);
    }

    private Mqtt5TopicFilter(@NotNull final String string) {
        super(string);
    }

    private Mqtt5TopicFilter(@NotNull final byte[] binary) {
        super(binary);
    }

    public boolean containsWildcards() {
        return (binary == null) ? containsWildcardCharacters(string) : containsWildcardCharacters(binary);
    }

    public boolean isShared() {
        return (binary == null) ? isShared(string) : isShared(binary);
    }

}
