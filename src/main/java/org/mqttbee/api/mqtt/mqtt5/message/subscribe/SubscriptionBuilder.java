package org.mqttbee.api.mqtt.mqtt5.message.subscribe;

import com.google.common.base.Preconditions;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.datatypes.MqttQoS;
import org.mqttbee.api.mqtt.datatypes.MqttTopicFilter;
import org.mqttbee.mqtt.datatypes.MqttTopicFilterImpl;
import org.mqttbee.mqtt.message.subscribe.MqttSubscription;
import org.mqttbee.mqtt.util.MqttBuilderUtil;

/**
 * @author Silvio Giebl
 */
public class SubscriptionBuilder {

    private MqttTopicFilterImpl topicFilter;
    private MqttQoS qos;
    private boolean noLocal = Mqtt5Subscription.DEFAULT_NO_LOCAL;
    private Mqtt5RetainHandling retainHandling = Mqtt5Subscription.DEFAULT_RETAIN_HANDLING;
    private boolean retainAsPublished = Mqtt5Subscription.DEFAULT_RETAIN_AS_PUBLISHED;

    SubscriptionBuilder() {
    }

    @NotNull
    public SubscriptionBuilder withTopicFilter(@NotNull final String topicFilter) {
        this.topicFilter = MqttBuilderUtil.topicFilter(topicFilter);
        return this;
    }

    @NotNull
    public SubscriptionBuilder withTopicFilter(@NotNull final MqttTopicFilter topicFilter) {
        this.topicFilter = MqttBuilderUtil.topicFilter(topicFilter);
        return this;
    }

    @NotNull
    public SubscriptionBuilder withQoS(@NotNull final MqttQoS qos) {
        this.qos = Preconditions.checkNotNull(qos);
        return this;
    }

    @NotNull
    public SubscriptionBuilder withNoLocal(final boolean noLocal) {
        this.noLocal = noLocal;
        return this;
    }

    @NotNull
    public SubscriptionBuilder withRetainHandling(@NotNull final Mqtt5RetainHandling retainHandling) {
        this.retainHandling = Preconditions.checkNotNull(retainHandling);
        return this;
    }

    @NotNull
    public SubscriptionBuilder withRetainAsPublished(final boolean retainAsPublished) {
        this.retainAsPublished = retainAsPublished;
        return this;
    }

    @NotNull
    public Mqtt5Subscription build() {
        Preconditions.checkNotNull(topicFilter);
        Preconditions.checkNotNull(qos);
        Preconditions.checkArgument(!(topicFilter.isShared() && noLocal));
        return new MqttSubscription(topicFilter, qos, noLocal, retainHandling, retainAsPublished);
    }

}
