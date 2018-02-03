package org.mqttbee.mqtt5.message.subscribe;

import com.google.common.collect.ImmutableList;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt5.message.Mqtt5Subscribe;
import org.mqttbee.mqtt5.message.Mqtt5QoS;
import org.mqttbee.mqtt5.message.Mqtt5TopicFilterImpl;
import org.mqttbee.mqtt5.message.Mqtt5UserPropertiesImpl;

/**
 * @author Silvio Giebl
 */
public class Mqtt5SubscribeImpl implements Mqtt5Subscribe {

    private final ImmutableList<SubscriptionImpl> subscriptions;
    private final Mqtt5UserPropertiesImpl userProperties;

    public Mqtt5SubscribeImpl(
            @NotNull final ImmutableList<SubscriptionImpl> subscriptions,
            @NotNull final Mqtt5UserPropertiesImpl userProperties) {
        this.subscriptions = subscriptions;
        this.userProperties = userProperties;
    }

    @NotNull
    @Override
    public ImmutableList<SubscriptionImpl> getSubscriptions() {
        return subscriptions;
    }

    @NotNull
    @Override
    public Mqtt5UserPropertiesImpl getUserProperties() {
        return userProperties;
    }


    public static class SubscriptionImpl implements Subscription {

        private final Mqtt5TopicFilterImpl topicFilter;
        private final Mqtt5QoS qos;
        private final boolean isNoLocal;
        private final Mqtt5RetainHandling retainHandling;
        private final boolean isRetainAsPublished;

        public SubscriptionImpl(
                @NotNull final Mqtt5TopicFilterImpl topicFilter, @NotNull final Mqtt5QoS qos, final boolean isNoLocal,
                @NotNull final Mqtt5RetainHandling retainHandling, final boolean isRetainAsPublished) {
            this.topicFilter = topicFilter;
            this.qos = qos;
            this.isNoLocal = isNoLocal;
            this.retainHandling = retainHandling;
            this.isRetainAsPublished = isRetainAsPublished;
        }

        @NotNull
        @Override
        public Mqtt5TopicFilterImpl getTopicFilter() {
            return topicFilter;
        }

        @NotNull
        @Override
        public Mqtt5QoS getQoS() {
            return qos;
        }

        @Override
        public boolean isNoLocal() {
            return isNoLocal;
        }

        @NotNull
        @Override
        public Mqtt5RetainHandling getRetainHandling() {
            return retainHandling;
        }

        @Override
        public boolean isRetainAsPublished() {
            return isRetainAsPublished;
        }

    }

}
