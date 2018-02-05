package org.mqttbee.mqtt5.message.publish;

import io.netty.buffer.ByteBuf;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt5.message.Mqtt5Publish;
import org.mqttbee.api.mqtt5.message.Mqtt5Topic;
import org.mqttbee.api.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.mqtt5.codec.encoder.Mqtt5PublishEncoder;
import org.mqttbee.mqtt5.message.*;
import org.mqttbee.mqtt5.message.util.ByteBufUtil;

import java.util.Optional;

/**
 * @author Silvio Giebl
 */
public class Mqtt5PublishImpl extends Mqtt5Message.WrappedMqtt5MessageWithUserProperties implements Mqtt5Publish {

    public static final long MESSAGE_EXPIRY_INTERVAL_INFINITY = Long.MAX_VALUE;

    private final Mqtt5TopicImpl topic;
    private final byte[] payload;
    private final Mqtt5QoS qos;
    private final boolean isRetain;
    private final long messageExpiryInterval;
    private final Mqtt5PayloadFormatIndicator payloadFormatIndicator;
    private final Mqtt5UTF8StringImpl contentType;
    private final Mqtt5TopicImpl responseTopic;
    private final byte[] correlationData;
    private final TopicAliasUsage topicAliasUsage;

    public Mqtt5PublishImpl(
            @NotNull final Mqtt5TopicImpl topic, @Nullable final byte[] payload, @NotNull final Mqtt5QoS qos,
            final boolean isRetain, final long messageExpiryInterval,
            @Nullable final Mqtt5PayloadFormatIndicator payloadFormatIndicator,
            @Nullable final Mqtt5UTF8StringImpl contentType, @Nullable final Mqtt5TopicImpl responseTopic,
            @Nullable final byte[] correlationData, @NotNull final TopicAliasUsage topicAliasUsage,
            @NotNull final Mqtt5UserPropertiesImpl userProperties) {
        super(userProperties);
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
    public Mqtt5TopicImpl getTopic() {
        return topic;
    }

    @NotNull
    @Override
    public Optional<ByteBuf> getPayload() {
        return ByteBufUtil.optionalReadOnly(payload);
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
    public Mqtt5UTF8StringImpl getRawContentType() {
        return contentType;
    }

    @NotNull
    @Override
    public Optional<Mqtt5Topic> getResponseTopic() {
        return Optional.ofNullable(responseTopic);
    }

    @Nullable
    public Mqtt5TopicImpl getRawResponseTopic() {
        return responseTopic;
    }

    @NotNull
    @Override
    public Optional<ByteBuf> getCorrelationData() {
        return ByteBufUtil.optionalReadOnly(correlationData);
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

    @Override
    protected int calculateEncodedRemainingLengthWithoutProperties() {
        return Mqtt5PublishEncoder.INSTANCE.encodedRemainingLengthWithoutProperties(this);
    }

    @Override
    protected int calculateEncodedPropertyLength() {
        return Mqtt5PublishEncoder.INSTANCE.encodedPropertyLength(this);
    }

}
