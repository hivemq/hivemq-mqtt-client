package org.mqttbee.mqtt5.message.pubrec;

import org.mqttbee.mqtt5.message.Mqtt5Property;

/**
 * All possible MQTT PUBREC properties and its byte code according to the MQTT 5 specification.
 *
 * @author Silvio Giebl
 */
public interface Mqtt5PubRecProperty {

    int REASON_STRING = Mqtt5Property.REASON_STRING;
    int USER_PROPERTY = Mqtt5Property.USER_PROPERTY;

}
