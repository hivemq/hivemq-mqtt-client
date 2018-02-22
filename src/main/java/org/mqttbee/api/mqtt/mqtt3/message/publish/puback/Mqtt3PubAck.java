package org.mqttbee.api.mqtt.mqtt3.message.publish.puback;

import org.mqttbee.annotations.DoNotImplement;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt3.message.Mqtt3Message;
import org.mqttbee.api.mqtt.mqtt3.message.Mqtt3MessageType;

/**
 * MQTT 3 PUBACK packet.
 */
@DoNotImplement
public interface Mqtt3PubAck extends Mqtt3Message {

    @NotNull
    @Override
    default Mqtt3MessageType getType() {
        return Mqtt3MessageType.PUBACK;
    }

}
