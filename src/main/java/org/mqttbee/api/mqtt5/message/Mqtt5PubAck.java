package org.mqttbee.api.mqtt5.message;

import org.mqttbee.annotations.DoNotImplement;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.message.puback.Mqtt5PubAckReasonCode;

import java.util.Optional;

/**
 * MQTT 5 PUBACK packet.
 *
 * @author Silvio Giebl
 */
@DoNotImplement
public interface Mqtt5PubAck {

    /**
     * @return the reason code of this PUBACK packet.
     */
    @NotNull
    Mqtt5PubAckReasonCode getReasonCode();

    /**
     * @return the optional reason string of this PUBACK packet.
     */
    @NotNull
    Optional<Mqtt5UTF8String> getReasonString();

    /**
     * @return the optional user properties of this PUBACK packet.
     */
    @NotNull
    Mqtt5UserProperties getUserProperties();

}
