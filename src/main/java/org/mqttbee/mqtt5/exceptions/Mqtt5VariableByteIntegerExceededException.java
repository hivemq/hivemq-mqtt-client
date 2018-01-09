package org.mqttbee.mqtt5.exceptions;

import org.mqttbee.annotations.NotNull;

/**
 * Exception to indicate that an integer can not be encoded as a variable byte integer according to the MQTT 5
 * specification.
 * <p>
 * This exception does not have a stack trace.
 *
 * @author Silvio Giebl
 */
public class Mqtt5VariableByteIntegerExceededException extends RuntimeException {

    /**
     * Creates a new Mqtt5VariableByteIntegerExceededException with the name of the variable byte integer.
     *
     * @param name the name of the variable byte integer.
     */
    public Mqtt5VariableByteIntegerExceededException(@NotNull final String name) {
        super("variable byte integer size exceeded for " + name);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }

}
