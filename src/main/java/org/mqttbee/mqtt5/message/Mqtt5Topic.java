package org.mqttbee.mqtt5.message;

import io.netty.buffer.ByteBuf;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;

/**
 * @author Silvio Giebl
 */
public class Mqtt5Topic extends Mqtt5UTF8String {

    @Nullable
    public static Mqtt5Topic from(@NotNull final byte[] binary) {
        return containsMustNotCharacters(binary) ? null : new Mqtt5Topic(binary);
    }

    @Nullable
    public static Mqtt5Topic from(@NotNull final ByteBuf byteBuf) {
        final byte[] binary = Mqtt5DataTypes.decodeBinaryData(byteBuf);
        return (binary == null) ? null : from(binary);
    }

    @Nullable
    public static Mqtt5Topic from(@NotNull final String string) {
        return containsMustNotCharacters(string) ? null : new Mqtt5Topic(string);
    }

    static boolean containsMustNotCharacters(@NotNull final byte[] binary) {
        return Mqtt5UTF8String.containsMustNotCharacters(binary) || Mqtt5TopicFilter.containsWildcardCharacters(binary);
    }

    static boolean containsMustNotCharacters(@NotNull final String string) {
        return Mqtt5UTF8String.containsMustNotCharacters(string) || Mqtt5TopicFilter.containsWildcardCharacters(string);
    }

    private Mqtt5Topic(@NotNull final String string) {
        super(string);
    }

    private Mqtt5Topic(@NotNull final byte[] binary) {
        super(binary);
    }

}
