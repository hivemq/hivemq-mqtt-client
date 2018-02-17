package org.mqttbee.api.mqtt5.exception;

import org.mqttbee.annotations.Nullable;

/**
 * @author Silvio Giebl
 */
public class ChannelClosedException extends Exception {

    public ChannelClosedException(@Nullable final String message) {
        super(message);
    }

}
