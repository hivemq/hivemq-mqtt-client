package org.mqttbee.api.mqtt5.message;

import com.google.common.collect.ImmutableList;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.mqtt5.message.Mqtt5UserProperty;
import org.mqttbee.mqtt5.message.pubrel.Mqtt5PubRelReasonCode;

import java.util.Optional;

/**
 * MQTT 5 PUBREL packet.
 *
 * @author Silvio Giebl
 */
public interface Mqtt5PubRel {

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
    ImmutableList<Mqtt5UserProperty> getUserProperties();

}
