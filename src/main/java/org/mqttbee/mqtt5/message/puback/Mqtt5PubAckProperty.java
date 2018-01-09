package org.mqttbee.mqtt5.message.puback;

import org.mqttbee.mqtt5.message.Mqtt5Property;

/**
 * All possible MQTT PUBACK properties and its byte code according to the MQTT 5 specification.
 *
 * @author Silvio Giebl
 */
public interface Mqtt5PubAckProperty {

    int REASON_STRING = Mqtt5Property.REASON_STRING;
    int USER_PROPERTY = Mqtt5Property.USER_PROPERTY;

}
