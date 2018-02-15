package org.mqttbee.api.mqtt5.exception;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.message.Mqtt5Message;

/**
 * @author Silvio Giebl
 */
public class Mqtt5TimeoutException extends Exception {

    public Mqtt5TimeoutException(@NotNull final Class<? extends Mqtt5Message<?>> expectedMessageType) {
        super("Timeout while expecting [" + expectedMessageType.getSimpleName() + "] message from server");
    }

}
