package org.mqttbee.api.mqtt.mqtt5.message.publish.pubcomp;

import org.mqttbee.annotations.DoNotImplement;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.datatypes.MqttUTF8String;
import org.mqttbee.api.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import org.mqttbee.api.mqtt.mqtt5.message.Mqtt5Message;
import org.mqttbee.api.mqtt.mqtt5.message.Mqtt5MessageType;

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
    Optional<MqttUTF8String> getReasonString();

    /**
     * @return the optional user properties of this PUBCOMP packet.
     */
    @NotNull
    Mqtt5UserProperties getUserProperties();

    @NotNull
    @Override
    default Mqtt5MessageType getType() {
        return Mqtt5MessageType.AUTH;
    }

}
