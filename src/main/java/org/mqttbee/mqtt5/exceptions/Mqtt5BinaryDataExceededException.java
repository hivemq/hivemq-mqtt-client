package org.mqttbee.mqtt5.exceptions;

import org.mqttbee.annotations.NotNull;

/**
 * Exception to indicate that a byte array can not be encoded as binary data according to the MQTT 5 specification.
 * <p>
 * This exception does not have a stack trace.
 *
 * @author Silvio Giebl
 */
public class Mqtt5BinaryDataExceededException extends RuntimeException {

    /**
     * Creates a new Mqtt5BinaryDataExceededException with the name of the binary data.
     *
     * @param name the name of the binary data.
     */
    public Mqtt5BinaryDataExceededException(@NotNull final String name) {
        super("binary data size exceeded for " + name);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }

}
