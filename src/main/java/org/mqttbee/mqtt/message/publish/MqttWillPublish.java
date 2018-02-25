package org.mqttbee.mqtt.message.publish;

import jdk.nashorn.internal.ir.annotations.Immutable;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.datatypes.MqttQoS;
import org.mqttbee.api.mqtt.mqtt5.message.publish.Mqtt5PayloadFormatIndicator;
import org.mqttbee.api.mqtt.mqtt5.message.publish.Mqtt5WillPublish;
import org.mqttbee.api.mqtt.mqtt5.message.publish.TopicAliasUsage;
import org.mqttbee.mqtt.codec.encoder.provider.MqttPublishEncoderProvider;
import org.mqttbee.mqtt.codec.encoder.provider.MqttWrappedMessageEncoderProvider;
import org.mqttbee.mqtt.datatypes.MqttTopicImpl;
import org.mqttbee.mqtt.datatypes.MqttUTF8StringImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;

import java.nio.ByteBuffer;

/**
 * @author Silvio Giebl
 */
@Immutable
public class MqttWillPublish extends MqttPublish implements Mqtt5WillPublish {

    private final long delayInterval;

    public MqttWillPublish(
            @NotNull final MqttTopicImpl topic, @Nullable final ByteBuffer payload, @NotNull final MqttQoS qos,
            final boolean isRetain, final long messageExpiryInterval,
            @Nullable final Mqtt5PayloadFormatIndicator payloadFormatIndicator,
            @Nullable final MqttUTF8StringImpl contentType, @Nullable final MqttTopicImpl responseTopic,
            @Nullable final ByteBuffer correlationData, @NotNull final MqttUserPropertiesImpl userProperties,
            final long delayInterval,
            @NotNull final MqttWrappedMessageEncoderProvider<MqttPublish, MqttPublishWrapper, MqttPublishEncoderProvider> encoderProvider) {

        super(topic, payload, qos, isRetain, messageExpiryInterval, payloadFormatIndicator, contentType, responseTopic,
                correlationData, TopicAliasUsage.MUST_NOT, userProperties, encoderProvider);
        this.delayInterval = delayInterval;
    }

    @Override
    public long getDelayInterval() {
        return delayInterval;
    }

}
