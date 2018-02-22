package org.mqttbee.api.mqtt.mqtt3.message.subscribe;

import com.google.common.collect.ImmutableList;
import org.mqttbee.annotations.DoNotImplement;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.datatypes.MqttQoS;
import org.mqttbee.api.mqtt.datatypes.MqttTopicFilter;
import org.mqttbee.api.mqtt.mqtt3.message.Mqtt3Message;
import org.mqttbee.api.mqtt.mqtt3.message.Mqtt3MessageType;

/**
 * MQTT 3 SUBSCRIBE packet.
 */
@DoNotImplement
public interface Mqtt3Subscribe extends Mqtt3Message {

    /**
     * @return the {@link Subscription}s of this SUBSCRIBE packet. The list contains at least one subscription.
     */
    @NotNull
    ImmutableList<? extends Subscription> getSubscriptions();

    @NotNull
    @Override
    default Mqtt3MessageType getType() {
        return Mqtt3MessageType.SUBSCRIBE;
    }


    interface Subscription {

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

}
