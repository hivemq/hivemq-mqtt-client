package org.mqttbee.api.mqtt5.message.publish;

import com.google.common.base.Preconditions;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt5.message.Mqtt5QoS;
import org.mqttbee.api.mqtt5.message.Mqtt5Topic;
import org.mqttbee.api.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.api.mqtt5.message.Mqtt5UserProperties;
import org.mqttbee.mqtt.MqttBuilderUtil;
import org.mqttbee.mqtt.codec.encoder.mqtt5.Mqtt5PublishEncoder;
import org.mqttbee.mqtt.message.publish.MqttWillPublishImpl;
import org.mqttbee.util.MustNotBeImplementedUtil;
import org.mqttbee.util.UnsignedDataTypes;

import java.nio.ByteBuffer;

import static org.mqttbee.mqtt.message.publish.MqttWillPublishImpl.DEFAULT_DELAY_INTERVAL;

/**
 * @author Silvio Giebl
 */
public class Mqtt5WillPublishBuilder extends Mqtt5PublishBuilder {

    private long delayInterval = DEFAULT_DELAY_INTERVAL;

    Mqtt5WillPublishBuilder() {
    }

    Mqtt5WillPublishBuilder(@NotNull final Mqtt5Publish publish) {
        super(publish);
        if (publish instanceof Mqtt5WillPublish) {
            delayInterval =
                    MustNotBeImplementedUtil.checkNotImplemented(publish, MqttWillPublishImpl.class).getDelayInterval();
        }
    }

    @NotNull
    @Override
    public Mqtt5WillPublishBuilder withTopic(@NotNull final String topic) {
        super.withTopic(topic);
        return this;
    }

    @NotNull
    @Override
    public Mqtt5WillPublishBuilder withTopic(@NotNull final Mqtt5Topic topic) {
        super.withTopic(topic);
        return this;
    }

    @NotNull
    @Override
    public Mqtt5WillPublishBuilder withPayload(@Nullable final byte[] payload) {
        this.payload = MqttBuilderUtil.binaryDataOrNull(payload);
        return this;
    }

    @NotNull
    @Override
    public Mqtt5WillPublishBuilder withPayload(@Nullable final ByteBuffer payload) {
        this.payload = MqttBuilderUtil.binaryDataOrNull(payload);
        return this;
    }

    @NotNull
    @Override
    public Mqtt5WillPublishBuilder withQos(@NotNull final Mqtt5QoS qos) {
        super.withQos(qos);
        return this;
    }

    @NotNull
    @Override
    public Mqtt5WillPublishBuilder withRetain(final boolean retain) {
        super.withRetain(retain);
        return this;
    }

    @NotNull
    @Override
    public Mqtt5WillPublishBuilder withPayloadFormatIndicator(
            @Nullable final Mqtt5PayloadFormatIndicator payloadFormatIndicator) {

        super.withPayloadFormatIndicator(payloadFormatIndicator);
        return this;
    }

    @NotNull
    @Override
    public Mqtt5WillPublishBuilder withContentType(@Nullable final String contentType) {
        super.withContentType(contentType);
        return this;
    }

    @NotNull
    @Override
    public Mqtt5WillPublishBuilder withContentType(@Nullable final Mqtt5UTF8String contentType) {
        super.withContentType(contentType);
        return this;
    }

    @NotNull
    @Override
    public Mqtt5WillPublishBuilder withResponseTopic(@Nullable final String responseTopic) {
        super.withResponseTopic(responseTopic);
        return this;
    }

    @NotNull
    @Override
    public Mqtt5WillPublishBuilder withResponseTopic(@Nullable final Mqtt5Topic responseTopic) {
        super.withResponseTopic(responseTopic);
        return this;
    }

    @NotNull
    @Override
    public Mqtt5WillPublishBuilder withCorrelationData(@Nullable final byte[] correlationData) {
        super.withCorrelationData(correlationData);
        return this;
    }

    @NotNull
    @Override
    public Mqtt5WillPublishBuilder withCorrelationData(@Nullable final ByteBuffer correlationData) {
        super.withCorrelationData(correlationData);
        return this;
    }

    @NotNull
    @Override
    @Deprecated
    public Mqtt5WillPublishBuilder withTopicAliasUsage(@NotNull final TopicAliasUsage topicAliasUsage) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public Mqtt5WillPublishBuilder withUserProperties(@NotNull final Mqtt5UserProperties userProperties) {
        super.withUserProperties(userProperties);
        return this;
    }

    @NotNull
    public Mqtt5WillPublishBuilder withDelayInterval(final long delayInterval) {
        Preconditions.checkArgument(UnsignedDataTypes.isUnsignedInt(delayInterval));
        this.delayInterval = delayInterval;
        return this;
    }

    @NotNull
    @Override
    public Mqtt5WillPublish build() {
        return new MqttWillPublishImpl(topic, payload, qos, retain, messageExpiryInterval, payloadFormatIndicator,
                contentType, responseTopic, correlationData, userProperties, delayInterval,
                Mqtt5PublishEncoder.PROVIDER);
    }

}
