package org.mqttbee.api.mqtt5.message;

import com.google.common.collect.ImmutableList;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.message.Mqtt5Message;
import org.mqttbee.mqtt5.message.Mqtt5QoS;
import org.mqttbee.mqtt5.message.Mqtt5TopicFilter;
import org.mqttbee.mqtt5.message.Mqtt5UserProperty;
import org.mqttbee.mqtt5.message.subscribe.Mqtt5RetainHandling;

/**
 * @author Silvio Giebl
 */
public interface Mqtt5Subscribe extends Mqtt5Message {

    @NotNull
    ImmutableList<? extends Subscription> getSubscriptions();

    @NotNull
    ImmutableList<Mqtt5UserProperty> getUserProperties();


    interface Subscription {

        boolean DEFAULT_NO_LOCAL = false;
        @NotNull
        Mqtt5RetainHandling DEFAULT_RETAIN_HANDLING = Mqtt5RetainHandling.SEND;
        boolean DEFAULT_RETAIN_AS_PUBLISHED = false;

        @NotNull
        Mqtt5TopicFilter getTopicFilter();

        @NotNull
        Mqtt5QoS getQoS();

        boolean isNoLocal();

        @NotNull
        Mqtt5RetainHandling getRetainHandling();

        boolean isRetainAsPublished();

    }

}
