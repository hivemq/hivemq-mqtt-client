/*
 * Copyright 2018 dc-square and the HiveMQ MQTT Client Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.hivemq.client.internal.mqtt.message.publish;

import com.hivemq.client.annotations.Immutable;
import com.hivemq.client.internal.checkpoint.Confirmable;
import com.hivemq.client.internal.mqtt.datatypes.MqttTopicImpl;
import com.hivemq.client.internal.mqtt.datatypes.MqttUserPropertiesImpl;
import com.hivemq.client.internal.mqtt.datatypes.MqttUtf8StringImpl;
import com.hivemq.client.internal.mqtt.handler.publish.outgoing.MqttTopicAliasMapping;
import com.hivemq.client.internal.mqtt.message.MqttMessageWithUserProperties;
import com.hivemq.client.internal.util.ByteBufferUtil;
import com.hivemq.client.internal.util.StringUtil;
import com.hivemq.client.internal.util.collections.ImmutableIntList;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.datatypes.MqttTopic;
import com.hivemq.client.mqtt.datatypes.MqttUtf8String;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PayloadFormatIndicator;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;

import static com.hivemq.client.internal.mqtt.message.publish.MqttStatefulPublish.DEFAULT_NO_SUBSCRIPTION_IDENTIFIERS;
import static com.hivemq.client.internal.mqtt.message.publish.MqttStatefulPublish.DEFAULT_NO_TOPIC_ALIAS;

/**
 * @author Silvio Giebl
 */
@Immutable
public class MqttPublish extends MqttMessageWithUserProperties implements Mqtt5Publish {

    public static final long NO_MESSAGE_EXPIRY = Long.MAX_VALUE;

    private final @NotNull MqttTopicImpl topic;
    private final @Nullable ByteBuffer payload;
    private final @NotNull MqttQos qos;
    private final boolean retain;
    private final long messageExpiryInterval;
    private final @Nullable Mqtt5PayloadFormatIndicator payloadFormatIndicator;
    private final @Nullable MqttUtf8StringImpl contentType;
    private final @Nullable MqttTopicImpl responseTopic;
    private final @Nullable ByteBuffer correlationData;

    private final @Nullable Confirmable confirmable;

    public MqttPublish(
            final @NotNull MqttTopicImpl topic, final @Nullable ByteBuffer payload, final @NotNull MqttQos qos,
            final boolean retain, final long messageExpiryInterval,
            final @Nullable Mqtt5PayloadFormatIndicator payloadFormatIndicator,
            final @Nullable MqttUtf8StringImpl contentType, final @Nullable MqttTopicImpl responseTopic,
            final @Nullable ByteBuffer correlationData, final @NotNull MqttUserPropertiesImpl userProperties,
            final @Nullable Confirmable confirmable) {

        super(userProperties);
        this.topic = topic;
        this.payload = payload;
        this.qos = qos;
        this.retain = retain;
        this.messageExpiryInterval = messageExpiryInterval;
        this.payloadFormatIndicator = payloadFormatIndicator;
        this.contentType = contentType;
        this.responseTopic = responseTopic;
        this.correlationData = correlationData;
        this.confirmable = confirmable;
    }

    @Override
    public @NotNull MqttTopicImpl getTopic() {
        return topic;
    }

    @Override
    public @NotNull Optional<ByteBuffer> getPayload() {
        return ByteBufferUtil.optionalReadOnly(payload);
    }

    public @Nullable ByteBuffer getRawPayload() {
        return payload;
    }

    @Override
    public byte @NotNull [] getPayloadAsBytes() {
        return ByteBufferUtil.copyBytes(payload);
    }

    @Override
    public @NotNull MqttQos getQos() {
        return qos;
    }

    @Override
    public boolean isRetain() {
        return retain;
    }

    @Override
    public @NotNull OptionalLong getMessageExpiryInterval() {
        return (messageExpiryInterval == NO_MESSAGE_EXPIRY) ? OptionalLong.empty() :
                OptionalLong.of(messageExpiryInterval);
    }

    public long getRawMessageExpiryInterval() {
        return messageExpiryInterval;
    }

    @Override
    public @NotNull Optional<Mqtt5PayloadFormatIndicator> getPayloadFormatIndicator() {
        return Optional.ofNullable(payloadFormatIndicator);
    }

    public @Nullable Mqtt5PayloadFormatIndicator getRawPayloadFormatIndicator() {
        return payloadFormatIndicator;
    }

    @Override
    public @NotNull Optional<MqttUtf8String> getContentType() {
        return Optional.ofNullable(contentType);
    }

    public @Nullable MqttUtf8StringImpl getRawContentType() {
        return contentType;
    }

    @Override
    public @NotNull Optional<MqttTopic> getResponseTopic() {
        return Optional.ofNullable(responseTopic);
    }

    public @Nullable MqttTopicImpl getRawResponseTopic() {
        return responseTopic;
    }

    @Override
    public @NotNull Optional<ByteBuffer> getCorrelationData() {
        return ByteBufferUtil.optionalReadOnly(correlationData);
    }

    public @Nullable ByteBuffer getRawCorrelationData() {
        return correlationData;
    }

    @Override
    public void acknowledge() {
        final Confirmable confirmable = this.confirmable;
        if (confirmable == null) {
            throw new UnsupportedOperationException(
                    "A publish must not be acknowledged if manual acknowledgement is not enabled");
        }
        if (!confirmable.confirm()) {
            throw new IllegalStateException("A publish must not be acknowledged more than once");
        }
    }

    @Override
    public @NotNull MqttWillPublish asWill() {
        return new MqttPublishBuilder.WillDefault(this).build();
    }

    @Override
    public @NotNull MqttPublishBuilder.Default extend() {
        return new MqttPublishBuilder.Default(this);
    }

    public @NotNull MqttStatefulPublish createStateful(
            final int packetIdentifier, final boolean dup, final int topicAlias,
            final @NotNull ImmutableIntList subscriptionIdentifiers) {

        return new MqttStatefulPublish(this, packetIdentifier, dup, topicAlias, subscriptionIdentifiers);
    }

    public @NotNull MqttStatefulPublish createStateful(
            final int packetIdentifier, final boolean dup, final @Nullable MqttTopicAliasMapping topicAliasMapping) {

        final int topicAlias =
                (topicAliasMapping == null) ? DEFAULT_NO_TOPIC_ALIAS : topicAliasMapping.onPublish(topic);
        return createStateful(packetIdentifier, dup, topicAlias, DEFAULT_NO_SUBSCRIPTION_IDENTIFIERS);
    }

    public @NotNull MqttPublish withConfirmable(final @NotNull Confirmable confirmable) {
        return new MqttPublish(topic, payload, qos, retain, messageExpiryInterval, payloadFormatIndicator, contentType,
                responseTopic, correlationData, getUserProperties(), confirmable);
    }

    @Override
    protected @NotNull String toAttributeString() {
        return "topic=" + topic + ((payload == null) ? "" : ", payload=" + payload.remaining() + "byte") + ", qos=" +
                qos + ", retain=" + retain + ((messageExpiryInterval == NO_MESSAGE_EXPIRY) ? "" :
                ", messageExpiryInterval=" + messageExpiryInterval) +
                ((payloadFormatIndicator == null) ? "" : ", payloadFormatIndicator=" + payloadFormatIndicator) +
                ((contentType == null) ? "" : ", contentType=" + contentType) +
                ((responseTopic == null) ? "" : ", responseTopic=" + responseTopic) +
                ((correlationData == null) ? "" : ", correlationData=" + correlationData.remaining() + "byte") +
                StringUtil.prepend(", ", super.toAttributeString());
    }

    @Override
    public @NotNull String toString() {
        return "MqttPublish{" + toAttributeString() + '}';
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MqttPublish)) {
            return false;
        }
        final MqttPublish that = (MqttPublish) o;

        return that.canEqual(this) && partialEquals(that) && topic.equals(that.topic) &&
                Objects.equals(payload, that.payload) && (qos == that.qos) && (retain == that.retain) &&
                (messageExpiryInterval == that.messageExpiryInterval) &&
                (payloadFormatIndicator == that.payloadFormatIndicator) &&
                Objects.equals(contentType, that.contentType) && Objects.equals(responseTopic, that.responseTopic) &&
                Objects.equals(correlationData, that.correlationData);
    }

    protected boolean canEqual(final @Nullable Object o) {
        return o instanceof MqttPublish;
    }

    @Override
    public int hashCode() {
        int result = partialHashCode();
        result = 31 * result + topic.hashCode();
        result = 31 * result + Objects.hashCode(payload);
        result = 31 * result + qos.hashCode();
        result = 31 * result + Boolean.hashCode(retain);
        result = 31 * result + Long.hashCode(messageExpiryInterval);
        result = 31 * result + Objects.hashCode(payloadFormatIndicator);
        result = 31 * result + Objects.hashCode(contentType);
        result = 31 * result + Objects.hashCode(responseTopic);
        result = 31 * result + Objects.hashCode(correlationData);
        return result;
    }
}
