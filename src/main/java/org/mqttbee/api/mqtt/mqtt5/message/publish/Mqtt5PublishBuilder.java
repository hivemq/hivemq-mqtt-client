package org.mqttbee.api.mqtt.mqtt5.message.publish;

import com.google.common.base.Preconditions;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.datatypes.MqttTopic;
import org.mqttbee.api.mqtt.datatypes.MqttUTF8String;
import org.mqttbee.api.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import org.mqttbee.api.mqtt.mqtt5.message.Mqtt5QoS;
import org.mqttbee.mqtt.MqttBuilderUtil;
import org.mqttbee.mqtt.codec.encoder.mqtt5.Mqtt5PublishEncoder;
import org.mqttbee.mqtt.datatypes.MqttTopicImpl;
import org.mqttbee.mqtt.datatypes.MqttUTF8StringImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.message.publish.MqttPublishImpl;
import org.mqttbee.util.ByteBufferUtil;
import org.mqttbee.util.MustNotBeImplementedUtil;
import org.mqttbee.util.UnsignedDataTypes;

import java.nio.ByteBuffer;

import static org.mqttbee.mqtt.message.publish.MqttPublishImpl.DEFAULT_TOPIC_ALIAS_USAGE;
import static org.mqttbee.mqtt.message.publish.MqttPublishImpl.MESSAGE_EXPIRY_INTERVAL_INFINITY;

/**
 * @author Silvio Giebl
 */
public class Mqtt5PublishBuilder {

    MqttTopicImpl topic;
    ByteBuffer payload;
    Mqtt5QoS qos;
    boolean retain;
    long messageExpiryInterval = MESSAGE_EXPIRY_INTERVAL_INFINITY;
    Mqtt5PayloadFormatIndicator payloadFormatIndicator;
    MqttUTF8StringImpl contentType;
    MqttTopicImpl responseTopic;
    ByteBuffer correlationData;
    private TopicAliasUsage topicAliasUsage = DEFAULT_TOPIC_ALIAS_USAGE;
    MqttUserPropertiesImpl userProperties = MqttUserPropertiesImpl.NO_USER_PROPERTIES;

    Mqtt5PublishBuilder() {
    }

    Mqtt5PublishBuilder(@NotNull final Mqtt5Publish publish) {
        final MqttPublishImpl publishImpl =
                MustNotBeImplementedUtil.checkNotImplemented(publish, MqttPublishImpl.class);
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
    public Mqtt5PublishBuilder withTopic(@NotNull final String topic) {
        this.topic = MqttBuilderUtil.topic(topic);
        return this;
    }

    @NotNull
    public Mqtt5PublishBuilder withTopic(@NotNull final MqttTopic topic) {
        this.topic = MqttBuilderUtil.topic(topic);
        return this;
    }

    @NotNull
    public Mqtt5PublishBuilder withPayload(@Nullable final byte[] payload) {
        this.payload = (payload == null) ? null : ByteBufferUtil.wrap(payload);
        return this;
    }

    @NotNull
    public Mqtt5PublishBuilder withPayload(@Nullable final ByteBuffer payload) {
        this.payload = (payload == null) ? null : ByteBufferUtil.slice(payload);
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
    public Mqtt5PublishBuilder withContentType(@Nullable final String contentType) {
        this.contentType = MqttBuilderUtil.stringOrNull(contentType);
        return this;
    }

    @NotNull
    public Mqtt5PublishBuilder withContentType(@Nullable final MqttUTF8String contentType) {
        this.contentType = MqttBuilderUtil.stringOrNull(contentType);
        return this;
    }

    @NotNull
    public Mqtt5PublishBuilder withResponseTopic(@Nullable final String responseTopic) {
        this.responseTopic = MqttBuilderUtil.topicOrNull(responseTopic);
        return this;
    }

    @NotNull
    public Mqtt5PublishBuilder withResponseTopic(@Nullable final MqttTopic responseTopic) {
        this.responseTopic = MqttBuilderUtil.topicOrNull(responseTopic);
        return this;
    }

    @NotNull
    public Mqtt5PublishBuilder withCorrelationData(@Nullable final byte[] correlationData) {
        this.correlationData = MqttBuilderUtil.binaryDataOrNull(correlationData);
        return this;
    }

    @NotNull
    public Mqtt5PublishBuilder withCorrelationData(@Nullable final ByteBuffer correlationData) {
        this.correlationData = MqttBuilderUtil.binaryDataOrNull(correlationData);
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
                MustNotBeImplementedUtil.checkNotImplemented(userProperties, MqttUserPropertiesImpl.class);
        return this;
    }

    @NotNull
    public Mqtt5Publish build() {
        Preconditions.checkNotNull(topic);
        Preconditions.checkNotNull(qos);
        return new MqttPublishImpl(topic, payload, qos, retain, messageExpiryInterval, payloadFormatIndicator,
                contentType, responseTopic, correlationData, topicAliasUsage, userProperties,
                Mqtt5PublishEncoder.PROVIDER);
    }

}
