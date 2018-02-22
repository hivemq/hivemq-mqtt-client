package org.mqttbee.api.mqtt.mqtt3.message;

import org.mqttbee.annotations.NotNull;

/**
 * MQTT message according to the MQTT 3 specification.
 *
 * @author Silvio Giebl
 */
public interface Mqtt3Message {

    /**
     * @return the type of this MQTT message.
     */
    @NotNull
    Mqtt3MessageType getType();

}
