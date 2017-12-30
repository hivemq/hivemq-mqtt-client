package org.mqttbee.mqtt5.message.unsuback;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.message.Mqtt5Message;
import org.mqttbee.mqtt5.message.Mqtt5MessageType;

/**
 * @author Silvio Giebl
 */
public class Mqtt5UnsubAck implements Mqtt5Message {

//    private final int unsubscribePacketIdentifier;
//    private final List<Mqtt5UnsubAckReasonCode> reasonCodes;
//    private final String reasonString;
//    private final List<Mqtt5UserProperty> userProperties;

    @NotNull
    @Override
    public Mqtt5MessageType getType() {
        return Mqtt5MessageType.UNSUBACK;
    }

}
