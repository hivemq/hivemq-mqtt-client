package org.mqttbee.mqtt5.message.publish;

import org.mqttbee.api.mqtt5.message.Mqtt5PublishBuilder;
import org.mqttbee.mqtt5.message.Mqtt5Message;
import org.mqttbee.mqtt5.message.Mqtt5QoS;
import org.mqttbee.mqtt5.message.Mqtt5UserProperty;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;

/**
 * @author Silvio Giebl
 */
public class Mqtt5Publish implements Mqtt5Message {

    public static Mqtt5PublishBuilder builder() {
        return new Mqtt5PublishBuilder();
    }

    private final String topic;
    private final ByteBuffer payload;
    private final Mqtt5QoS qos;
    private final boolean isRetain;
    private final int messageExpiryInterval;
    private final Mqtt5PayloadFormatIndicator payloadFormatIndicator;
    private final String contentType;
    private final String responseTopic;
    private final String correlationData;
    private final List<Mqtt5UserProperty> userProperties;

    Mqtt5Publish(final String topic, final ByteBuffer payload, final Mqtt5QoS qos, final boolean isRetain,
                 final int messageExpiryInterval, final Mqtt5PayloadFormatIndicator payloadFormatIndicator,
                 final String contentType, final String responseTopic, final String correlationData,
                 final List<Mqtt5UserProperty> userProperties) {
        this.topic = topic;
        this.payload = payload;
        this.qos = qos;
        this.isRetain = isRetain;
        this.messageExpiryInterval = messageExpiryInterval;
        this.payloadFormatIndicator = payloadFormatIndicator;
        this.contentType = contentType;
        this.responseTopic = responseTopic;
        this.correlationData = correlationData;
        this.userProperties = Collections.unmodifiableList(userProperties);
    }

    public String getTopic() {
        return topic;
    }

    public ByteBuffer getPayload() {
        return payload;
    }

    public Mqtt5QoS getQos() {
        return qos;
    }

    public boolean isRetain() {
        return isRetain;
    }

    public int getMessageExpiryInterval() {
        return messageExpiryInterval;
    }

    public Mqtt5PayloadFormatIndicator getPayloadFormatIndicator() {
        return payloadFormatIndicator;
    }

    public String getContentType() {
        return contentType;
    }

    public String getResponseTopic() {
        return responseTopic;
    }

    public String getCorrelationData() {
        return correlationData;
    }

    public List<Mqtt5UserProperty> getUserProperties() {
        return userProperties;
    }

}
