package org.mqttbee.mqtt5.exceptions;

/**
 * Exception to indicate that the maximum packet size is exceeds the restricted packet size.
 * <p>
 * This exception does not have a stack trace.
 *
 * @author Silvio Giebl
 */
public class Mqtt5MaximumPacketSizeExceededException extends RuntimeException {

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }

}
