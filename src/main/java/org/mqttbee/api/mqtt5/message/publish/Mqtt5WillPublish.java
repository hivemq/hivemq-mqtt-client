package org.mqttbee.api.mqtt5.message.publish;

import org.mqttbee.annotations.DoNotImplement;

/**
 * MQTT 5 Will Publish which can be a part of the CONNECT packet.
 *
 * @author Silvio Giebl
 */
@DoNotImplement
public interface Mqtt5WillPublish extends Mqtt5Publish {

    /**
     * The default delay of Will Publishes.
     */
    long DEFAULT_DELAY_INTERVAL = 0;

    /**
     * @return the delay of this Will Publish. The default is {@link #DEFAULT_DELAY_INTERVAL}.
     */
    long getDelayInterval();

}
