package org.mqttbee.mqtt.message.publish;

import com.google.common.primitives.ImmutableIntArray;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.datatypes.MqttQoS;
import org.mqttbee.api.mqtt.datatypes.MqttTopic;
import org.mqttbee.api.mqtt.datatypes.MqttUTF8String;
import org.mqttbee.api.mqtt.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.api.mqtt.mqtt5.message.publish.Mqtt5PayloadFormatIndicator;
import org.mqttbee.api.mqtt.mqtt5.message.publish.Mqtt5Publish;
import org.mqttbee.api.mqtt.mqtt5.message.publish.TopicAliasUsage;
import org.mqttbee.mqtt.codec.encoder.provider.MqttPublishEncoderProvider;
import org.mqttbee.mqtt.codec.encoder.provider.MqttWrappedMessageEncoderProvider;
import org.mqttbee.mqtt.datatypes.MqttTopicImpl;
import org.mqttbee.mqtt.datatypes.MqttUTF8StringImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.message.MqttWrappedMessage;
import org.mqttbee.util.ByteBufferUtil;

import java.nio.ByteBuffer;
import java.util.Optional;

/**
 * @author Silvio Giebl
 */
public class MqttPublishImpl extends MqttWrappedMessage<MqttPublishImpl, MqttPublishWrapper, MqttPublishEncoderProvider>
        implements Mqtt5Publish {

    public static final long MESSAGE_EXPIRY_INTERVAL_INFINITY = Long.MAX_VALUE;

    private final MqttTopicImpl topic;
    private final ByteBuffer payload;
    private final MqttQoS qos;
    private final boolean isRetain;
    private final long messageExpiryInterval;
    private final Mqtt5PayloadFormatIndicator payloadFormatIndicator;
    private final MqttUTF8StringImpl contentType;
    private final MqttTopicImpl responseTopic;
    private final ByteBuffer correlationData;
    private final TopicAliasUsage topicAliasUsage;

    public MqttPublishImpl(
            @NotNull final MqttTopicImpl topic, @Nullable final ByteBuffer payload, @NotNull final MqttQoS qos,
            final boolean isRetain, final long messageExpiryInterval,
            @Nullable final Mqtt5PayloadFormatIndicator payloadFormatIndicator,
            @Nullable final MqttUTF8StringImpl contentType, @Nullable final MqttTopicImpl responseTopic,
            @Nullable final ByteBuffer correlationData, @NotNull final TopicAliasUsage topicAliasUsage,
            @NotNull final MqttUserPropertiesImpl userProperties,
            @NotNull final MqttWrappedMessageEncoderProvider<MqttPublishImpl, MqttPublishWrapper, MqttPublishEncoderProvider> encoderProvider) {

        super(userProperties, encoderProvider);
        this.topic = topic;
        this.payload = payload;
        this.qos = qos;
        this.isRetain = isRetain;
        this.messageExpiryInterval = messageExpiryInterval;
        this.payloadFormatIndicator = payloadFormatIndicator;
        this.contentType = contentType;
        this.responseTopic = responseTopic;
        this.correlationData = correlationData;
        this.topicAliasUsage = topicAliasUsage;
    }

    @NotNull
    @Override
    public MqttTopicImpl getTopic() {
        return topic;
    }

    @NotNull
    @Override
    public Optional<ByteBuffer> getPayload() {
        return ByteBufferUtil.optionalReadOnly(payload);
    }

    @Nullable
    public ByteBuffer getRawPayload() {
        return payload;
    }

    @NotNull
    @Override
    public MqttQoS getQos() {
        return qos;
    }

    @Override
    public boolean isRetain() {
        return isRetain;
    }

    @NotNull
    @Override
    public Optional<Long> getMessageExpiryInterval() {
        return (messageExpiryInterval == MESSAGE_EXPIRY_INTERVAL_INFINITY) ? Optional.empty() :
                Optional.of(messageExpiryInterval);
    }

    public long getRawMessageExpiryInterval() {
        return messageExpiryInterval;
    }

    @NotNull
    @Override
    public Optional<Mqtt5PayloadFormatIndicator> getPayloadFormatIndicator() {
        return Optional.ofNullable(payloadFormatIndicator);
    }

    @Nullable
    public Mqtt5PayloadFormatIndicator getRawPayloadFormatIndicator() {
        return payloadFormatIndicator;
    }

    @NotNull
    @Override
    public Optional<MqttUTF8String> getContentType() {
        return Optional.ofNullable(contentType);
    }

    @Nullable
    public MqttUTF8StringImpl getRawContentType() {
        return contentType;
    }

    @NotNull
    @Override
    public Optional<MqttTopic> getResponseTopic() {
        return Optional.ofNullable(responseTopic);
    }

    @Nullable
    public MqttTopicImpl getRawResponseTopic() {
        return responseTopic;
    }

    @NotNull
    @Override
    public Optional<ByteBuffer> getCorrelationData() {
        return ByteBufferUtil.optionalReadOnly(correlationData);
    }

    @Nullable
    public ByteBuffer getRawCorrelationData() {
        return correlationData;
    }

    @NotNull
    @Override
    public TopicAliasUsage getTopicAliasUsage() {
        return topicAliasUsage;
    }

    @NotNull
    @Override
    protected MqttPublishImpl getCodable() {
        return this;
    }

    public MqttPublishWrapper wrap(
            final int packetIdentifier, final boolean isDup, final int topicAlias, final boolean isNewTopicAlias,
            @NotNull final ImmutableIntArray subscriptionIdentifiers) {

        return new MqttPublishWrapper(
                this, packetIdentifier, isDup, topicAlias, isNewTopicAlias, subscriptionIdentifiers);
    }

}
