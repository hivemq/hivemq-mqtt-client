package org.mqttbee.mqtt5.exceptions;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.message.Mqtt5Message;

/**
 * Exception to indicate that the packet size exceeds the maximum packet size.
 * <p>
 * This exception does not have a stack trace.
 *
 * @author Silvio Giebl
 */
public class Mqtt5MaximumPacketSizeExceededException extends RuntimeException {

    public Mqtt5MaximumPacketSizeExceededException(@NotNull final Mqtt5Message message, final int maxPacketSize) {
        super("packet size exceeded for " + message.getClass().getSimpleName() + ", size: " +
                message.getEncoder().encodedLength(Integer.MAX_VALUE) + ", maximum: " + maxPacketSize);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }

}
