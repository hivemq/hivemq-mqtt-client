package org.mqttbee.mqtt5.message.unsubscribe;

import com.google.common.collect.ImmutableList;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt5.message.Mqtt5Unsubscribe;
import org.mqttbee.mqtt5.message.Mqtt5TopicFilter;
import org.mqttbee.mqtt5.message.Mqtt5UserProperties;
import org.mqttbee.mqtt5.message.Mqtt5UserProperty;

/**
 * @author Silvio Giebl
 */
public class Mqtt5UnsubscribeImpl implements Mqtt5Unsubscribe {

    private final ImmutableList<Mqtt5TopicFilter> topicFilters;
    private final Mqtt5UserProperties userProperties;

    public Mqtt5UnsubscribeImpl(
            @NotNull final ImmutableList<Mqtt5TopicFilter> topicFilters,
            @NotNull final Mqtt5UserProperties userProperties) {
        this.topicFilters = topicFilters;
        this.userProperties = userProperties;
    }

    @NotNull
    @Override
    public ImmutableList<Mqtt5TopicFilter> getTopicFilters() {
        return topicFilters;
    }

    @NotNull
    @Override
    public ImmutableList<Mqtt5UserProperty> getUserProperties() {
        return userProperties.asList();
    }

    @NotNull
    public Mqtt5UserProperties getRawUserProperties() {
        return userProperties;
    }

}
