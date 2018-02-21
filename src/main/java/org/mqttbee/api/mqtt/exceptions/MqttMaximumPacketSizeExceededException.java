package org.mqttbee.api.mqtt.exceptions;

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
        super("packet size exceeded for " + message.getType() + ", encoded length: " +
                message.getEncoder().encodedLength(Integer.MAX_VALUE) + ", maximum: " + maxPacketSize);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }

}
