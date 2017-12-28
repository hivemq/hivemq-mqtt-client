package org.mqttbee.mqtt5.message.publish;

import org.mqttbee.mqtt5.message.Mqtt5Property;

/**
 * @author Silvio Giebl
 */
public interface Mqtt5PublishProperty {

    int PAYLOAD_FORMAT_INDICATOR = Mqtt5Property.PAYLOAD_FORMAT_INDICATOR;
    int MESSAGE_EXPIRY_INTERVAL = Mqtt5Property.MESSAGE_EXPIRY_INTERVAL;
    int CORRELATION_DATA = Mqtt5Property.CORRELATION_DATA;
    int CONTENT_TYPE = Mqtt5Property.CONTENT_TYPE;
    int RESPONSE_TOPIC = Mqtt5Property.RESPONSE_TOPIC;
    int SUBSCRIPTION_IDENTIFIER = Mqtt5Property.SUBSCRIPTION_IDENTIFIER;
    int TOPIC_ALIAS = Mqtt5Property.TOPIC_ALIAS;
    int USER_PROPERTY = Mqtt5Property.USER_PROPERTY;

}
