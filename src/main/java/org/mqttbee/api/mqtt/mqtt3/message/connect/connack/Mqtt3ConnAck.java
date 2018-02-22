package org.mqttbee.api.mqtt.mqtt3.message.connect.connack;

import org.mqttbee.annotations.DoNotImplement;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt3.message.Mqtt3Message;
import org.mqttbee.api.mqtt.mqtt3.message.Mqtt3MessageType;

/**
 * MQTT 3 CONNACK packet.
 */
@DoNotImplement
public interface Mqtt3ConnAck extends Mqtt3Message {

    /**
     * @return the return code of this CONNACK packet.
     */
    @NotNull
    Mqtt3ConnAckReturnCode getReturnCode();

    /**
     * @return whether the server has a session present.
     */
    boolean isSessionPresent();

    @NotNull
    @Override
    default Mqtt3MessageType getType() {
        return Mqtt3MessageType.CONNACK;
    }

}
