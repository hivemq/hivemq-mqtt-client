package org.mqttbee.mqtt5.exceptions;

/**
 * @author Silvio Giebl
 */
public class Mqtt5MaximumPacketSizeExceededException extends RuntimeException {

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }

}
