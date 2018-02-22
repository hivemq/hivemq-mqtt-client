package org.mqttbee.api.mqtt.mqtt3.message.ping;

import org.mqttbee.annotations.DoNotImplement;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt5.message.Mqtt5Message;
import org.mqttbee.api.mqtt.mqtt5.message.Mqtt5MessageType;

/**
 * MQTT 3 PINGRESP packet.
 */
@DoNotImplement
public interface Mqtt3PingResp extends Mqtt5Message {

    @NotNull
    @Override
    default Mqtt5MessageType getType() {
        return Mqtt5MessageType.PINGRESP;
    }

}
