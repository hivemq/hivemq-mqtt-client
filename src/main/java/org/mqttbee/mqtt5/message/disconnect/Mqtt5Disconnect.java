package org.mqttbee.mqtt5.message.disconnect;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.message.Mqtt5Message;
import org.mqttbee.mqtt5.message.Mqtt5MessageType;

/**
 * @author Silvio Giebl
 */
public class Mqtt5Disconnect implements Mqtt5Message {

//    private final Mqtt5DisconnectReasonCode reasonCode;
//    private final int sessionExpiryInterval;
//    private final String serverReference;
//    private final String reasonString;
//    private final List<Mqtt5UserProperty> userProperties;

    @NotNull
    @Override
    public Mqtt5MessageType getType() {
        return Mqtt5MessageType.DISCONNECT;
    }

}
