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

import static org.mqttbee.mqtt.message.publish.MqttPublish.MESSAGE_EXPIRY_INTERVAL_INFINITY;

/**
 * @author Silvio Giebl
 */
abstract class AbstractMqtt5PublishBuilder<S extends AbstractMqtt5PublishBuilder<S, B, P>, B extends Mqtt5Publish, P>
        extends FluentBuilder<B, P> {

    @Nullable MqttTopicImpl topic;
    @Nullable ByteBuffer payload;
    @NotNull MqttQos qos = Mqtt5Publish.DEFAULT_QOS;
    boolean retain;
    long messageExpiryIntervalSeconds = MESSAGE_EXPIRY_INTERVAL_INFINITY;
    @Nullable Mqtt5PayloadFormatIndicator payloadFormatIndicator;
    @Nullable MqttUTF8StringImpl contentType;
    @Nullable MqttTopicImpl responseTopic;
    @Nullable ByteBuffer correlationData;
    @NotNull MqttUserPropertiesImpl userProperties = MqttUserPropertiesImpl.NO_USER_PROPERTIES;

    AbstractMqtt5PublishBuilder(final @Nullable Function<? super B, P> parentConsumer) {
        super(parentConsumer);
    }

    AbstractMqtt5PublishBuilder(final @NotNull Mqtt5Publish publish) {
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
        userProperties = publishImpl.getUserProperties();
    }

    abstract @NotNull S self();

    public @NotNull S topic(final @NotNull String topic) {
        this.topic = MqttBuilderUtil.topic(topic);
        return self();
    }

    public @NotNull S topic(final @NotNull MqttTopic topic) {
        this.topic = MqttBuilderUtil.topic(topic);
        return self();
    }

    public @NotNull MqttTopicBuilder<S> topic() {
        return new MqttTopicBuilder<>(this::topic);
    }

    public @NotNull S payload(final @Nullable byte[] payload) {
        this.payload = ByteBufferUtil.wrap(payload);
        return self();
    }

    public @NotNull S payload(final @Nullable ByteBuffer payload) {
        this.payload = ByteBufferUtil.slice(payload);
        return self();
    }

    public @NotNull S qos(final @NotNull MqttQos qos) {
        this.qos = Preconditions.checkNotNull(qos, "QoS must not be null.");
        return self();
    }

    public @NotNull S retain(final boolean retain) {
        this.retain = retain;
        return self();
    }

    public @NotNull S messageExpiryInterval(final long messageExpiryInterval, final @NotNull TimeUnit timeUnit) {
        final long messageExpiryIntervalSeconds = timeUnit.toSeconds(messageExpiryInterval);
        Preconditions.checkArgument(UnsignedDataTypes.isUnsignedInt(messageExpiryIntervalSeconds),
                "The value of session expiry interval converted in seconds must not exceed the value range of unsigned int. Found: %s which is bigger than %s (max unsigned int).",
                messageExpiryIntervalSeconds, UnsignedDataTypes.UNSIGNED_INT_MAX_VALUE);
        this.messageExpiryIntervalSeconds = messageExpiryIntervalSeconds;
        return self();
    }

    public @NotNull S payloadFormatIndicator(final @Nullable Mqtt5PayloadFormatIndicator payloadFormatIndicator) {
        this.payloadFormatIndicator = payloadFormatIndicator;
        return self();
    }

    public @NotNull S contentType(final @Nullable String contentType) {
        this.contentType = MqttBuilderUtil.stringOrNull(contentType);
        return self();
    }

    public @NotNull S contentType(final @Nullable MqttUTF8String contentType) {
        this.contentType = MqttBuilderUtil.stringOrNull(contentType);
        return self();
    }

    public @NotNull S responseTopic(final @Nullable String responseTopic) {
        this.responseTopic = MqttBuilderUtil.topicOrNull(responseTopic);
        return self();
    }

    public @NotNull S responseTopic(final @Nullable MqttTopic responseTopic) {
        this.responseTopic = MqttBuilderUtil.topicOrNull(responseTopic);
        return self();
    }

    public @NotNull MqttTopicBuilder<S> responseTopic() {
        return new MqttTopicBuilder<>(this::responseTopic);
    }

    public @NotNull S correlationData(final @Nullable byte[] correlationData) {
        this.correlationData = MqttBuilderUtil.binaryDataOrNull(correlationData);
        return self();
    }

    public @NotNull S correlationData(final @Nullable ByteBuffer correlationData) {
        this.correlationData = MqttBuilderUtil.binaryDataOrNull(correlationData);
        return self();
    }

    public @NotNull S userProperties(final @NotNull Mqtt5UserProperties userProperties) {
        this.userProperties = MqttBuilderUtil.userProperties(userProperties);
        return self();
    }

    public @NotNull Mqtt5UserPropertiesBuilder<S> userProperties() {
        return new Mqtt5UserPropertiesBuilder<>(this::userProperties);
    }
}
