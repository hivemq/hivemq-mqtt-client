package org.mqttbee.mqtt.message.subscribe.mqtt3;

import com.google.common.collect.ImmutableList;
import jdk.nashorn.internal.ir.annotations.Immutable;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.datatypes.MqttQoS;
import org.mqttbee.api.mqtt.datatypes.MqttTopicFilter;
import org.mqttbee.api.mqtt.mqtt3.message.subscribe.Mqtt3Subscribe;
import org.mqttbee.mqtt.codec.encoder.mqtt3.Mqtt3SubscribeEncoder;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.message.subscribe.MqttSubscribeImpl;

/**
 * @author Silvio Giebl
 */
@Immutable
public class Mqtt3SubscribeView implements Mqtt3Subscribe {

    public static MqttSubscribeImpl wrapped(
            @NotNull final ImmutableList<MqttSubscribeImpl.SubscriptionImpl> subscriptions) {

        return new MqttSubscribeImpl(subscriptions, MqttUserPropertiesImpl.NO_USER_PROPERTIES,
                Mqtt3SubscribeEncoder.PROVIDER);
    }

    private static ImmutableList<SubscriptionView> wrapSubscriptions(
            @NotNull final ImmutableList<MqttSubscribeImpl.SubscriptionImpl> subscriptions) {

        final ImmutableList.Builder<SubscriptionView> builder =
                ImmutableList.builderWithExpectedSize(subscriptions.size());
        for (int i = 0; i < subscriptions.size(); i++) {
            builder.add(new SubscriptionView(subscriptions.get(i)));
        }
        return builder.build();
    }

    public static Mqtt3SubscribeView create(
            @NotNull final ImmutableList<MqttSubscribeImpl.SubscriptionImpl> subscriptions) {

        return new Mqtt3SubscribeView((wrapped(subscriptions)));
    }

    private final MqttSubscribeImpl wrapped;

    private Mqtt3SubscribeView(@NotNull final MqttSubscribeImpl wrapped) {
        this.wrapped = wrapped;
    }

    @NotNull
    @Override
    public ImmutableList<? extends Subscription> getSubscriptions() {
        return wrapSubscriptions(wrapped.getSubscriptions());
    }

    @NotNull
    public MqttSubscribeImpl getWrapped() {
        return wrapped;
    }


    public static class SubscriptionView implements Subscription {

        public static SubscriptionView wrapped(@NotNull final MqttSubscribeImpl.SubscriptionImpl subscription) {
            return new SubscriptionView(subscription);
        }

        private final MqttSubscribeImpl.SubscriptionImpl wrapped;

        private SubscriptionView(@NotNull final MqttSubscribeImpl.SubscriptionImpl wrapped) {
            this.wrapped = wrapped;
        }

        @NotNull
        @Override
        public MqttTopicFilter getTopicFilter() {
            return wrapped.getTopicFilter();
        }

        @NotNull
        @Override
        public MqttQoS getQoS() {
            return wrapped.getQoS();
        }

    }

}
