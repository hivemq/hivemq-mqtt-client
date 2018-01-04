package org.mqttbee.mqtt5.exceptions;

import org.mqttbee.annotations.NotNull;

/**
 * @author Silvio Giebl
 */
public class Mqtt5VariableByteIntegerExceededException extends RuntimeException {

    public Mqtt5VariableByteIntegerExceededException(@NotNull final String name) {
        super("variable byte integer size exceeded for " + name);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }

}
