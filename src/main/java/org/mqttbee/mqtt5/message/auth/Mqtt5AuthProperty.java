package org.mqttbee.mqtt5.message.auth;

import org.mqttbee.mqtt5.message.Mqtt5Property;

/**
 * @author Silvio Giebl
 */
public interface Mqtt5AuthProperty {

    int AUTHENTICATION_METHOD = Mqtt5Property.AUTHENTICATION_METHOD;
    int AUTHENTICATION_DATA = Mqtt5Property.AUTHENTICATION_DATA;
    int REASON_STRING = Mqtt5Property.REASON_STRING;
    int USER_PROPERTY = Mqtt5Property.USER_PROPERTY;

}
