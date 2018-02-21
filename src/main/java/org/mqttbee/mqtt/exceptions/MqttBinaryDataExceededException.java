package org.mqttbee.mqtt.exceptions;

import org.mqttbee.annotations.NotNull;

/**
 * Exception to indicate that a byte array can not be encoded as binary data.
 * <p>
 * This exception does not have a stack trace.
 *
 * @author Silvio Giebl
 */
public class MqttBinaryDataExceededException extends RuntimeException {

    /**
     * Creates a new MqttBinaryDataExceededException with the name of the binary data.
     *
     * @param name the name of the binary data.
     */
    public MqttBinaryDataExceededException(@NotNull final String name) {
        super("binary data size exceeded for " + name);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }

}
