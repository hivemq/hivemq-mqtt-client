package org.mqttbee.mqtt.message.publish;

import org.mqttbee.mqtt.message.MqttProperty;

/**
 * All possible MQTT PUBLISH properties and its byte code according to the MQTT 5 specification.
 *
 * @author Silvio Giebl
 */
public interface MqttPublishProperty {

    int PAYLOAD_FORMAT_INDICATOR = MqttProperty.PAYLOAD_FORMAT_INDICATOR;
    int MESSAGE_EXPIRY_INTERVAL = MqttProperty.MESSAGE_EXPIRY_INTERVAL;
    int CORRELATION_DATA = MqttProperty.CORRELATION_DATA;
    int CONTENT_TYPE = MqttProperty.CONTENT_TYPE;
    int RESPONSE_TOPIC = MqttProperty.RESPONSE_TOPIC;
    int SUBSCRIPTION_IDENTIFIER = MqttProperty.SUBSCRIPTION_IDENTIFIER;
    int TOPIC_ALIAS = MqttProperty.TOPIC_ALIAS;
    int USER_PROPERTY = MqttProperty.USER_PROPERTY;

}
