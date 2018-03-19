package org.mqttbee.mqtt5.handler.publish;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.datatypes.MqttTopic;
import org.mqttbee.util.ScLinkedList;

/**
 * @author Silvio Giebl
 */
public interface MqttSubscriptionFlows {

    void add(@NotNull MqttSubscriptionFlow subscriptionFlow);

    void remove(@NotNull MqttSubscriptionFlow subscriptionFlow);

    @NotNull
    ScLinkedList<MqttSubscriptionFlow> findMatching(@NotNull MqttTopic topic);

}
