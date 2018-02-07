package org.mqttbee.api.mqtt5.message;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.message.Mqtt5QoS;
import org.mqttbee.mqtt5.message.Mqtt5TopicFilterImpl;
import org.mqttbee.mqtt5.message.Mqtt5UserPropertiesImpl;
import org.mqttbee.mqtt5.message.subscribe.Mqtt5RetainHandling;
import org.mqttbee.mqtt5.message.subscribe.Mqtt5SubscribeImpl;
import org.mqttbee.util.MustNotBeImplementedUtil;

import static org.mqttbee.api.mqtt5.message.Mqtt5Subscribe.Subscription;
import static org.mqttbee.mqtt5.message.subscribe.Mqtt5SubscribeImpl.SubscriptionImpl;

/**
 * @author Silvio Giebl
 */
public class Mqtt5SubscribeBuilder {

    private final ImmutableList.Builder<SubscriptionImpl> subscriptionBuilder = ImmutableList.builder();
    private Mqtt5UserPropertiesImpl userProperties = Mqtt5UserPropertiesImpl.NO_USER_PROPERTIES;

    @NotNull
    public Mqtt5SubscribeBuilder addSubscription(@NotNull final Subscription subscription) {
        Preconditions.checkNotNull(subscription);
        subscriptionBuilder.add(MustNotBeImplementedUtil.checkNotImplemented(subscription, SubscriptionImpl.class));
        return this;
    }

    @NotNull
    public Mqtt5SubscribeBuilder setUserProperties(@NotNull final Mqtt5UserProperties userProperties) {
        Preconditions.checkNotNull(userProperties);
        this.userProperties = MustNotBeImplementedUtil.checkNotImplemented(userProperties, Mqtt5UserPropertiesImpl.class);
        return this;
    }

    @NotNull
    public Mqtt5Subscribe build() {
        final ImmutableList<Mqtt5SubscribeImpl.SubscriptionImpl> subscriptions = subscriptionBuilder.build();
        Preconditions.checkState(!subscriptions.isEmpty());
        return new Mqtt5SubscribeImpl(subscriptions, userProperties);
    }


    public static class SubscriptionBuilder {

        private Mqtt5TopicFilterImpl topicFilter;
        private Mqtt5QoS qos;
        private boolean noLocal = Subscription.DEFAULT_NO_LOCAL;
        private Mqtt5RetainHandling retainHandling = Subscription.DEFAULT_RETAIN_HANDLING;
        private boolean retainAsPublished = Subscription.DEFAULT_RETAIN_AS_PUBLISHED;

        @NotNull
        public SubscriptionBuilder setTopicFilter(@NotNull final Mqtt5TopicFilter topicFilter) {
            Preconditions.checkNotNull(topicFilter);
            this.topicFilter = MustNotBeImplementedUtil.checkNotImplemented(topicFilter, Mqtt5TopicFilterImpl.class);
            return this;
        }

        @NotNull
        public SubscriptionBuilder setQoS(@NotNull final Mqtt5QoS qos) {
            Preconditions.checkNotNull(qos);
            this.qos = qos;
            return this;
        }

        @NotNull
        public SubscriptionBuilder setNoLocal(final boolean noLocal) {
            this.noLocal = noLocal;
            return this;
        }

        @NotNull
        public SubscriptionBuilder setRetainHandling(@NotNull final Mqtt5RetainHandling retainHandling) {
            Preconditions.checkNotNull(retainHandling);
            this.retainHandling = retainHandling;
            return this;
        }

        @NotNull
        public SubscriptionBuilder setRetainAsPublished(final boolean retainAsPublished) {
            this.retainAsPublished = retainAsPublished;
            return this;
        }

        @NotNull
        public Subscription build() {
            Preconditions.checkNotNull(topicFilter);
            Preconditions.checkNotNull(qos);
            return new SubscriptionImpl(topicFilter, qos, noLocal, retainHandling, retainAsPublished);
        }

    }

}
