package org.mqttbee.api.mqtt5.message.publish;

import com.google.common.base.Preconditions;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt5.message.Mqtt5QoS;
import org.mqttbee.api.mqtt5.message.Mqtt5Topic;
import org.mqttbee.api.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.api.mqtt5.message.Mqtt5UserProperties;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;
import org.mqttbee.mqtt5.codec.encoder.Mqtt5PublishEncoder;
import org.mqttbee.mqtt5.message.Mqtt5TopicImpl;
import org.mqttbee.mqtt5.message.Mqtt5UTF8StringImpl;
import org.mqttbee.mqtt5.message.Mqtt5UserPropertiesImpl;
import org.mqttbee.mqtt5.message.publish.Mqtt5PublishImpl;
import org.mqttbee.util.ByteBufferUtil;
import org.mqttbee.util.MustNotBeImplementedUtil;
import org.mqttbee.util.UnsignedDataTypes;

import java.nio.ByteBuffer;

import static org.mqttbee.mqtt5.message.publish.Mqtt5PublishImpl.DEFAULT_TOPIC_ALIAS_USAGE;
import static org.mqttbee.mqtt5.message.publish.Mqtt5PublishImpl.MESSAGE_EXPIRY_INTERVAL_INFINITY;

/**
 * @author Silvio Giebl
 */
public class Mqtt5PublishBuilder {

    Mqtt5TopicImpl topic;
    ByteBuffer payload;
    Mqtt5QoS qos;
    boolean retain;
    long messageExpiryInterval = MESSAGE_EXPIRY_INTERVAL_INFINITY;
    Mqtt5PayloadFormatIndicator payloadFormatIndicator;
    Mqtt5UTF8StringImpl contentType;
    Mqtt5TopicImpl responseTopic;
    ByteBuffer correlationData;
    private TopicAliasUsage topicAliasUsage = DEFAULT_TOPIC_ALIAS_USAGE;
    Mqtt5UserPropertiesImpl userProperties = Mqtt5UserPropertiesImpl.NO_USER_PROPERTIES;

    Mqtt5PublishBuilder() {
    }

    Mqtt5PublishBuilder(@NotNull final Mqtt5Publish publish) {
        final Mqtt5PublishImpl publishImpl =
                MustNotBeImplementedUtil.checkNotImplemented(publish, Mqtt5PublishImpl.class);
        topic = publishImpl.getTopic();
        payload = publishImpl.getRawPayload();
        qos = publishImpl.getQos();
        retain = publishImpl.isRetain();
        messageExpiryInterval = publishImpl.getRawMessageExpiryInterval();
        payloadFormatIndicator = publishImpl.getRawPayloadFormatIndicator();
        contentType = publishImpl.getRawContentType();
        responseTopic = publishImpl.getRawResponseTopic();
        correlationData = publishImpl.getRawCorrelationData();
        topicAliasUsage = publishImpl.getTopicAliasUsage();
        userProperties = publishImpl.getUserProperties();
    }

    @NotNull
    public Mqtt5PublishBuilder withTopic(@NotNull final Mqtt5Topic topic) {
        this.topic = MustNotBeImplementedUtil.checkNotImplemented(topic, Mqtt5TopicImpl.class);
        return this;
    }

    @NotNull
    public Mqtt5PublishBuilder withPayload(@Nullable final byte[] payload) {
        this.payload = ByteBufferUtil.wrap(payload);
        return this;
    }

    @NotNull
    public Mqtt5PublishBuilder withPayload(@Nullable final ByteBuffer payload) {
        this.payload = ByteBufferUtil.slice(payload);
        return this;
    }

    @NotNull
    public Mqtt5PublishBuilder withQos(@NotNull final Mqtt5QoS qos) {
        this.qos = Preconditions.checkNotNull(qos);
        return this;
    }

    @NotNull
    public Mqtt5PublishBuilder withRetain(final boolean retain) {
        this.retain = retain;
        return this;
    }

    @NotNull
    public Mqtt5PublishBuilder withMessageExpiryInterval(final long messageExpiryInterval) {
        Preconditions.checkArgument(UnsignedDataTypes.isUnsignedInt(messageExpiryInterval));
        this.messageExpiryInterval = messageExpiryInterval;
        return this;
    }

    @NotNull
    public Mqtt5PublishBuilder withPayloadFormatIndicator(
            @Nullable final Mqtt5PayloadFormatIndicator payloadFormatIndicator) {

        this.payloadFormatIndicator = payloadFormatIndicator;
        return this;
    }

    @NotNull
    public Mqtt5PublishBuilder withContentType(@Nullable final Mqtt5UTF8String contentType) {
        this.contentType = MustNotBeImplementedUtil.checkNullOrNotImplemented(contentType, Mqtt5UTF8StringImpl.class);
        return this;
    }

    @NotNull
    public Mqtt5PublishBuilder withResponseTopic(@Nullable final Mqtt5Topic responseTopic) {
        this.responseTopic = MustNotBeImplementedUtil.checkNullOrNotImplemented(responseTopic, Mqtt5TopicImpl.class);
        return this;
    }

    @NotNull
    public Mqtt5PublishBuilder withCorrelationData(@Nullable final byte[] correlationData) {
        Preconditions.checkArgument((correlationData == null) || Mqtt5DataTypes.isInBinaryDataRange(correlationData));
        this.correlationData = ByteBufferUtil.wrap(correlationData);
        return this;
    }

    @NotNull
    public Mqtt5PublishBuilder withCorrelationData(@Nullable final ByteBuffer correlationData) {
        Preconditions.checkArgument((correlationData == null) || Mqtt5DataTypes.isInBinaryDataRange(correlationData));
        this.correlationData = ByteBufferUtil.slice(correlationData);
        return this;
    }

    @NotNull
    public Mqtt5PublishBuilder withTopicAliasUsage(@NotNull final TopicAliasUsage topicAliasUsage) {
        Preconditions.checkNotNull(topicAliasUsage);
        this.topicAliasUsage = topicAliasUsage;
        return this;
    }

    @NotNull
    public Mqtt5PublishBuilder withUserProperties(@NotNull final Mqtt5UserProperties userProperties) {
        this.userProperties =
                MustNotBeImplementedUtil.checkNotImplemented(userProperties, Mqtt5UserPropertiesImpl.class);
        return this;
    }

    @NotNull
    public Mqtt5Publish build() {
        Preconditions.checkNotNull(topic);
        Preconditions.checkNotNull(qos);
        return new Mqtt5PublishImpl(topic, payload, qos, retain, messageExpiryInterval, payloadFormatIndicator,
                contentType, responseTopic, correlationData, topicAliasUsage, userProperties,
                Mqtt5PublishEncoder.PROVIDER);
    }

}
