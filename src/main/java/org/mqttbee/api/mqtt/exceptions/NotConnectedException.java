package org.mqttbee.api.mqtt.exceptions;

/**
 * @author Silvio Giebl
 */
public class NotConnectedException extends Exception {

    public NotConnectedException() {
        super("MQTT client is not connected");
    }

}
