package org.mqttbee.api.mqtt5.message;

import com.google.common.collect.ImmutableList;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.mqtt5.message.Mqtt5UserProperty;
import org.mqttbee.mqtt5.message.suback.Mqtt5SubAckReasonCode;

import java.util.Optional;

/**
 * MQTT 5 SUBACK packet.
 *
 * @author Silvio Giebl
 */
public interface Mqtt5SubAck {

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
    ImmutableList<Mqtt5UserProperty> getUserProperties();

}
