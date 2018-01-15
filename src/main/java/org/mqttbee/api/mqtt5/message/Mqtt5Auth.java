package org.mqttbee.api.mqtt5.message;

import com.google.common.collect.ImmutableList;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.mqtt5.message.Mqtt5UserProperty;
import org.mqttbee.mqtt5.message.auth.Mqtt5AuthReasonCode;

import java.util.Optional;

/**
 * MQTT 5 AUTH packet.
 *
 * @author Silvio Giebl
 */
public interface Mqtt5Auth extends Mqtt5ExtendedAuth {

    /**
     * @return the reason code of this AUTH packet.
     */
    @NotNull
    Mqtt5AuthReasonCode getReasonCode();

    /**
     * @return the optional reason string of this AUTH packet.
     */
    @NotNull
    Optional<Mqtt5UTF8String> getReasonString();

    /**
     * @return the optional user properties of this AUTH packet.
     */
    @NotNull
    ImmutableList<Mqtt5UserProperty> getUserProperties();

}
