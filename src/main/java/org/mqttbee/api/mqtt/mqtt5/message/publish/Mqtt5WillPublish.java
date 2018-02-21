package org.mqttbee.api.mqtt.mqtt5.message.publish;

import org.mqttbee.annotations.DoNotImplement;
import org.mqttbee.annotations.NotNull;

/**
 * MQTT 5 Will Publish which can be a part of the CONNECT packet.
 *
 * @author Silvio Giebl
 */
@DoNotImplement
public interface Mqtt5WillPublish extends Mqtt5Publish {

    @NotNull
    static Mqtt5WillPublishBuilder build() {
        return new Mqtt5WillPublishBuilder();
    }

    @NotNull
    static Mqtt5WillPublishBuilder extend(@NotNull final Mqtt5Publish publish) {
        return new Mqtt5WillPublishBuilder(publish);
    }

    /**
     * The default delay of Will Publishes.
     */
    long DEFAULT_DELAY_INTERVAL = 0;

    /**
     * @return the delay of this Will Publish. The default is {@link #DEFAULT_DELAY_INTERVAL}.
     */
    long getDelayInterval();

}
