package org.mqttbee.api.mqtt.mqtt5.message.subscribe;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.datatypes.MqttQoS;
import org.mqttbee.api.mqtt.datatypes.MqttTopicFilter;

/**
 * Subscription in the MQTT 5 SUBSCRIBE packet.
 *
 * @author Silvio Giebl
 */
public interface Mqtt5Subscription {

    /**
     * The default for whether the client must not receive messages published by itself.
     */
    boolean DEFAULT_NO_LOCAL = false;
    /**
     * The default handling of retained message.
     */
    @NotNull
    Mqtt5RetainHandling DEFAULT_RETAIN_HANDLING = Mqtt5RetainHandling.SEND;
    /**
     * The default for whether the retain flag for incoming publishes must be set to its original value.
     */
    boolean DEFAULT_RETAIN_AS_PUBLISHED = false;

    @NotNull
    static SubscriptionBuilder builder() {
        return new SubscriptionBuilder();
    }

    /**
     * @return the Topic Filter of this subscription.
     */
    @NotNull
    MqttTopicFilter getTopicFilter();

    /**
     * @return the QoS of this subscription.
     */
    @NotNull
    MqttQoS getQoS();

    /**
     * @return whether the client must not receive messages published by itself. The default is {@link
     * #DEFAULT_NO_LOCAL}.
     */
    boolean isNoLocal();

    /**
     * @return the handling of retained message for this subscription. The default is {@link
     * #DEFAULT_RETAIN_HANDLING}.
     */
    @NotNull
    Mqtt5RetainHandling getRetainHandling();

    /**
     * @return whether the retain flag for incoming publishes must be set to its original value.
     */
    boolean isRetainAsPublished();

}
