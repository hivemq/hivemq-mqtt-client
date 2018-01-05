package org.mqttbee.api.mqtt5.message;

/**
 * @author Silvio Giebl
 */
public interface Mqtt5WillPublish extends Mqtt5Publish {

    long DEFAULT_DELAY_INTERVAL = 0;

    long getDelayInterval();

}
