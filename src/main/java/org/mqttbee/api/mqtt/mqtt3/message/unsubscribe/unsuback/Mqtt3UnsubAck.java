package org.mqttbee.api.mqtt.mqtt3.message.unsubscribe.unsuback;

import org.mqttbee.annotations.DoNotImplement;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt3.message.Mqtt3Message;
import org.mqttbee.api.mqtt.mqtt3.message.Mqtt3MessageType;

/**
 * MQTT 3 UNSUBACK packet.
 */
@DoNotImplement
public interface Mqtt3UnsubAck extends Mqtt3Message {

    @NotNull
    @Override
    default Mqtt3MessageType getType() {
        return Mqtt3MessageType.UNSUBACK;
    }

}
