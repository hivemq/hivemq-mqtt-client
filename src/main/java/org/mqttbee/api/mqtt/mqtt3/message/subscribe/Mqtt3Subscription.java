package org.mqttbee.api.mqtt.mqtt3.message.subscribe;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.datatypes.MqttQoS;
import org.mqttbee.api.mqtt.datatypes.MqttTopicFilter;

/**
 * Subscription in the MQTT 3 SUBSCRIBE packet.
 *
 * @author Silvio Giebl
 */
public interface Mqtt3Subscription {

    @NotNull
    static Mqtt3SubscriptionBuilder builder() {
        return new Mqtt3SubscriptionBuilder();
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

}
