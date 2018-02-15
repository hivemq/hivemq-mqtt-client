package org.mqttbee.api.mqtt5.message.publish.pubrel;

import org.mqttbee.annotations.DoNotImplement;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt5.message.Mqtt5Message;
import org.mqttbee.api.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.api.mqtt5.message.Mqtt5UserProperties;

import java.util.Optional;

/**
 * MQTT 5 PUBREL packet.
 *
 * @author Silvio Giebl
 */
@DoNotImplement
public interface Mqtt5PubRel extends Mqtt5Message {

    /**
     * @return the reason code of this PUBREL packet.
     */
    @NotNull
    Mqtt5PubRelReasonCode getReasonCode();

    /**
     * @return the optional reason string of this PUBREL packet.
     */
    @NotNull
    Optional<Mqtt5UTF8String> getReasonString();

    /**
     * @return the optional user properties of this PUBREL packet.
     */
    @NotNull
    Mqtt5UserProperties getUserProperties();

}
