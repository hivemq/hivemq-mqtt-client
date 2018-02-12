package org.mqttbee.api.mqtt3.message;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt3.message.connack.Mqtt3ConnAckReasonCode;

public interface Mqtt3ConnAck {

    /**
     * @return the reason code of this CONNACK packet.
     */
    @NotNull
    Mqtt3ConnAckReasonCode getReasonCode();

    /**
     * @return whether the server has a session present.
     */
    boolean isSessionPresent();

}
