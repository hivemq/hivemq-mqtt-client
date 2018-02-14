package org.mqttbee.api.mqtt5.message.unsubscribe.unsuback;

import com.google.common.collect.ImmutableList;
import org.mqttbee.annotations.DoNotImplement;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.api.mqtt5.message.Mqtt5UserProperties;

import java.util.Optional;

/**
 * MQTT 5 UNSUBACK packet.
 *
 * @author Silvio Giebl
 */
@DoNotImplement
public interface Mqtt5UnsubAck {

    /**
     * @return the reason codes of this UNSUBACK packet, each belonging to a topic filter in the corresponding
     * UNSUBSCRIBE packet in the same order.
     */
    @NotNull
    ImmutableList<Mqtt5UnsubAckReasonCode> getReasonCodes();

    /**
     * @return the optional reason string of this UNSUBACK packet.
     */
    @NotNull
    Optional<Mqtt5UTF8String> getReasonString();

    /**
     * @return the optional user properties of this UNSUBACK packet.
     */
    @NotNull
    Mqtt5UserProperties getUserProperties();

}
