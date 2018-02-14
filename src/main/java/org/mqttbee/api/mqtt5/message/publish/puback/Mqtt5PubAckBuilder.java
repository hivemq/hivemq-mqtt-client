package org.mqttbee.api.mqtt5.message.publish.puback;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt5.message.Mqtt5UserProperties;

/**
 * @author Silvio Giebl
 */
public interface Mqtt5PubAckBuilder {

    Mqtt5PubAckBuilder withUserProperties(@NotNull Mqtt5UserProperties userProperties);

}
