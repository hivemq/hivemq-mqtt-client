package org.mqttbee.api.mqtt.mqtt5.message.subscribe;

import com.google.common.collect.ImmutableList;
import org.mqttbee.annotations.DoNotImplement;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import org.mqttbee.api.mqtt.mqtt5.message.Mqtt5Message;
import org.mqttbee.api.mqtt.mqtt5.message.Mqtt5MessageType;

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
     * @return the {@link Mqtt5Subscription}s of this SUBSCRIBE packet. The list contains at least one subscription.
     */
    @NotNull
    ImmutableList<? extends Mqtt5Subscription> getSubscriptions();

    /**
     * @return the optional user properties of this SUBSCRIBE packet.
     */
    @NotNull
    Mqtt5UserProperties getUserProperties();

    @NotNull
    @Override
    default Mqtt5MessageType getType() {
        return Mqtt5MessageType.SUBSCRIBE;
    }


}
