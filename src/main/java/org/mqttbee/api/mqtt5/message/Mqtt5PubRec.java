package org.mqttbee.api.mqtt5.message;

import com.google.common.collect.ImmutableList;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.mqtt5.message.Mqtt5UserProperty;
import org.mqttbee.mqtt5.message.pubrec.Mqtt5PubRecReasonCode;

import java.util.Optional;

/**
 * MQTT 5 PUBREC packet.
 *
 * @author Silvio Giebl
 */
public interface Mqtt5PubRec {

    /**
     * @return the reason code of this PUBREC packet.
     */
    @NotNull
    Mqtt5PubRecReasonCode getReasonCode();

    /**
     * @return the optional reason string of this PUBREC packet.
     */
    @NotNull
    Optional<Mqtt5UTF8String> getReasonString();

    /**
     * @return the optional user properties of this PUBREC packet.
     */
    @NotNull
    ImmutableList<Mqtt5UserProperty> getUserProperties();

}
