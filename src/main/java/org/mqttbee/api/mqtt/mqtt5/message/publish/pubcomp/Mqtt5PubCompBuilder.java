package org.mqttbee.api.mqtt.mqtt5.message.publish.pubcomp;

import org.mqttbee.annotations.DoNotImplement;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt5.datatypes.Mqtt5UserProperties;

/**
 * @author Silvio Giebl
 */
@DoNotImplement
public interface Mqtt5PubCompBuilder {

    @NotNull
    Mqtt5PubCompBuilder withUserProperties(@NotNull Mqtt5UserProperties userProperties);

}
