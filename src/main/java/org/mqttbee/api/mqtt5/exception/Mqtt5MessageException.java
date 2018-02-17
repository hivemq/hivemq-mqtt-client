package org.mqttbee.api.mqtt5.exception;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt5.message.Mqtt5Message;

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

        super(cause);
        this.mqtt5Message = mqtt5Message;
    }

    public Mqtt5MessageException(
            @NotNull final Mqtt5Message mqtt5Message, @Nullable final String message, @Nullable final Throwable cause) {

        super(message, cause);
        this.mqtt5Message = mqtt5Message;
    }

    @NotNull
    public Mqtt5Message getMqttMessage() {
        return mqtt5Message;
    }

}
