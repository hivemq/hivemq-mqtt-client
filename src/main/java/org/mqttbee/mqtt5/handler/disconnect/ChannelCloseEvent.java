package org.mqttbee.mqtt5.handler.disconnect;

import org.mqttbee.annotations.NotNull;

/**
 * Event that is fired when the channel will be closed containing the cause.
 * <p>
 * Only one such event is fired in all cases:
 * <ul>
 * <li>Server sent a DISCONNECT message</li>
 * <li>Client sends a DISCONNECT message</li>
 * <li>Server closed the channel without a DISCONNECT message</li>
 * <li>Client closes the channel without a DISCONNECT message</li>
 * </ul>
 *
 * @author Silvio Giebl
 */
public class ChannelCloseEvent {

    private final Throwable cause;

    ChannelCloseEvent(@NotNull final Throwable cause) {
        this.cause = cause;
    }

    /**
     * @return the cause for the channel closing.
     */
    @NotNull
    public Throwable getCause() {
        return cause;
    }

}
