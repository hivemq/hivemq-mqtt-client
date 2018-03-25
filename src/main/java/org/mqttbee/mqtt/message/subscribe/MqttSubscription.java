package org.mqttbee.mqtt.message.subscribe;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.datatypes.MqttQoS;
import org.mqttbee.api.mqtt.mqtt5.message.subscribe.Mqtt5RetainHandling;
import org.mqttbee.api.mqtt.mqtt5.message.subscribe.Mqtt5Subscription;
import org.mqttbee.mqtt.datatypes.MqttTopicFilterImpl;

import javax.annotation.concurrent.Immutable;

/**
 * @author Silvio Giebl
 */
@Immutable
public class MqttSubscription implements Mqtt5Subscription {

    private final MqttTopicFilterImpl topicFilter;
    private final MqttQoS qos;
    private final boolean isNoLocal;
    private final Mqtt5RetainHandling retainHandling;
    private final boolean isRetainAsPublished;

    public MqttSubscription(
            @NotNull final MqttTopicFilterImpl topicFilter, @NotNull final MqttQoS qos, final boolean isNoLocal,
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
    public MqttQoS getQoS() {
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
