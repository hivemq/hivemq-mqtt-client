package org.mqttbee.api.mqtt.datatypes;

import org.mqttbee.annotations.DoNotImplement;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt.MqttBuilderUtil;

/**
 * MQTT Client Identifier according to the MQTT specification.
 * <p>
 * A Client Identifier has the same restrictions from {@link MqttUTF8String}.
 *
 * @author Silvio Giebl
 */
@DoNotImplement
public interface MqttClientIdentifier extends MqttUTF8String {

    /**
     * Validates and creates a Client Identifier from the given string.
     *
     * @param string the Client Identifier as a UTF-16 encoded Java string.
     * @return the created Client Identifier.
     * @throws IllegalArgumentException if the string is not a valid Client Identifier.
     */
    @NotNull
    static MqttClientIdentifier from(@NotNull final String string) {
        return MqttBuilderUtil.clientIdentifier(string);
    }

    /**
     * Checks whether this Client Identifier must be allowed by a MQTT broker according to the MQTT specification.
     * <p>
     * A Client Identifier must be allowed by a MQTT broker if it is between 1 and 23 characters long and only contains
     * lower or uppercase alphabetical characters or numbers.
     *
     * @return whether this Client Identifier must be allowed by a MQTT broker.
     */
    boolean mustBeAllowedByServer();

}
