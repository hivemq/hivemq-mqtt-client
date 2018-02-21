package org.mqttbee.mqtt.exceptions;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt.message.MqttMessage;

/**
 * Exception to indicate that the packet size exceeds the maximum packet size.
 * <p>
 * This exception does not have a stack trace.
 *
 * @author Silvio Giebl
 */
public class MqttMaximumPacketSizeExceededException extends RuntimeException {

    public MqttMaximumPacketSizeExceededException(@NotNull final MqttMessage message, final int maxPacketSize) {
        // TODO replace simple name
        super("packet size exceeded for " + message.getClass().getSimpleName() + ", encoded length: " +
                message.getEncoder().encodedLength(Integer.MAX_VALUE) + ", maximum: " + maxPacketSize);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }

}
