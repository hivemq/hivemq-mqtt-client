package org.mqttbee.mqtt5.handler.publish;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.mqtt.datatypes.MqttTopicFilterImpl;
import org.mqttbee.mqtt.datatypes.MqttTopicImpl;
import org.mqttbee.util.collections.ScNodeList;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.function.Consumer;

/**
 * @author Silvio Giebl
 */
@NotThreadSafe
public interface MqttSubscriptionFlows {

    void subscribe(@NotNull final MqttTopicFilterImpl topicFilter, @NotNull MqttSubscriptionFlow flow);

    void unsubscribe(
            @NotNull final MqttTopicFilterImpl topicFilter,
            @Nullable final Consumer<MqttSubscriptionFlow> unsubscribedCallback);

    void cancel(@NotNull MqttSubscriptionFlow flow);

    boolean findMatching(@NotNull MqttTopicImpl topic, final ScNodeList<MqttIncomingPublishFlow> matchingFlows);

}
