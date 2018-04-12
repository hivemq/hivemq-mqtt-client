package org.mqttbee.api.mqtt.mqtt3;

/**
 * @author Silvio Giebl
 */
public interface Mqtt3ClientConnectionData {

    int getKeepAlive();

    int getReceiveMaximum();

    boolean hasWillPublish();

}
