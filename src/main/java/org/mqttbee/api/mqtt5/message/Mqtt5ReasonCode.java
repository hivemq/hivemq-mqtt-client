package org.mqttbee.api.mqtt5.message;

/**
 * @author Silvio Giebl
 */
public interface Mqtt5ReasonCode {

    int getCode();

    default boolean isError() {
        return getCode() > 0x80;
    }

}
