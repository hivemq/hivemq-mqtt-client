package org.mqttbee.api.mqtt.mqtt5.message.publish.pubrec;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt5.datatypes.Mqtt5UserProperties;

/**
 * @author Silvio Giebl
 */
public interface Mqtt5PubRecBuilder {

    Mqtt5PubRecBuilder withUserProperties(@NotNull Mqtt5UserProperties userProperties);

}
