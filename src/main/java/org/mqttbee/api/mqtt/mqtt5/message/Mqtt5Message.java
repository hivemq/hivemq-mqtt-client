package org.mqttbee.api.mqtt.mqtt5.message;

/**
 * MQTT message according to the MQTT 5 specification.
 *
 * @author Silvio Giebl
 */
public interface Mqtt5Message {

    /**
     * @return the type of this MQTT message.
     */
    Mqtt5MessageType getType();

}
