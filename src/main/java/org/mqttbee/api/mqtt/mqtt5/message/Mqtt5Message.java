package org.mqttbee.api.mqtt.mqtt5.message;

import org.mqttbee.annotations.NotNull;

/**
 * MQTT message according to the MQTT 5 specification.
 *
 * @author Silvio Giebl
 */
public interface Mqtt5Message {

    /**
     * @return the type of this MQTT message.
     */
    @NotNull
    Mqtt5MessageType getType();

}
