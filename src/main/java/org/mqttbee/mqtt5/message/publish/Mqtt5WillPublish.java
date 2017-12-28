package org.mqttbee.mqtt5.message.publish;

import org.mqttbee.mqtt5.message.Mqtt5QoS;
import org.mqttbee.mqtt5.message.Mqtt5UserProperty;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * @author Silvio Giebl
 */
public class Mqtt5WillPublish extends Mqtt5Publish {

    private final int delay;

    Mqtt5WillPublish(final String topic, final ByteBuffer payload, final Mqtt5QoS qos, final boolean isRetain,
                     final int messageExpiryInterval, final Mqtt5PayloadFormatIndicator payloadFormatIndicator,
                     final String contentType, final String responseTopic, final String correlationData,
                     final List<Mqtt5UserProperty> userProperties, final int delay) {
        super(topic, payload, qos, isRetain, messageExpiryInterval, payloadFormatIndicator, contentType, responseTopic, correlationData, userProperties);
        this.delay = delay;
    }

    public int getDelay() {
        return delay;
    }

}
