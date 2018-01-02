package org.mqttbee.mqtt5.message.subscribe;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt5.message.Mqtt5Subscribe;
import org.mqttbee.mqtt5.codec.encoder.Mqtt5MessageEncoders;
import org.mqttbee.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.mqtt5.message.Mqtt5QoS;
import org.mqttbee.mqtt5.message.Mqtt5TopicFilter;
import org.mqttbee.mqtt5.message.Mqtt5UserProperty;

/**
 * @author Silvio Giebl
 */
public class Mqtt5SubscribeImpl implements Mqtt5Subscribe {

    private final ImmutableList<SubscriptionImpl> subscriptions;
    private final ImmutableList<Mqtt5UserProperty> userProperties;

    public Mqtt5SubscribeImpl(
            @NotNull final ImmutableList<SubscriptionImpl> subscriptions,
            @NotNull final ImmutableList<Mqtt5UserProperty> userProperties) {
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
    public ImmutableList<Mqtt5UserProperty> getUserProperties() {
        return userProperties;
    }

    @NotNull
    @Override
    public Mqtt5MessageType getType() {
        return Mqtt5MessageType.SUBSCRIBE;
    }

    @Override
    public void encode(
            @NotNull final Mqtt5MessageEncoders encoders, @NotNull final Channel channel, @NotNull final ByteBuf out) {
        encoders.getSubscribeEncoder().encode(this, channel, out);
    }


    public static class SubscriptionImpl implements Subscription {

        private final Mqtt5TopicFilter topicFilter;
        private final Mqtt5QoS qos;
        private final boolean isNoLocal;
        private final Mqtt5RetainHandling retainHandling;
        private final boolean isRetainAsPublished;

        public SubscriptionImpl(
                @NotNull final Mqtt5TopicFilter topicFilter, @NotNull final Mqtt5QoS qos, final boolean isNoLocal,
                @NotNull final Mqtt5RetainHandling retainHandling, final boolean isRetainAsPublished) {
            this.topicFilter = topicFilter;
            this.qos = qos;
            this.isNoLocal = isNoLocal;
            this.retainHandling = retainHandling;
            this.isRetainAsPublished = isRetainAsPublished;
        }

        @NotNull
        @Override
        public Mqtt5TopicFilter getTopicFilter() {
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
