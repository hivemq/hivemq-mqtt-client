package org.mqttbee.api.mqtt.mqtt5.message.subscribe;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import org.mqttbee.mqtt.codec.encoder.mqtt5.Mqtt5SubscribeEncoder;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.message.subscribe.MqttSubscribe;
import org.mqttbee.mqtt.message.subscribe.MqttSubscription;
import org.mqttbee.mqtt.util.MqttBuilderUtil;
import org.mqttbee.util.MustNotBeImplementedUtil;

/**
 * @author Silvio Giebl
 */
public class Mqtt5SubscribeBuilder {

    private final ImmutableList.Builder<MqttSubscription> subscriptionBuilder = ImmutableList.builder();
    private MqttUserPropertiesImpl userProperties = MqttUserPropertiesImpl.NO_USER_PROPERTIES;

    Mqtt5SubscribeBuilder() {
    }

    @NotNull
    public Mqtt5SubscribeBuilder addSubscription(@NotNull final Mqtt5Subscription subscription) {
        subscriptionBuilder.add(MustNotBeImplementedUtil.checkNotImplemented(subscription, MqttSubscription.class));
        return this;
    }

    @NotNull
    public Mqtt5SubscribeBuilder withUserProperties(@NotNull final Mqtt5UserProperties userProperties) {
        this.userProperties = MqttBuilderUtil.userProperties(userProperties);
        return this;
    }

    @NotNull
    public Mqtt5Subscribe build() {
        final ImmutableList<MqttSubscription> subscriptions = subscriptionBuilder.build();
        Preconditions.checkState(!subscriptions.isEmpty());
        return new MqttSubscribe(subscriptions, userProperties, Mqtt5SubscribeEncoder.PROVIDER);
    }

}
