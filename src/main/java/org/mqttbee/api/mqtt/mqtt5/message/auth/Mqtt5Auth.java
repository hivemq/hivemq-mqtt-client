package org.mqttbee.api.mqtt.mqtt5.message.auth;

import org.mqttbee.annotations.DoNotImplement;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.datatypes.MqttUTF8String;
import org.mqttbee.api.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import org.mqttbee.api.mqtt.mqtt5.message.Mqtt5Message;

import java.util.Optional;

/**
 * MQTT 5 AUTH packet.
 *
 * @author Silvio Giebl
 */
@DoNotImplement
public interface Mqtt5Auth extends Mqtt5Message, Mqtt5EnhancedAuth {

    /**
     * @return the reason code of this AUTH packet.
     */
    @NotNull
    Mqtt5AuthReasonCode getReasonCode();

    /**
     * @return the optional reason string of this AUTH packet.
     */
    @NotNull
    Optional<MqttUTF8String> getReasonString();

    /**
     * @return the optional user properties of this AUTH packet.
     */
    @NotNull
    Mqtt5UserProperties getUserProperties();

}
