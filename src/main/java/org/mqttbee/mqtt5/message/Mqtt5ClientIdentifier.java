package org.mqttbee.mqtt5.message;

import io.netty.buffer.ByteBuf;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;

import java.util.regex.Pattern;

/**
 * MQTT Client Identifier according to the MQTT 5 specification.
 * <p>
 * This class lazily en/decodes between UTF-8 and UTF-16 encoding, but performs validation upfront.
 * <p>
 * A Client Identifier has the same restrictions from {@link Mqtt5UTF8String}.
 *
 * @author Silvio Giebl
 */
public class Mqtt5ClientIdentifier extends Mqtt5UTF8String {

    /**
     * Placeholder for a Client Identifier to indicate that the MQTT broker should assign the Client Identifier.
     */
    @NotNull
    public static final Mqtt5ClientIdentifier REQUEST_CLIENT_IDENTIFIER_FROM_SERVER =
            new Mqtt5ClientIdentifier(encode(""));
    private static final Pattern MUST_BE_ALLOWED_BY_SERVER_PATTERN = Pattern.compile("([0-9]|[a-z]|[A-Z])*");
    private static final int MUST_BE_ALLOWED_BY_SERVER_MIN_BYTES = 1;
    private static final int MUST_BE_ALLOWED_BY_SERVER_MAX_BYTES = 23;

    /**
     * Validates and decodes a Client Identifier from the given byte array.
     *
     * @param binary the byte array with the UTF-8 encoded data to decode from.
     * @return the created Client Identifier or null if the byte array does not contain a well-formed encoded Client
     * Identifier.
     */
    @Nullable
    public static Mqtt5ClientIdentifier from(@NotNull final byte[] binary) {
        return containsMustNotCharacters(binary) ? null : new Mqtt5ClientIdentifier(binary);
    }

    /**
     * Validates and creates a Client Identifier from the given string.
     *
     * @param string the Client Identifier as a UTF-16 encoded Java string.
     * @return the created Client Identifier or null if the given string is not a valid Client Identifier.
     */
    @Nullable
    public static Mqtt5ClientIdentifier from(@NotNull final String string) {
        return containsMustNotCharacters(string) ? null : new Mqtt5ClientIdentifier(string);
    }

    /**
     * Validates and decodes a Client Identifier from the given byte buffer at the current reader index.
     * <p>
     * In case of a wrong encoding the reader index of the byte buffer will be in an undefined state after the method
     * returns.
     *
     * @param byteBuf the byte buffer with the UTF-8 encoded data to decode from.
     * @return the created Client Identifier or null if the byte buffer does not contain a well-formed encoded Client
     * Identifier.
     */
    @Nullable
    public static Mqtt5ClientIdentifier from(@NotNull final ByteBuf byteBuf) {
        final byte[] binary = Mqtt5DataTypes.decodeBinaryData(byteBuf);
        return (binary == null) ? null : from(binary);
    }


    private Mqtt5ClientIdentifier(@NotNull final byte[] binary) {
        super(binary);
    }

    private Mqtt5ClientIdentifier(@NotNull final String string) {
        super(string);
    }

    /**
     * Checks whether this Client Identifier must be allowed by a MQTT broker according to the MQTT 5 specification.
     * <p>
     * A Client Identifier must be allowed by a MQTT broker if it is between 1 and 23 characters long and only contains
     * lower or uppercase alphabetical characters or numbers.
     *
     * @return whether this Client Identifier must be allowed by a MQTT broker.
     */
    public boolean mustBeAllowedByServer() {
        final byte[] binary = toBinary();
        return binary.length >= MUST_BE_ALLOWED_BY_SERVER_MIN_BYTES &&
                binary.length <= MUST_BE_ALLOWED_BY_SERVER_MAX_BYTES &&
                MUST_BE_ALLOWED_BY_SERVER_PATTERN.matcher(toString()).matches();
    }

}
