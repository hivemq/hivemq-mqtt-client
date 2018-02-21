package org.mqttbee.api.mqtt5.message.publish.pubrec;

import org.mqttbee.annotations.DoNotImplement;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.datatypes.MqttUTF8String;
import org.mqttbee.api.mqtt.datatypes.mqtt5.Mqtt5UserProperties;
import org.mqttbee.api.mqtt5.message.Mqtt5Message;

import java.util.Optional;

/**
 * MQTT 5 PUBREC packet.
 *
 * @author Silvio Giebl
 */
@DoNotImplement
public interface Mqtt5PubRec extends Mqtt5Message {

    /**
     * @return the reason code of this PUBREC packet.
     */
    @NotNull
    Mqtt5PubRecReasonCode getReasonCode();

    /**
     * @return the optional reason string of this PUBREC packet.
     */
    @NotNull
    Optional<MqttUTF8String> getReasonString();

    /**
     * @return the optional user properties of this PUBREC packet.
     */
    @NotNull
    Mqtt5UserProperties getUserProperties();

}
