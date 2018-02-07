package org.mqttbee.api.mqtt5.message.auth;

import org.mqttbee.annotations.DoNotImplement;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.api.mqtt5.message.Mqtt5UserProperties;

import java.util.Optional;

/**
 * MQTT 5 AUTH packet.
 *
 * @author Silvio Giebl
 */
@DoNotImplement
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
    Mqtt5UserProperties getUserProperties();

}
