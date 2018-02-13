package org.mqttbee.api.mqtt5.message.suback;

import com.google.common.collect.ImmutableList;
import org.mqttbee.annotations.DoNotImplement;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.api.mqtt5.message.Mqtt5UserProperties;
import org.mqttbee.api.mqtt5.message.subscribe.Mqtt5SubscribeResult;

import java.util.Optional;

/**
 * MQTT 5 SUBACK packet.
 *
 * @author Silvio Giebl
 */
@DoNotImplement
public interface Mqtt5SubAck extends Mqtt5SubscribeResult {

    /**
     * @return the reason codes of this SUBACK packet, each belonging to a subscription in the corresponding SUBSCRIBE
     * packet in the same order.
     */
    @NotNull
    ImmutableList<Mqtt5SubAckReasonCode> getReasonCodes();

    /**
     * @return the optional reason string of this SUBACK packet.
     */
    @NotNull
    Optional<Mqtt5UTF8String> getReasonString();

    /**
     * @return the optional user properties of this SUBACK packet.
     */
    @NotNull
    Mqtt5UserProperties getUserProperties();

}
