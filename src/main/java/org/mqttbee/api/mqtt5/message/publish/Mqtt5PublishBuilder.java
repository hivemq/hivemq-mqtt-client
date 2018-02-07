package org.mqttbee.api.mqtt5.message.publish;

import com.google.common.base.Preconditions;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt5.message.Mqtt5QoS;
import org.mqttbee.api.mqtt5.message.Mqtt5Topic;
import org.mqttbee.api.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.api.mqtt5.message.Mqtt5UserProperties;
import org.mqttbee.mqtt5.message.Mqtt5TopicImpl;
import org.mqttbee.mqtt5.message.Mqtt5UTF8StringImpl;
import org.mqttbee.mqtt5.message.Mqtt5UserPropertiesImpl;
import org.mqttbee.mqtt5.message.publish.Mqtt5PublishImpl;
import org.mqttbee.util.MustNotBeImplementedUtil;
import org.mqttbee.util.UnsignedDataTypes;

import static org.mqttbee.mqtt5.message.publish.Mqtt5PublishImpl.DEFAULT_TOPIC_ALIAS_USAGE;
import static org.mqttbee.mqtt5.message.publish.Mqtt5PublishImpl.MESSAGE_EXPIRY_INTERVAL_INFINITY;

/**
 * @author Silvio Giebl
 */
public class Mqtt5PublishBuilder {

    Mqtt5TopicImpl topic;
    byte[] payload;
    Mqtt5QoS qos;
    boolean retain;
    long messageExpiryInterval = MESSAGE_EXPIRY_INTERVAL_INFINITY;
    Mqtt5PayloadFormatIndicator payloadFormatIndicator;
    Mqtt5UTF8StringImpl contentType;
    Mqtt5TopicImpl responseTopic;
    byte[] correlationData;
    private TopicAliasUsage topicAliasUsage = DEFAULT_TOPIC_ALIAS_USAGE;
    Mqtt5UserPropertiesImpl userProperties = Mqtt5UserPropertiesImpl.NO_USER_PROPERTIES;

    @NotNull
    public Mqtt5PublishBuilder withTopic(@NotNull final Mqtt5Topic topic) {
        Preconditions.checkNotNull(topic);
        this.topic = MustNotBeImplementedUtil.checkNotImplemented(topic, Mqtt5TopicImpl.class);
        return this;
    }

    @NotNull
    public Mqtt5PublishBuilder withPayload(@NotNull final byte[] payload) { // TODO
        Preconditions.checkNotNull(payload);
        this.payload = payload;
        return this;
    }

    @NotNull
    public Mqtt5PublishBuilder withQos(@NotNull final Mqtt5QoS qos) {
        Preconditions.checkNotNull(qos);
        this.qos = qos;
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
            @NotNull final Mqtt5PayloadFormatIndicator payloadFormatIndicator) {

        Preconditions.checkNotNull(payloadFormatIndicator);
        this.payloadFormatIndicator = payloadFormatIndicator;
        return this;
    }

    @NotNull
    public Mqtt5PublishBuilder withContentType(@NotNull final Mqtt5UTF8String contentType) {
        Preconditions.checkNotNull(contentType);
        this.contentType = MustNotBeImplementedUtil.checkNotImplemented(contentType, Mqtt5UTF8StringImpl.class);
        return this;
    }

    @NotNull
    public Mqtt5PublishBuilder withResponseTopic(@NotNull final Mqtt5Topic responseTopic) {
        Preconditions.checkNotNull(responseTopic);
        this.responseTopic = MustNotBeImplementedUtil.checkNotImplemented(responseTopic, Mqtt5TopicImpl.class);
        return this;
    }

    @NotNull
    public Mqtt5PublishBuilder withCorrelationData(@NotNull final byte[] correlationData) { // TODO
        Preconditions.checkNotNull(correlationData);
        this.correlationData = correlationData;
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
        Preconditions.checkNotNull(userProperties);
        this.userProperties =
                MustNotBeImplementedUtil.checkNotImplemented(userProperties, Mqtt5UserPropertiesImpl.class);
        return this;
    }

    @NotNull
    public Mqtt5Publish build() {
        Preconditions.checkNotNull(topic);
        Preconditions.checkNotNull(qos);
        return new Mqtt5PublishImpl(topic, payload, qos, retain, messageExpiryInterval, payloadFormatIndicator,
                contentType, responseTopic, correlationData, topicAliasUsage, userProperties);
    }

}
