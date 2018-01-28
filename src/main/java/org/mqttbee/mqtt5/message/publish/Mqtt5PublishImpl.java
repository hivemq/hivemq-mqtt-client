package org.mqttbee.mqtt5.message.publish;

import com.google.common.collect.ImmutableList;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt5.message.Mqtt5Publish;
import org.mqttbee.mqtt5.message.*;

import java.util.Optional;

/**
 * @author Silvio Giebl
 */
public class Mqtt5PublishImpl implements Mqtt5Publish {

    public static final long MESSAGE_EXPIRY_INTERVAL_INFINITY = Long.MAX_VALUE;

    private final Mqtt5Topic topic;
    private final byte[] payload;
    private final Mqtt5QoS qos;
    private final boolean isRetain;
    private final long messageExpiryInterval;
    private final Mqtt5PayloadFormatIndicator payloadFormatIndicator;
    private final Mqtt5UTF8String contentType;
    private final Mqtt5Topic responseTopic;
    private final byte[] correlationData;
    private final TopicAliasUsage topicAliasUsage;
    private final Mqtt5UserProperties userProperties;

    public Mqtt5PublishImpl(
            @NotNull final Mqtt5Topic topic, @Nullable final byte[] payload, @NotNull final Mqtt5QoS qos,
            final boolean isRetain, final long messageExpiryInterval,
            @Nullable final Mqtt5PayloadFormatIndicator payloadFormatIndicator,
            @Nullable final Mqtt5UTF8String contentType, @Nullable final Mqtt5Topic responseTopic,
            @Nullable final byte[] correlationData, @NotNull final TopicAliasUsage topicAliasUsage,
            @NotNull final Mqtt5UserProperties userProperties) {
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
        this.userProperties = userProperties;
    }

    @NotNull
    @Override
    public Mqtt5Topic getTopic() {
        return topic;
    }

    @NotNull
    @Override
    public Optional<byte[]> getPayload() {
        return Optional.ofNullable(payload);
    }

    @Nullable
    public byte[] getRawPayload() {
        return payload;
    }

    @NotNull
    @Override
    public Mqtt5QoS getQos() {
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
    public Optional<Mqtt5UTF8String> getContentType() {
        return Optional.ofNullable(contentType);
    }

    @Nullable
    public Mqtt5UTF8String getRawContentType() {
        return contentType;
    }

    @NotNull
    @Override
    public Optional<Mqtt5Topic> getResponseTopic() {
        return Optional.ofNullable(responseTopic);
    }

    @Nullable
    public Mqtt5Topic getRawResponseTopic() {
        return responseTopic;
    }

    @NotNull
    @Override
    public Optional<byte[]> getCorrelationData() {
        return Optional.ofNullable(correlationData);
    }

    @Nullable
    public byte[] getRawCorrelationData() {
        return correlationData;
    }

    @NotNull
    @Override
    public TopicAliasUsage getTopicAliasUsage() {
        return topicAliasUsage;
    }

    @NotNull
    @Override
    public ImmutableList<Mqtt5UserProperty> getUserProperties() {
        return userProperties.asList();
    }

    @NotNull
    public Mqtt5UserProperties getRawUserProperties() {
        return userProperties;
    }

}
