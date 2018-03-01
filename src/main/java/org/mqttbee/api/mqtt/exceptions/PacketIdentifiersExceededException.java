package org.mqttbee.api.mqtt.exceptions;

/**
 * @author Silvio Giebl
 */
public class PacketIdentifiersExceededException extends Exception {

    public static PacketIdentifiersExceededException INSTANCE = new PacketIdentifiersExceededException();

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }

}
