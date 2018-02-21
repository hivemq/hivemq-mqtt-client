package org.mqttbee.mqtt.message.subscribe;

import com.google.common.collect.ImmutableList;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt5.message.Mqtt5QoS;
import org.mqttbee.api.mqtt.mqtt5.message.subscribe.Mqtt5RetainHandling;
import org.mqttbee.api.mqtt.mqtt5.message.subscribe.Mqtt5Subscribe;
import org.mqttbee.mqtt.codec.encoder.provider.MqttMessageEncoderProvider;
import org.mqttbee.mqtt.codec.encoder.provider.MqttWrappedMessageEncoderProvider;
import org.mqttbee.mqtt.datatypes.MqttTopicFilterImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.message.MqttWrappedMessage;

/**
 * @author Silvio Giebl
 */
public class MqttSubscribeImpl extends
        MqttWrappedMessage<MqttSubscribeImpl, MqttSubscribeWrapper, MqttMessageEncoderProvider<MqttSubscribeWrapper>>
        implements Mqtt5Subscribe {

    private final ImmutableList<SubscriptionImpl> subscriptions;

    public MqttSubscribeImpl(
            @NotNull final ImmutableList<SubscriptionImpl> subscriptions,
            @NotNull final MqttUserPropertiesImpl userProperties,
            @NotNull final MqttWrappedMessageEncoderProvider<MqttSubscribeImpl, MqttSubscribeWrapper, MqttMessageEncoderProvider<MqttSubscribeWrapper>> encoderProvider) {

        super(userProperties, encoderProvider);
        this.subscriptions = subscriptions;
    }

    @NotNull
    @Override
    public ImmutableList<SubscriptionImpl> getSubscriptions() {
        return subscriptions;
    }

    @NotNull
    @Override
    protected MqttSubscribeImpl getCodable() {
        return this;
    }

    public MqttSubscribeWrapper wrap(final int packetIdentifier, final int subscriptionIdentifier) {
        return new MqttSubscribeWrapper(this, packetIdentifier, subscriptionIdentifier);
    }


    public static class SubscriptionImpl implements Subscription {

        private final MqttTopicFilterImpl topicFilter;
        private final Mqtt5QoS qos;
        private final boolean isNoLocal;
        private final Mqtt5RetainHandling retainHandling;
        private final boolean isRetainAsPublished;

        public SubscriptionImpl(
                @NotNull final MqttTopicFilterImpl topicFilter, @NotNull final Mqtt5QoS qos, final boolean isNoLocal,
                @NotNull final Mqtt5RetainHandling retainHandling, final boolean isRetainAsPublished) {
            this.topicFilter = topicFilter;
            this.qos = qos;
            this.isNoLocal = isNoLocal;
            this.retainHandling = retainHandling;
            this.isRetainAsPublished = isRetainAsPublished;
        }

        @NotNull
        @Override
        public MqttTopicFilterImpl getTopicFilter() {
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
