package org.mqttbee.api.mqtt.mqtt5.message.unsubscribe;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.datatypes.MqttTopicFilter;
import org.mqttbee.api.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import org.mqttbee.mqtt.codec.encoder.mqtt5.Mqtt5UnsubscribeEncoder;
import org.mqttbee.mqtt.datatypes.MqttTopicFilterImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.message.unsubscribe.MqttUnsubscribe;
import org.mqttbee.mqtt.util.MqttBuilderUtil;
import org.mqttbee.util.MustNotBeImplementedUtil;

/**
 * @author Silvio Giebl
 */
public class Mqtt5UnsubscribeBuilder {

    private final ImmutableList.Builder<MqttTopicFilterImpl> topicFiltersBuilder = ImmutableList.builder();
    private MqttUserPropertiesImpl userProperties = MqttUserPropertiesImpl.NO_USER_PROPERTIES;

    Mqtt5UnsubscribeBuilder() {
    }

    @NotNull
    public Mqtt5UnsubscribeBuilder addTopicFilter(@NotNull final String topicFilter) {
        topicFiltersBuilder.add(MqttBuilderUtil.topicFilter(topicFilter));
        return this;
    }

    @NotNull
    public Mqtt5UnsubscribeBuilder addTopicFilter(@NotNull final MqttTopicFilter topicFilter) {
        topicFiltersBuilder.add(MqttBuilderUtil.topicFilter(topicFilter));
        return this;
    }

    @NotNull
    public Mqtt5UnsubscribeBuilder withUserProperties(@NotNull final Mqtt5UserProperties userProperties) {
        this.userProperties =
                MustNotBeImplementedUtil.checkNotImplemented(userProperties, MqttUserPropertiesImpl.class);
        return this;
    }

    @NotNull
    public Mqtt5Unsubscribe build() {
        final ImmutableList<MqttTopicFilterImpl> topicFilters = topicFiltersBuilder.build();
        Preconditions.checkState(!topicFilters.isEmpty());
        return new MqttUnsubscribe(topicFilters, userProperties, Mqtt5UnsubscribeEncoder.PROVIDER);
    }

}
