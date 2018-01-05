package org.mqttbee.mqtt5.message;

import io.netty.buffer.ByteBuf;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;

import java.util.regex.Pattern;

/**
 * @author Silvio Giebl
 */
public class Mqtt5ClientIdentifier extends Mqtt5UTF8String {

    @NotNull
    public static final Mqtt5ClientIdentifier REQUEST_CLIENT_IDENTIFIER_FROM_SERVER =
            new Mqtt5ClientIdentifier(encode(""));
    private static final Pattern MUST_BE_ALLOWED_BY_SERVER_PATTERN = Pattern.compile("([0-9]|[a-z]|[A-Z])*");
    private static final int MUST_BE_ALLOWED_BY_SERVER_MIN_BYTES = 1;
    private static final int MUST_BE_ALLOWED_BY_SERVER_MAX_BYTES = 23;

    @Nullable
    public static Mqtt5ClientIdentifier from(@NotNull final byte[] binary) {
        return containsMustNotCharacters(binary) ? null : new Mqtt5ClientIdentifier(binary);
    }

    @Nullable
    public static Mqtt5ClientIdentifier from(@NotNull final ByteBuf byteBuf) {
        final byte[] binary = Mqtt5DataTypes.decodeBinaryData(byteBuf);
        return (binary == null) ? null : from(binary);
    }

    @Nullable
    public static Mqtt5ClientIdentifier from(@NotNull final String string) {
        return containsMustNotCharacters(string) ? null : new Mqtt5ClientIdentifier(string);
    }

    private Mqtt5ClientIdentifier(@NotNull final String string) {
        super(string);
    }

    private Mqtt5ClientIdentifier(@NotNull final byte[] binary) {
        super(binary);
    }

    public boolean mustBeAllowedByServer() {
        final byte[] binary = toBinary();
        return binary.length >= MUST_BE_ALLOWED_BY_SERVER_MIN_BYTES &&
                binary.length <= MUST_BE_ALLOWED_BY_SERVER_MAX_BYTES &&
                MUST_BE_ALLOWED_BY_SERVER_PATTERN.matcher(toString()).matches();
    }

}
