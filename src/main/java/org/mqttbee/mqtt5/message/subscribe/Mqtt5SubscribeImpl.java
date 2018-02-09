package org.mqttbee.mqtt5.message.subscribe;

import com.google.common.collect.ImmutableList;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt5.message.Mqtt5QoS;
import org.mqttbee.api.mqtt5.message.subscribe.Mqtt5RetainHandling;
import org.mqttbee.api.mqtt5.message.subscribe.Mqtt5Subscribe;
import org.mqttbee.mqtt5.codec.encoder.Mqtt5WrappedMessageEncoder;
import org.mqttbee.mqtt5.message.Mqtt5TopicFilterImpl;
import org.mqttbee.mqtt5.message.Mqtt5UserPropertiesImpl;
import org.mqttbee.mqtt5.message.Mqtt5WrappedMessage;

import java.util.function.Function;

/**
 * @author Silvio Giebl
 */
public class Mqtt5SubscribeImpl extends Mqtt5WrappedMessage<Mqtt5SubscribeImpl, Mqtt5SubscribeWrapper>
        implements Mqtt5Subscribe {

    private final ImmutableList<SubscriptionImpl> subscriptions;

    public Mqtt5SubscribeImpl(
            @NotNull final ImmutableList<SubscriptionImpl> subscriptions,
            @NotNull final Mqtt5UserPropertiesImpl userProperties,
            @NotNull final Function<Mqtt5SubscribeImpl, ? extends Mqtt5WrappedMessageEncoder<Mqtt5SubscribeImpl, Mqtt5SubscribeWrapper>> encoderProvider) {

        super(userProperties, encoderProvider);
        this.subscriptions = subscriptions;
    }

    @NotNull
    @Override
    public ImmutableList<SubscriptionImpl> getSubscriptions() {
        return subscriptions;
    }

    @Override
    protected Mqtt5SubscribeImpl getCodable() {
        return this;
    }

    public Mqtt5SubscribeWrapper wrap(final int packetIdentifier, final int subscriptionIdentifier) {
        return new Mqtt5SubscribeWrapper(this, packetIdentifier, subscriptionIdentifier);
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
