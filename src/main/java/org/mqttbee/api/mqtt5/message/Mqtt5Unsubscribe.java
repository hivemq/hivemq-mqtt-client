package org.mqttbee.api.mqtt5.message;

import com.google.common.collect.ImmutableList;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.message.Mqtt5TopicFilter;
import org.mqttbee.mqtt5.message.Mqtt5UserProperty;

/**
 * @author Silvio Giebl
 */
public interface Mqtt5Unsubscribe {

    @NotNull
    ImmutableList<Mqtt5TopicFilter> getTopicFilters();

    @NotNull
    ImmutableList<Mqtt5UserProperty> getUserProperties();

}
