package org.mqttbee.mqtt5.handler.disconnect;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt5.exceptions.Mqtt5MessageException;
import org.mqttbee.mqtt.message.disconnect.MqttDisconnect;

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
    private final boolean fromServer;

    ChannelCloseEvent(@NotNull final Throwable cause, final boolean fromServer) {
        this.cause = cause;
        this.fromServer = fromServer;
    }

    /**
     * @return the cause for the channel closing.
     */
    @NotNull
    public Throwable getCause() {
        return cause;
    }

    /**
     * @return whether the server sent a DISCONNECT message or closed the channel without a DISCONNECT message.
     */
    public boolean fromServer() {
        return fromServer;
    }

    /**
     * @return whether the channel is closed after a DISCONNECT message was sent or received.
     */
    public boolean withDisconnect() {
        return (cause instanceof Mqtt5MessageException) &&
                (((Mqtt5MessageException) cause).getMqttMessage() instanceof MqttDisconnect);
    }

}
