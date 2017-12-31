package org.mqttbee.mqtt5.message;

import io.netty.buffer.ByteBuf;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;

/**
 * @author Silvio Giebl
 */
public class Mqtt5TopicFilter extends Mqtt5UTF8String {

    @Nullable
    public static Mqtt5TopicFilter from(@NotNull final byte[] binary) {
        final String string = decode(binary); // TODO: containsMustNotCharacters(byte[]) without decoding
        return (string == null) ? null : new Mqtt5TopicFilter(binary, string);
    }

    @Nullable
    public static Mqtt5TopicFilter from(@NotNull final ByteBuf byteBuf) {
        final byte[] binary = Mqtt5DataTypes.decodeBinaryData(byteBuf);
        return (binary == null) ? null : from(binary);
    }

    @Nullable
    public static Mqtt5TopicFilter from(@NotNull final String string) {
        return containsMustNotCharacters(string) ? null : new Mqtt5TopicFilter(string);
    }

    private Mqtt5TopicFilter(@NotNull final String string) {
        super(string);
    }

    private Mqtt5TopicFilter(@NotNull final byte[] binary, @NotNull final String string) {
        super(binary, string);
    }

}
