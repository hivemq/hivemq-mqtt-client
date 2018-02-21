package org.mqttbee.api.mqtt5.message.publish.puback;

import org.mqttbee.annotations.DoNotImplement;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.datatypes.MqttUTF8String;
import org.mqttbee.api.mqtt.datatypes.mqtt5.Mqtt5UserProperties;
import org.mqttbee.api.mqtt5.message.Mqtt5Message;

import java.util.Optional;

/**
 * MQTT 5 PUBACK packet.
 *
 * @author Silvio Giebl
 */
@DoNotImplement
public interface Mqtt5PubAck extends Mqtt5Message {

    /**
     * @return the reason code of this PUBACK packet.
     */
    @NotNull
    Mqtt5PubAckReasonCode getReasonCode();

    /**
     * @return the optional reason string of this PUBACK packet.
     */
    @NotNull
    Optional<MqttUTF8String> getReasonString();

    /**
     * @return the optional user properties of this PUBACK packet.
     */
    @NotNull
    Mqtt5UserProperties getUserProperties();

}
