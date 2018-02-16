package org.mqttbee.api.mqtt5.exception;

/**
 * @author Silvio Giebl
 */
public class AlreadyConnectedException extends Exception {

    public AlreadyConnectedException(final boolean connecting) {
        super("MQTT client is already " + (connecting ? "connecting" : "connected"));
    }

}
