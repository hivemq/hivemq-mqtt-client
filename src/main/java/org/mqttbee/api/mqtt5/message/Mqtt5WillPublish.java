package org.mqttbee.api.mqtt5.message;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.mqtt5.message.publish.Mqtt5WillPublishImpl;

/**
 * @author Silvio Giebl
 */
public interface Mqtt5WillPublish extends Mqtt5Publish {

    @Nullable
    Mqtt5WillPublishImpl DEFAULT_NO_WILL_PUBLISH = null;
    @NotNull
    byte[] DEFAULT_NO_PAYLOAD = new byte[0];
    long DEFAULT_DELAY_INTERVAL = 0;

    long getDelayInterval();

}
