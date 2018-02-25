package org.mqttbee.mqtt.message.subscribe.mqtt3;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.datatypes.MqttQoS;
import org.mqttbee.api.mqtt.datatypes.MqttTopicFilter;
import org.mqttbee.api.mqtt.mqtt3.message.subscribe.Mqtt3Subscription;
import org.mqttbee.mqtt.message.subscribe.MqttSubscriptionImpl;

/**
 * @author Silvio Giebl
 */
public class Mqtt3SubscriptionView implements Mqtt3Subscription {

    public static Mqtt3SubscriptionView wrapped(@NotNull final MqttSubscriptionImpl subscription) {
        return new Mqtt3SubscriptionView(subscription);
    }

    private final MqttSubscriptionImpl wrapped;

    Mqtt3SubscriptionView(@NotNull final MqttSubscriptionImpl wrapped) {
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
