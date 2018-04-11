package org.mqttbee.api.mqtt.mqtt3.message.unsubscribe;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.datatypes.MqttTopicFilter;
import org.mqttbee.api.mqtt.mqtt3.message.subscribe.Mqtt3Subscribe;
import org.mqttbee.api.mqtt.mqtt3.message.subscribe.Mqtt3Subscription;
import org.mqttbee.mqtt.datatypes.MqttTopicFilterImpl;
import org.mqttbee.mqtt.message.unsubscribe.mqtt3.Mqtt3UnsubscribeView;
import org.mqttbee.mqtt.util.MqttBuilderUtil;

/**
 * @author Silvio Giebl
 */
public class Mqtt3UnsubscribeBuilder {

    private final ImmutableList.Builder<MqttTopicFilterImpl> topicFiltersBuilder = ImmutableList.builder();

    Mqtt3UnsubscribeBuilder() {
    }

    @NotNull
    public Mqtt3UnsubscribeBuilder addTopicFilter(@NotNull final String topicFilter) {
        topicFiltersBuilder.add(MqttBuilderUtil.topicFilter(topicFilter));
        return this;
    }

    @NotNull
    public Mqtt3UnsubscribeBuilder addTopicFilter(@NotNull final MqttTopicFilter topicFilter) {
        topicFiltersBuilder.add(MqttBuilderUtil.topicFilter(topicFilter));
        return this;
    }

    @NotNull
    public Mqtt3UnsubscribeBuilder counter(@NotNull final Mqtt3Subscribe subscribe) {
        final ImmutableList<? extends Mqtt3Subscription> subscriptions = subscribe.getSubscriptions();
        for (final Mqtt3Subscription subscription : subscriptions) {
            addTopicFilter(subscription.getTopicFilter());
        }
        return this;
    }

    @NotNull
    public Mqtt3Unsubscribe build() {
        final ImmutableList<MqttTopicFilterImpl> topicFilters = topicFiltersBuilder.build();
        Preconditions.checkState(!topicFilters.isEmpty());
        return Mqtt3UnsubscribeView.create(topicFilters);
    }

}
