package org.mqttbee.mqtt5.message;

import org.mqttbee.annotations.NotNull;

/**
 * @author Silvio Giebl
 */
public interface Mqtt5Message {

    @NotNull
    Mqtt5MessageType getType();

}
