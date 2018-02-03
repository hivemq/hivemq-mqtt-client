package org.mqttbee.api.mqtt5.message;

import org.mqttbee.annotations.DoNotImplement;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.message.pubcomp.Mqtt5PubCompReasonCode;

import java.util.Optional;

/**
 * MQTT 5 PUBCOMP packet.
 *
 * @author Silvio Giebl
 */
@DoNotImplement
public interface Mqtt5PubComp {

    /**
     * @return the reason code of this PUBCOMP packet.
     */
    @NotNull
    Mqtt5PubCompReasonCode getReasonCode();

    /**
     * @return the optional reason string of this PUBCOMP packet.
     */
    @NotNull
    Optional<Mqtt5UTF8String> getReasonString();

    /**
     * @return the optional user properties of this PUBCOMP packet.
     */
    @NotNull
    Mqtt5UserProperties getUserProperties();

}
