package org.mqttbee.mqtt5.message.unsubscribe;

import com.google.common.collect.ImmutableList;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt5.message.Mqtt5Unsubscribe;
import org.mqttbee.mqtt5.message.Mqtt5TopicFilterImpl;
import org.mqttbee.mqtt5.message.Mqtt5UserPropertiesImpl;

/**
 * @author Silvio Giebl
 */
public class Mqtt5UnsubscribeImpl implements Mqtt5Unsubscribe {

    private final ImmutableList<Mqtt5TopicFilterImpl> topicFilters;
    private final Mqtt5UserPropertiesImpl userProperties;

    public Mqtt5UnsubscribeImpl(
            @NotNull final ImmutableList<Mqtt5TopicFilterImpl> topicFilters,
            @NotNull final Mqtt5UserPropertiesImpl userProperties) {
        this.topicFilters = topicFilters;
        this.userProperties = userProperties;
    }

    @NotNull
    @Override
    public ImmutableList<Mqtt5TopicFilterImpl> getTopicFilters() {
        return topicFilters;
    }

    @NotNull
    @Override
    public Mqtt5UserPropertiesImpl getUserProperties() {
        return userProperties;
    }

}
