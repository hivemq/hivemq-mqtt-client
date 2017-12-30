package org.mqttbee.mqtt5.message.pubcomp;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.message.Mqtt5Message;
import org.mqttbee.mqtt5.message.Mqtt5MessageType;

/**
 * @author Silvio Giebl
 */
public class Mqtt5PubComp implements Mqtt5Message {

//    private final int publishPacketIdentifier;
//    private final Mqtt5PubCompReasonCode reasonCode;
//    private final String reasonString;
//    private final List<Mqtt5UserProperty> userProperties;

    @NotNull
    @Override
    public Mqtt5MessageType getType() {
        return Mqtt5MessageType.PUBCOMP;
    }

}
