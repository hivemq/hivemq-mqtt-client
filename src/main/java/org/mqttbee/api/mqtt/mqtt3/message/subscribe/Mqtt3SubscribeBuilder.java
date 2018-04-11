package org.mqttbee.api.mqtt.mqtt3.message.subscribe;

import com.google.common.collect.ImmutableList;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt.message.subscribe.MqttSubscription;
import org.mqttbee.mqtt.message.subscribe.mqtt3.Mqtt3SubscribeView;
import org.mqttbee.mqtt.message.subscribe.mqtt3.Mqtt3SubscriptionView;
import org.mqttbee.util.MustNotBeImplementedUtil;

/**
 * @author Silvio Giebl
 */
public class Mqtt3SubscribeBuilder {

    private final ImmutableList.Builder<MqttSubscription> subscriptionBuilder = ImmutableList.builder();

    Mqtt3SubscribeBuilder() {
    }

    @NotNull
    public Mqtt3SubscribeBuilder addSubscription(@NotNull final Mqtt3Subscription subscription) {
        final Mqtt3SubscriptionView subscriptionView =
                MustNotBeImplementedUtil.checkNotImplemented(subscription, Mqtt3SubscriptionView.class);
        subscriptionBuilder.add(subscriptionView.getWrapped());
        return this;
    }

    @NotNull
    public Mqtt3Subscribe build() {
        return Mqtt3SubscribeView.create(subscriptionBuilder.build());
    }

}
