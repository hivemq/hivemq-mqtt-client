package org.mqttbee.api.mqtt5.message.publish.pubcomp;

import org.mqttbee.annotations.DoNotImplement;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt5.message.Mqtt5Message;
import org.mqttbee.api.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.api.mqtt5.message.Mqtt5UserProperties;

import java.util.Optional;

/**
 * MQTT 5 PUBCOMP packet.
 *
 * @author Silvio Giebl
 */
@DoNotImplement
public interface Mqtt5PubComp extends Mqtt5Message {

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
