package org.mqttbee.api.mqtt.mqtt5.exceptions;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt5.message.Mqtt5Message;

/**
 * @author Silvio Giebl
 */
public class Mqtt5MessageException extends Exception {

    private final Mqtt5Message mqtt5Message;

    public Mqtt5MessageException(
            @NotNull final Mqtt5Message mqtt5Message, @NotNull final String message) {

        super(message);
        this.mqtt5Message = mqtt5Message;
    }

    public Mqtt5MessageException(
            @NotNull final Mqtt5Message mqtt5Message, @NotNull final Throwable cause) {

        super(cause.getMessage(), cause);
        this.mqtt5Message = mqtt5Message;
    }

    @NotNull
    public Mqtt5Message getMqttMessage() {
        return mqtt5Message;
    }

}
