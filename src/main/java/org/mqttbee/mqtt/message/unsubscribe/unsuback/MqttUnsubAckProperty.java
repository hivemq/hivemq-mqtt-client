package org.mqttbee.mqtt.message.unsubscribe.unsuback;

import org.mqttbee.mqtt.message.MqttProperty;

/**
 * All possible MQTT UNSUBACK properties and its byte code according to the MQTT 5 specification.
 *
 * @author Silvio Giebl
 */
public interface MqttUnsubAckProperty {

    int REASON_STRING = MqttProperty.REASON_STRING;
    int USER_PROPERTY = MqttProperty.USER_PROPERTY;

}
