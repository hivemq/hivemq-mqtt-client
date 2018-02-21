package org.mqttbee.mqtt.message.publish;

import org.mqttbee.mqtt.message.MqttProperty;

/**
 * All possible MQTT Will Publish properties and its byte code according to the MQTT 5 specification.
 *
 * @author Silvio Giebl
 */
public interface MqttWillPublishProperty {

    int PAYLOAD_FORMAT_INDICATOR = MqttProperty.PAYLOAD_FORMAT_INDICATOR;
    int MESSAGE_EXPIRY_INTERVAL = MqttProperty.MESSAGE_EXPIRY_INTERVAL;
    int CORRELATION_DATA = MqttProperty.CORRELATION_DATA;
    int CONTENT_TYPE = MqttProperty.CONTENT_TYPE;
    int RESPONSE_TOPIC = MqttProperty.RESPONSE_TOPIC;
    int WILL_DELAY_INTERVAL = MqttProperty.WILL_DELAY_INTERVAL;
    int USER_PROPERTY = MqttProperty.USER_PROPERTY;

}
