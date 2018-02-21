package org.mqttbee.api.mqtt.mqtt3.message.connect.connack;

import org.mqttbee.annotations.NotNull;

public interface Mqtt3ConnAck {

    /**
     * @return the reason code of this CONNACK packet.
     */
    @NotNull
    Mqtt3ConnAckReturnCode getReasonCode();

    /**
     * @return whether the server has a session present.
     */
    boolean isSessionPresent();

}
