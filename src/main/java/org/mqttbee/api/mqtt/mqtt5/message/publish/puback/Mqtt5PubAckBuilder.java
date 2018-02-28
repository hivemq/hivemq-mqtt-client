package org.mqttbee.api.mqtt.mqtt5.message.publish.puback;

import org.mqttbee.annotations.DoNotImplement;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt5.datatypes.Mqtt5UserProperties;

/**
 * @author Silvio Giebl
 */
@DoNotImplement
public interface Mqtt5PubAckBuilder {

    @NotNull
    Mqtt5PubAckBuilder withUserProperties(@NotNull Mqtt5UserProperties userProperties);

}
