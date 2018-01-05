package org.mqttbee.mqtt5.exceptions;

import org.mqttbee.annotations.NotNull;

/**
 * @author Silvio Giebl
 */
public class Mqtt5BinaryDataExceededException extends RuntimeException {

    public Mqtt5BinaryDataExceededException(@NotNull final String name) {
        super("binary data size exceeded for " + name);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }

}
