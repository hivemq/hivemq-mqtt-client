package org.mqttbee.mqtt5.message.disconnect;

import org.mqttbee.mqtt5.message.Mqtt5Property;

/**
 * All possible MQTT DISCONNECT properties and its byte code according to the MQTT 5 specification.
 *
 * @author Silvio Giebl
 */
public interface Mqtt5DisconnectProperty {

    int SESSION_EXPIRY_INTERVAL = Mqtt5Property.SESSION_EXPIRY_INTERVAL;
    int SERVER_REFERENCE = Mqtt5Property.SERVER_REFERENCE;
    int REASON_STRING = Mqtt5Property.REASON_STRING;
    int USER_PROPERTY = Mqtt5Property.USER_PROPERTY;

}
