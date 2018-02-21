package org.mqttbee.api.mqtt5.message.subscribe;

import com.google.common.collect.ImmutableList;
import org.mqttbee.annotations.DoNotImplement;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.datatypes.MqttTopicFilter;
import org.mqttbee.api.mqtt.datatypes.mqtt5.Mqtt5UserProperties;
import org.mqttbee.api.mqtt5.message.Mqtt5Message;
import org.mqttbee.api.mqtt5.message.Mqtt5QoS;
import org.mqttbee.api.mqtt5.message.subscribe.Mqtt5SubscribeBuilder.SubscriptionBuilder;

/**
 * MQTT 5 SUBSCRIBE packet.
 *
 * @author Silvio Giebl
 */
@DoNotImplement
public interface Mqtt5Subscribe extends Mqtt5Message {

    @NotNull
    static Mqtt5SubscribeBuilder builder() {
        return new Mqtt5SubscribeBuilder();
    }

    /**
     * @return the {@link Subscription}s of this SUBSCRIBE packet. The list contains at least one subscription.
     */
    @NotNull
    ImmutableList<? extends Subscription> getSubscriptions();

    /**
     * @return the optional user properties of this SUBSCRIBE packet.
     */
    @NotNull
    Mqtt5UserProperties getUserProperties();


    interface Subscription {

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
        Mqtt5QoS getQoS();

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

}
