package org.mqttbee.api.mqtt5.message;

import com.google.common.base.Preconditions;
import org.mqttbee.annotations.DoNotImplement;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.message.Mqtt5ClientIdentifierImpl;

/**
 * MQTT Client Identifier according to the MQTT 5 specification.
 * <p>
 * A Client Identifier has the same restrictions from {@link Mqtt5UTF8String}.
 *
 * @author Silvio Giebl
 */
@DoNotImplement
public interface Mqtt5ClientIdentifier extends Mqtt5UTF8String {

    /**
     * Validates and creates a Client Identifier from the given string.
     *
     * @param string the Client Identifier as a UTF-16 encoded Java string.
     * @return the created Client Identifier.
     * @throws IllegalArgumentException if the string is not a valid Client Identifier.
     */
    @NotNull
    static Mqtt5ClientIdentifier from(@NotNull final String string) {
        Preconditions.checkNotNull(string);

        final Mqtt5ClientIdentifier clientIdentifier = Mqtt5ClientIdentifierImpl.from(string);
        if (clientIdentifier == null) {
            throw new IllegalArgumentException("The string: [" + string + "] is not a valid Client Identifier.");
        }
        return clientIdentifier;
    }

    /**
     * Checks whether this Client Identifier must be allowed by a MQTT broker according to the MQTT 5 specification.
     * <p>
     * A Client Identifier must be allowed by a MQTT broker if it is between 1 and 23 characters long and only contains
     * lower or uppercase alphabetical characters or numbers.
     *
     * @return whether this Client Identifier must be allowed by a MQTT broker.
     */
    boolean mustBeAllowedByServer();

}
