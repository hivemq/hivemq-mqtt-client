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

package org.mqttbee.api.mqtt.mqtt5.message.publish;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.api.mqtt.datatypes.MqttQos;
import org.mqttbee.api.mqtt.datatypes.MqttTopic;
import org.mqttbee.api.mqtt.datatypes.MqttTopicBuilder;
import org.mqttbee.api.mqtt.datatypes.MqttUTF8String;
import org.mqttbee.api.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import org.mqttbee.api.mqtt.mqtt5.datatypes.Mqtt5UserPropertiesBuilder;
import org.mqttbee.mqtt.datatypes.MqttTopicImpl;
import org.mqttbee.mqtt.datatypes.MqttUTF8StringImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.message.publish.MqttPublish;
import org.mqttbee.mqtt.util.MqttBuilderUtil;
import org.mqttbee.util.ByteBufferUtil;
import org.mqttbee.util.FluentBuilder;
import org.mqttbee.util.MustNotBeImplementedUtil;
import org.mqttbee.util.UnsignedDataTypes;

import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static org.mqttbee.mqtt.message.publish.MqttPublish.DEFAULT_TOPIC_ALIAS_USAGE;
import static org.mqttbee.mqtt.message.publish.MqttPublish.MESSAGE_EXPIRY_INTERVAL_INFINITY;

/**
 * @author Silvio Giebl
 */
public class Mqtt5PublishBuilder<P> extends FluentBuilder<Mqtt5Publish, P> {

    @Nullable MqttTopicImpl topic;
    @Nullable ByteBuffer payload;
    @NotNull MqttQos qos = Mqtt5Publish.DEFAULT_QOS;
    boolean retain;
    long messageExpiryIntervalSeconds = MESSAGE_EXPIRY_INTERVAL_INFINITY;
    @Nullable Mqtt5PayloadFormatIndicator payloadFormatIndicator;
    @Nullable MqttUTF8StringImpl contentType;
    @Nullable MqttTopicImpl responseTopic;
    @Nullable ByteBuffer correlationData;
    private @NotNull TopicAliasUsage topicAliasUsage = DEFAULT_TOPIC_ALIAS_USAGE;
    @NotNull MqttUserPropertiesImpl userProperties = MqttUserPropertiesImpl.NO_USER_PROPERTIES;

    public Mqtt5PublishBuilder(final @Nullable Function<? super Mqtt5Publish, P> parentConsumer) {
        super(parentConsumer);
    }

    Mqtt5PublishBuilder(final @NotNull Mqtt5Publish publish) {
        super(null);
        final MqttPublish publishImpl = MustNotBeImplementedUtil.checkNotImplemented(publish, MqttPublish.class);
        topic = publishImpl.getTopic();
        payload = publishImpl.getRawPayload();
        qos = publishImpl.getQos();
        retain = publishImpl.isRetain();
        messageExpiryIntervalSeconds = publishImpl.getRawMessageExpiryInterval();
        payloadFormatIndicator = publishImpl.getRawPayloadFormatIndicator();
        contentType = publishImpl.getRawContentType();
        responseTopic = publishImpl.getRawResponseTopic();
        correlationData = publishImpl.getRawCorrelationData();
        topicAliasUsage = publishImpl.usesTopicAlias();
        userProperties = publishImpl.getUserProperties();
    }

    public @NotNull Mqtt5PublishBuilder<P> topic(final @NotNull String topic) {
        this.topic = MqttBuilderUtil.topic(topic);
        return this;
    }

    public @NotNull Mqtt5PublishBuilder<P> topic(final @NotNull MqttTopic topic) {
        this.topic = MqttBuilderUtil.topic(topic);
        return this;
    }

    public @NotNull MqttTopicBuilder<? extends Mqtt5PublishBuilder<P>> topic() {
        return new MqttTopicBuilder<>("", this::topic);
    }

    public @NotNull Mqtt5PublishBuilder<P> payload(final @Nullable byte[] payload) {
        this.payload = (payload == null) ? null : ByteBufferUtil.wrap(payload);
        return this;
    }

    public @NotNull Mqtt5PublishBuilder<P> payload(final @Nullable ByteBuffer payload) {
        this.payload = (payload == null) ? null : ByteBufferUtil.slice(payload);
        return this;
    }

    public @NotNull Mqtt5PublishBuilder<P> qos(final @NotNull MqttQos qos) {
        this.qos = Preconditions.checkNotNull(qos, "QoS must not be null.");
        return this;
    }

    public @NotNull Mqtt5PublishBuilder<P> retain(final boolean retain) {
        this.retain = retain;
        return this;
    }

    public @NotNull Mqtt5PublishBuilder<P> messageExpiryInterval(
            final long messageExpiryInterval, final @NotNull TimeUnit timeUnit) {

        final long messageExpiryIntervalSeconds = timeUnit.toSeconds(messageExpiryInterval);
        Preconditions.checkArgument(UnsignedDataTypes.isUnsignedInt(messageExpiryIntervalSeconds),
                "The value of session expiry interval converted in seconds must not exceed the value range of unsigned int. Found: %s which is bigger than %s (max unsigned int).",
                messageExpiryIntervalSeconds, UnsignedDataTypes.UNSIGNED_INT_MAX_VALUE);
        this.messageExpiryIntervalSeconds = messageExpiryIntervalSeconds;
        return this;
    }

    public @NotNull Mqtt5PublishBuilder<P> payloadFormatIndicator(
            final @Nullable Mqtt5PayloadFormatIndicator payloadFormatIndicator) {

        this.payloadFormatIndicator = payloadFormatIndicator;
        return this;
    }

    public @NotNull Mqtt5PublishBuilder<P> contentType(final @Nullable String contentType) {
        this.contentType = MqttBuilderUtil.stringOrNull(contentType);
        return this;
    }

    public @NotNull Mqtt5PublishBuilder<P> contentType(final @Nullable MqttUTF8String contentType) {
        this.contentType = MqttBuilderUtil.stringOrNull(contentType);
        return this;
    }

    public @NotNull Mqtt5PublishBuilder<P> responseTopic(final @Nullable String responseTopic) {
        this.responseTopic = MqttBuilderUtil.topicOrNull(responseTopic);
        return this;
    }

    public @NotNull Mqtt5PublishBuilder<P> responseTopic(final @Nullable MqttTopic responseTopic) {
        this.responseTopic = MqttBuilderUtil.topicOrNull(responseTopic);
        return this;
    }

    public @NotNull MqttTopicBuilder<? extends Mqtt5PublishBuilder<P>> responseTopic() {
        return new MqttTopicBuilder<>("", this::responseTopic);
    }

    public @NotNull Mqtt5PublishBuilder<P> correlationData(final @Nullable byte[] correlationData) {
        this.correlationData = MqttBuilderUtil.binaryDataOrNull(correlationData);
        return this;
    }

    public @NotNull Mqtt5PublishBuilder<P> correlationData(final @Nullable ByteBuffer correlationData) {
        this.correlationData = MqttBuilderUtil.binaryDataOrNull(correlationData);
        return this;
    }

    public @NotNull Mqtt5PublishBuilder<P> useTopicAlias(final @NotNull TopicAliasUsage topicAliasUsage) {
        this.topicAliasUsage = topicAliasUsage;
        return this;
    }

    public @NotNull Mqtt5PublishBuilder<P> userProperties(final @NotNull Mqtt5UserProperties userProperties) {
        this.userProperties = MqttBuilderUtil.userProperties(userProperties);
        return this;
    }

    public @NotNull Mqtt5UserPropertiesBuilder<? extends Mqtt5PublishBuilder<P>> userProperties() {
        return new Mqtt5UserPropertiesBuilder<>(this::userProperties);
    }

    @Override
    public @NotNull Mqtt5Publish build() {
        Preconditions.checkNotNull(topic, "Topic must not be null.");
        return new MqttPublish(topic, payload, qos, retain, messageExpiryIntervalSeconds, payloadFormatIndicator,
                contentType, responseTopic, correlationData, topicAliasUsage, userProperties);
    }

}
