package org.mqttbee.mqtt.message.disconnect;

import org.mqttbee.mqtt.message.MqttProperty;

/**
 * All possible MQTT DISCONNECT properties and its byte code according to the MQTT 5 specification.
 *
 * @author Silvio Giebl
 */
public interface MqttDisconnectProperty {

    int SESSION_EXPIRY_INTERVAL = MqttProperty.SESSION_EXPIRY_INTERVAL;
    int SERVER_REFERENCE = MqttProperty.SERVER_REFERENCE;
    int REASON_STRING = MqttProperty.REASON_STRING;
    int USER_PROPERTY = MqttProperty.USER_PROPERTY;

}
