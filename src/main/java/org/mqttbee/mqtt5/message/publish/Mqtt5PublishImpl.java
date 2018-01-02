package org.mqttbee.mqtt5.message.publish;

import com.google.common.collect.ImmutableList;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt5.message.Mqtt5Publish;
import org.mqttbee.mqtt5.message.Mqtt5QoS;
import org.mqttbee.mqtt5.message.Mqtt5Topic;
import org.mqttbee.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.mqtt5.message.Mqtt5UserProperty;

import java.util.Optional;

/**
 * @author Silvio Giebl
 */
public class Mqtt5PublishImpl implements Mqtt5Publish {

    private final Mqtt5Topic topic;
    private final byte[] payload;
    private final Mqtt5QoS qos;
    private final boolean isRetain;
    private final long messageExpiryInterval;
    private final Mqtt5PayloadFormatIndicator payloadFormatIndicator;
    private final Mqtt5UTF8String contentType;
    private final Mqtt5UTF8String responseTopic;
    private final byte[] correlationData;
    private final ImmutableList<Mqtt5UserProperty> userProperties;

    public Mqtt5PublishImpl(
            @NotNull final Mqtt5Topic topic, @Nullable final byte[] payload, @NotNull final Mqtt5QoS qos,
            final boolean isRetain, final long messageExpiryInterval,
            @Nullable final Mqtt5PayloadFormatIndicator payloadFormatIndicator,
            @Nullable final Mqtt5UTF8String contentType, @Nullable final Mqtt5UTF8String responseTopic,
            @Nullable final byte[] correlationData, @NotNull final ImmutableList<Mqtt5UserProperty> userProperties) {
        this.topic = topic;
        this.payload = payload;
        this.qos = qos;
        this.isRetain = isRetain;
        this.messageExpiryInterval = messageExpiryInterval;
        this.payloadFormatIndicator = payloadFormatIndicator;
        this.contentType = contentType;
        this.responseTopic = responseTopic;
        this.correlationData = correlationData;
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
    public ImmutableList<Mqtt5UserProperty> getUserProperties() {
        return userProperties;
    }

}
