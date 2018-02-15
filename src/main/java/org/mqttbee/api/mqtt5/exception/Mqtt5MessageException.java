package org.mqttbee.api.mqtt5.exception;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt5.message.Mqtt5Message;

/**
 * @author Silvio Giebl
 */
public class Mqtt5MessageException extends Exception {

    private final Mqtt5Message mqtt5Message;

    public Mqtt5MessageException(@NotNull final String message, @NotNull final Mqtt5Message mqtt5Message) {
        super(message);
        this.mqtt5Message = mqtt5Message;
    }

    @NotNull
    public Mqtt5Message getMqttMessage() {
        return mqtt5Message;
    }

}
