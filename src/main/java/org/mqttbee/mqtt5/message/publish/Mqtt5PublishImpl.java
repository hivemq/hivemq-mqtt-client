package org.mqttbee.mqtt5.message.publish;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt5.message.Mqtt5Publish;
import org.mqttbee.mqtt5.codec.encoder.Mqtt5MessageEncoders;
import org.mqttbee.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.mqtt5.message.Mqtt5QoS;
import org.mqttbee.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.mqtt5.message.Mqtt5UserProperty;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * @author Silvio Giebl
 */
public class Mqtt5PublishImpl implements Mqtt5Publish {

    private final Mqtt5UTF8String topic;
    private final byte[] payload;
    private final Mqtt5QoS qos;
    private final boolean isRetain;
    private final long messageExpiryInterval;
    private final Mqtt5PayloadFormatIndicator payloadFormatIndicator;
    private final Mqtt5UTF8String contentType;
    private final Mqtt5UTF8String responseTopic;
    private final byte[] correlationData;
    private final List<Mqtt5UserProperty> userProperties;

    Mqtt5PublishImpl(
            @NotNull final Mqtt5UTF8String topic, @Nullable final byte[] payload, @NotNull final Mqtt5QoS qos,
            final boolean isRetain, final long messageExpiryInterval,
            @Nullable final Mqtt5PayloadFormatIndicator payloadFormatIndicator,
            @Nullable final Mqtt5UTF8String contentType, @Nullable final Mqtt5UTF8String responseTopic,
            @Nullable final byte[] correlationData, @NotNull final List<Mqtt5UserProperty> userProperties) {
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

    @NotNull
    @Override
    public Mqtt5UTF8String getTopic() {
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

    @Override
    public long getMessageExpiryInterval() {
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
    public Optional<Mqtt5UTF8String> getResponseTopic() {
        return Optional.ofNullable(responseTopic);
    }

    @Nullable
    public Mqtt5UTF8String getRawResponseTopic() {
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
    public List<Mqtt5UserProperty> getUserProperties() {
        return userProperties;
    }

    @NotNull
    @Override
    public Mqtt5MessageType getType() {
        return Mqtt5MessageType.PUBLISH;
    }

    @Override
    public void encode(
            @NotNull final Mqtt5MessageEncoders encoders, @NotNull final Channel channel, @NotNull final ByteBuf out) {
        encoders.getPublishEncoder().encode(this, channel, out);
    }

}
