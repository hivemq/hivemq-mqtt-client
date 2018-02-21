package org.mqttbee.api.mqtt.mqtt5.message;

/**
 * Reason Code according to the MQTT 5 specification.
 *
 * @author Silvio Giebl
 */
public interface Mqtt5ReasonCode {

    /**
     * @return the byte code of this Reason Code.
     */
    int getCode();

    /**
     * @return whether this Reason Code is an Error Code.
     */
    default boolean isError() {
        return getCode() > 0x80;
    }

}
