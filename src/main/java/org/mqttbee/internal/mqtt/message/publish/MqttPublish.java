/*
 * Copyright 2018 The MQTT Bee project
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

package org.mqttbee.internal.mqtt.message.publish;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.annotations.Immutable;
import org.mqttbee.api.mqtt.datatypes.MqttQos;
import org.mqttbee.api.mqtt.datatypes.MqttTopic;
import org.mqttbee.api.mqtt.datatypes.MqttUtf8String;
import org.mqttbee.api.mqtt.mqtt5.message.publish.Mqtt5PayloadFormatIndicator;
import org.mqttbee.api.mqtt.mqtt5.message.publish.Mqtt5Publish;
import org.mqttbee.api.mqtt.mqtt5.message.publish.TopicAliasUsage;
import org.mqttbee.internal.mqtt.datatypes.MqttTopicImpl;
import org.mqttbee.internal.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.internal.mqtt.datatypes.MqttUtf8StringImpl;
import org.mqttbee.internal.mqtt.message.MqttMessageWithUserProperties;
import org.mqttbee.util.ByteBufferUtil;
import org.mqttbee.util.collections.ImmutableIntList;

import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.OptionalLong;

import static org.mqttbee.internal.mqtt.message.publish.MqttStatefulPublish.DEFAULT_NO_SUBSCRIPTION_IDENTIFIERS;
import static org.mqttbee.internal.mqtt.message.publish.MqttStatefulPublish.DEFAULT_NO_TOPIC_ALIAS;

/**
 * @author Silvio Giebl
 */
@Immutable
public class MqttPublish extends MqttMessageWithUserProperties implements Mqtt5Publish {

    public static final long NO_MESSAGE_EXPIRY = Long.MAX_VALUE;

    private final @NotNull MqttTopicImpl topic;
    private final @Nullable ByteBuffer payload;
    private final @NotNull MqttQos qos;
    private final boolean isRetain;
    private final long messageExpiryInterval;
    private final @Nullable Mqtt5PayloadFormatIndicator payloadFormatIndicator;
    private final @Nullable MqttUtf8StringImpl contentType;
    private final @Nullable MqttTopicImpl responseTopic;
    private final @Nullable ByteBuffer correlationData;
    private final @NotNull TopicAliasUsage topicAliasUsage;

    public MqttPublish(
            final @NotNull MqttTopicImpl topic, final @Nullable ByteBuffer payload, final @NotNull MqttQos qos,
            final boolean isRetain, final long messageExpiryInterval,
            final @Nullable Mqtt5PayloadFormatIndicator payloadFormatIndicator,
            final @Nullable MqttUtf8StringImpl contentType, final @Nullable MqttTopicImpl responseTopic,
            final @Nullable ByteBuffer correlationData, final @NotNull TopicAliasUsage topicAliasUsage,
            final @NotNull MqttUserPropertiesImpl userProperties) {

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
    public @NotNull byte[] getPayloadAsBytes() {
        if (payload == null) {
            return new byte[0];
        }
        return ByteBufferUtil.getBytes(payload);
    }

    @Override
    public @NotNull MqttQos getQos() {
        return qos;
    }

    @Override
    public boolean isRetain() {
        return isRetain;
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
    public @NotNull TopicAliasUsage usesTopicAlias() {
        return topicAliasUsage;
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
            final int packetIdentifier, final boolean isDup, final int topicAlias, final boolean isNewTopicAlias,
            final @NotNull ImmutableIntList subscriptionIdentifiers) {

        return new MqttStatefulPublish(
                this, packetIdentifier, isDup, topicAlias, isNewTopicAlias, subscriptionIdentifiers);
    }

    public @NotNull MqttStatefulPublish createStateful(
            final int packetIdentifier, final boolean isDup, final @Nullable MqttTopicAliasMapping topicAliasMapping) {

        int topicAlias;
        final boolean isNewTopicAlias;
        if (topicAliasMapping == null) {
            topicAlias = DEFAULT_NO_TOPIC_ALIAS;
            isNewTopicAlias = false;
        } else {
            topicAlias = topicAliasMapping.get(topic);
            if (topicAlias != DEFAULT_NO_TOPIC_ALIAS) {
                isNewTopicAlias = false;
            } else {
                topicAlias = topicAliasMapping.set(topic, topicAliasUsage);
                isNewTopicAlias = topicAlias != DEFAULT_NO_TOPIC_ALIAS;
            }
        }
        return createStateful(
                packetIdentifier, isDup, topicAlias, isNewTopicAlias, DEFAULT_NO_SUBSCRIPTION_IDENTIFIERS);
    }
}
