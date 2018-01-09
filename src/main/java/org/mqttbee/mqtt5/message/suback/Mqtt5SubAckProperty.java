package org.mqttbee.mqtt5.message.suback;

import org.mqttbee.mqtt5.message.Mqtt5Property;

/**
 * All possible MQTT SUBACK properties and its byte code according to the MQTT 5 specification.
 *
 * @author Silvio Giebl
 */
public interface Mqtt5SubAckProperty {

    int REASON_STRING = Mqtt5Property.REASON_STRING;
    int USER_PROPERTY = Mqtt5Property.USER_PROPERTY;

}
