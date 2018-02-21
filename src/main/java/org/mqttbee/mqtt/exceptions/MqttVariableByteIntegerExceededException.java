package org.mqttbee.mqtt.exceptions;

import org.mqttbee.annotations.NotNull;

/**
 * Exception to indicate that an integer can not be encoded as a variable byte integer.
 * <p>
 * This exception does not have a stack trace.
 *
 * @author Silvio Giebl
 */
public class MqttVariableByteIntegerExceededException extends RuntimeException {

    /**
     * Creates a new MqttVariableByteIntegerExceededException with the name of the variable byte integer.
     *
     * @param name the name of the variable byte integer.
     */
    public MqttVariableByteIntegerExceededException(@NotNull final String name) {
        super("variable byte integer size exceeded for " + name);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }

}
