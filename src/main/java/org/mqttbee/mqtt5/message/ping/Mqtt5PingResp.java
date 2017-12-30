package org.mqttbee.mqtt5.message.ping;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.message.Mqtt5Message;
import org.mqttbee.mqtt5.message.Mqtt5MessageType;

/**
 * @author Silvio Giebl
 */
public class Mqtt5PingResp implements Mqtt5Message {

    @NotNull
    @Override
    public Mqtt5MessageType getType() {
        return Mqtt5MessageType.PINGRESP;
    }

}
