package org.mqttbee.mqtt5.message.unsubscribe;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.message.Mqtt5Message;
import org.mqttbee.mqtt5.message.Mqtt5MessageType;

/**
 * @author Silvio Giebl
 */
public class Mqtt5Unsubscribe implements Mqtt5Message {

//    private final int unsubscribeIdentifier; // TODO remove?
//    private final List<String> topicFilters;
//    private final List<Mqtt5UserProperty> userProperties;

    @NotNull
    @Override
    public Mqtt5MessageType getType() {
        return Mqtt5MessageType.UNSUBSCRIBE;
    }

}
