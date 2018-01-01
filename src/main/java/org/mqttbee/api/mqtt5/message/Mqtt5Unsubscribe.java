package org.mqttbee.api.mqtt5.message;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.message.Mqtt5Message;
import org.mqttbee.mqtt5.message.Mqtt5TopicFilter;
import org.mqttbee.mqtt5.message.Mqtt5UserProperty;

import java.util.List;

/**
 * @author Silvio Giebl
 */
public interface Mqtt5Unsubscribe extends Mqtt5Message {

    @NotNull
    List<Mqtt5TopicFilter> getTopicFilters();

    @NotNull
    List<Mqtt5UserProperty> getUserProperties();

}
