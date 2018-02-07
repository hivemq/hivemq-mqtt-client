package org.mqttbee.api.mqtt5.message;

import com.google.common.base.Preconditions;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.message.Mqtt5QoS;
import org.mqttbee.mqtt5.message.Mqtt5TopicImpl;
import org.mqttbee.mqtt5.message.Mqtt5UTF8StringImpl;
import org.mqttbee.mqtt5.message.Mqtt5UserPropertiesImpl;
import org.mqttbee.mqtt5.message.publish.Mqtt5PayloadFormatIndicator;
import org.mqttbee.mqtt5.message.publish.Mqtt5PublishImpl;
import org.mqttbee.util.MustNotBeImplementedUtil;
import org.mqttbee.util.UnsignedDataTypes;

import static org.mqttbee.api.mqtt5.message.Mqtt5Publish.TopicAliasUsage;
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
    public Mqtt5PublishBuilder setTopic(@NotNull final Mqtt5Topic topic) {
        Preconditions.checkNotNull(topic);
        this.topic = MustNotBeImplementedUtil.checkNotImplemented(topic, Mqtt5TopicImpl.class);
        return this;
    }

    @NotNull
    public Mqtt5PublishBuilder setPayload(@NotNull final byte[] payload) { // TODO
        Preconditions.checkNotNull(payload);
        this.payload = payload;
        return this;
    }

    @NotNull
    public Mqtt5PublishBuilder setQos(@NotNull final Mqtt5QoS qos) {
        Preconditions.checkNotNull(qos);
        this.qos = qos;
        return this;
    }

    @NotNull
    public Mqtt5PublishBuilder isRetain(final boolean retain) {
        this.retain = retain;
        return this;
    }

    @NotNull
    public Mqtt5PublishBuilder setMessageExpiryInterval(final long messageExpiryInterval) {
        Preconditions.checkArgument(UnsignedDataTypes.isUnsignedInt(messageExpiryInterval));
        this.messageExpiryInterval = messageExpiryInterval;
        return this;
    }

    @NotNull
    public Mqtt5PublishBuilder setPayloadFormatIndicator(
            @NotNull final Mqtt5PayloadFormatIndicator payloadFormatIndicator) {

        Preconditions.checkNotNull(payloadFormatIndicator);
        this.payloadFormatIndicator = payloadFormatIndicator;
        return this;
    }

    @NotNull
    public Mqtt5PublishBuilder setContentType(@NotNull final Mqtt5UTF8String contentType) {
        Preconditions.checkNotNull(contentType);
        this.contentType = MustNotBeImplementedUtil.checkNotImplemented(contentType, Mqtt5UTF8StringImpl.class);
        return this;
    }

    @NotNull
    public Mqtt5PublishBuilder setResponseTopic(@NotNull final Mqtt5Topic responseTopic) {
        Preconditions.checkNotNull(responseTopic);
        this.responseTopic = MustNotBeImplementedUtil.checkNotImplemented(responseTopic, Mqtt5TopicImpl.class);
        return this;
    }

    @NotNull
    public Mqtt5PublishBuilder setCorrelationData(@NotNull final byte[] correlationData) { // TODO
        Preconditions.checkNotNull(correlationData);
        this.correlationData = correlationData;
        return this;
    }

    @NotNull
    public Mqtt5PublishBuilder setTopicAliasUsage(@NotNull final TopicAliasUsage topicAliasUsage) {
        Preconditions.checkNotNull(topicAliasUsage);
        this.topicAliasUsage = topicAliasUsage;
        return this;
    }

    @NotNull
    public Mqtt5PublishBuilder setUserProperties(@NotNull final Mqtt5UserProperties userProperties) {
        Preconditions.checkNotNull(userProperties);
        this.userProperties = MustNotBeImplementedUtil.checkNotImplemented(userProperties, Mqtt5UserPropertiesImpl.class);
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
