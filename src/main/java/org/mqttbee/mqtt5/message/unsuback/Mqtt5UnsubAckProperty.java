package org.mqttbee.mqtt5.message.unsuback;

import org.mqttbee.mqtt5.message.Mqtt5Property;

/**
 * @author Silvio Giebl
 */
public interface Mqtt5UnsubAckProperty {

    int REASON_STRING = Mqtt5Property.REASON_STRING;
    int USER_PROPERTY = Mqtt5Property.USER_PROPERTY;

}
