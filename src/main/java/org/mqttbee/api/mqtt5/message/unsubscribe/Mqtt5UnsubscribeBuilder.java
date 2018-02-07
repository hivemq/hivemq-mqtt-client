package org.mqttbee.api.mqtt5.message.unsubscribe;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt5.message.Mqtt5TopicFilter;
import org.mqttbee.api.mqtt5.message.Mqtt5UserProperties;
import org.mqttbee.mqtt5.message.Mqtt5TopicFilterImpl;
import org.mqttbee.mqtt5.message.Mqtt5UserPropertiesImpl;
import org.mqttbee.mqtt5.message.unsubscribe.Mqtt5UnsubscribeImpl;
import org.mqttbee.util.MustNotBeImplementedUtil;

/**
 * @author Silvio Giebl
 */
public class Mqtt5UnsubscribeBuilder {

    private final ImmutableList.Builder<Mqtt5TopicFilterImpl> topicFiltersBuilder = ImmutableList.builder();
    private Mqtt5UserPropertiesImpl userProperties = Mqtt5UserPropertiesImpl.NO_USER_PROPERTIES;

    @NotNull
    public Mqtt5UnsubscribeBuilder addTopicFilter(@NotNull final Mqtt5TopicFilter topicFilter) {
        Preconditions.checkNotNull(topicFilter);
        topicFiltersBuilder.add(MustNotBeImplementedUtil.checkNotImplemented(topicFilter, Mqtt5TopicFilterImpl.class));
        return this;
    }

    @NotNull
    public Mqtt5UnsubscribeBuilder withUserProperties(@NotNull final Mqtt5UserProperties userProperties) {
        Preconditions.checkNotNull(userProperties);
        this.userProperties =
                MustNotBeImplementedUtil.checkNotImplemented(userProperties, Mqtt5UserPropertiesImpl.class);
        return this;
    }

    @NotNull
    public Mqtt5Unsubscribe build() {
        final ImmutableList<Mqtt5TopicFilterImpl> topicFilters = topicFiltersBuilder.build();
        Preconditions.checkState(!topicFilters.isEmpty());
        return new Mqtt5UnsubscribeImpl(topicFilters, userProperties);
    }

}
