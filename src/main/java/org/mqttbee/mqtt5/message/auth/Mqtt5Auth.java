package org.mqttbee.mqtt5.message.auth;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.message.Mqtt5Message;
import org.mqttbee.mqtt5.message.Mqtt5MessageType;

/**
 * @author Silvio Giebl
 */
public class Mqtt5Auth implements Mqtt5Message {

//    private final Mqtt5AuthReasonCode reasonCode;
//    private final String reasonString;
//    private final String method;
//    private final String data;
//    private final List<Mqtt5UserProperty> userProperties;

    @NotNull
    @Override
    public Mqtt5MessageType getType() {
        return Mqtt5MessageType.AUTH;
    }

}
