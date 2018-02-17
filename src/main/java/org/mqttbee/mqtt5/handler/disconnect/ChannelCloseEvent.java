package org.mqttbee.mqtt5.handler.disconnect;

import org.mqttbee.annotations.NotNull;

/**
 * @author Silvio Giebl
 */
public class ChannelCloseEvent {

    private final Throwable cause;

    ChannelCloseEvent(@NotNull final Throwable cause) {
        this.cause = cause;
    }

    @NotNull
    public Throwable getCause() {
        return cause;
    }

}
