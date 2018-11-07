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

package org.mqttbee.mqtt.message.publish;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.api.mqtt.datatypes.MqttQos;
import org.mqttbee.api.mqtt.datatypes.MqttTopic;
import org.mqttbee.api.mqtt.datatypes.MqttUTF8String;
import org.mqttbee.api.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import org.mqttbee.api.mqtt.mqtt5.datatypes.Mqtt5UserPropertiesBuilder;
import org.mqttbee.api.mqtt.mqtt5.message.publish.*;
import org.mqttbee.mqtt.datatypes.MqttTopicImpl;
import org.mqttbee.mqtt.datatypes.MqttTopicImplBuilder;
import org.mqttbee.mqtt.datatypes.MqttUTF8StringImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.util.MqttBuilderUtil;
import org.mqttbee.util.ByteBufferUtil;
import org.mqttbee.util.MustNotBeImplementedUtil;
import org.mqttbee.util.UnsignedDataTypes;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static org.mqttbee.api.mqtt.mqtt5.message.publish.Mqtt5Publish.DEFAULT_TOPIC_ALIAS_USAGE;
import static org.mqttbee.api.mqtt.mqtt5.message.publish.Mqtt5WillPublish.DEFAULT_DELAY_INTERVAL;
import static org.mqttbee.mqtt.message.publish.MqttPublish.MESSAGE_EXPIRY_INTERVAL_INFINITY;

/**
 * @author Silvio Giebl
 */
public abstract class MqttPublishBuilder<B extends MqttPublishBuilder<B>> {

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

    MqttPublishBuilder() {}

    MqttPublishBuilder(final @NotNull Mqtt5Publish publish) {
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

    abstract @NotNull B self();

    public @NotNull B topic(final @NotNull String topic) {
        this.topic = MqttBuilderUtil.topic(topic);
        return self();
    }

    public @NotNull B topic(final @NotNull MqttTopic topic) {
        this.topic = MqttBuilderUtil.topic(topic);
        return self();
    }

    public @NotNull MqttTopicImplBuilder.Nested<B> topic() {
        return new MqttTopicImplBuilder.Nested<>(this::topic);
    }

    public @NotNull B qos(final @NotNull MqttQos qos) {
        this.qos = Objects.requireNonNull(qos, "QoS must not be null.");
        return self();
    }

    public @NotNull B retain(final boolean retain) {
        this.retain = retain;
        return self();
    }

    public @NotNull B messageExpiryInterval(final long messageExpiryInterval, final @NotNull TimeUnit timeUnit) {
        final long messageExpiryIntervalSeconds = timeUnit.toSeconds(messageExpiryInterval);
        Preconditions.checkArgument(UnsignedDataTypes.isUnsignedInt(messageExpiryIntervalSeconds),
                "The value of session expiry interval converted in seconds must not exceed the value range of unsigned int. Found: %s which is bigger than %s (max unsigned int).",
                messageExpiryIntervalSeconds, UnsignedDataTypes.UNSIGNED_INT_MAX_VALUE);
        this.messageExpiryIntervalSeconds = messageExpiryIntervalSeconds;
        return self();
    }

    public @NotNull B payloadFormatIndicator(final @Nullable Mqtt5PayloadFormatIndicator payloadFormatIndicator) {
        this.payloadFormatIndicator = payloadFormatIndicator;
        return self();
    }

    public @NotNull B contentType(final @Nullable String contentType) {
        this.contentType = MqttBuilderUtil.stringOrNull(contentType);
        return self();
    }

    public @NotNull B contentType(final @Nullable MqttUTF8String contentType) {
        this.contentType = MqttBuilderUtil.stringOrNull(contentType);
        return self();
    }

    public @NotNull B responseTopic(final @Nullable String responseTopic) {
        this.responseTopic = MqttBuilderUtil.topicOrNull(responseTopic);
        return self();
    }

    public @NotNull B responseTopic(final @Nullable MqttTopic responseTopic) {
        this.responseTopic = MqttBuilderUtil.topicOrNull(responseTopic);
        return self();
    }

    public @NotNull MqttTopicImplBuilder.Nested<B> responseTopic() {
        return new MqttTopicImplBuilder.Nested<>(this::responseTopic);
    }

    public @NotNull B correlationData(final @Nullable byte[] correlationData) {
        this.correlationData = MqttBuilderUtil.binaryDataOrNull(correlationData);
        return self();
    }

    public @NotNull B correlationData(final @Nullable ByteBuffer correlationData) {
        this.correlationData = MqttBuilderUtil.binaryDataOrNull(correlationData);
        return self();
    }

    public @NotNull B userProperties(final @NotNull Mqtt5UserProperties userProperties) {
        this.userProperties = MqttBuilderUtil.userProperties(userProperties);
        return self();
    }

    public @NotNull Mqtt5UserPropertiesBuilder<B> userProperties() {
        return new Mqtt5UserPropertiesBuilder<>(this::userProperties);
    }

    private static abstract class Base<B extends Base<B>> extends MqttPublishBuilder<B> {

        private @NotNull TopicAliasUsage topicAliasUsage = DEFAULT_TOPIC_ALIAS_USAGE;

        Base() {}

        Base(final @NotNull Mqtt5Publish publish) {
            super(publish);
            topicAliasUsage = publish.usesTopicAlias();
        }

        public @NotNull B payload(final @Nullable byte[] payload) {
            this.payload = ByteBufferUtil.wrap(payload);
            return self();
        }

        public @NotNull B payload(final @Nullable ByteBuffer payload) {
            this.payload = ByteBufferUtil.slice(payload);
            return self();
        }

        public @NotNull B useTopicAlias(final @NotNull TopicAliasUsage topicAliasUsage) {
            this.topicAliasUsage = Objects.requireNonNull(topicAliasUsage, "Topic alias usage must not be null.");
            return self();
        }

        public @NotNull MqttPublish build() {
            Objects.requireNonNull(topic, "Topic must be given.");
            return new MqttPublish(topic, payload, qos, retain, messageExpiryIntervalSeconds, payloadFormatIndicator,
                    contentType, responseTopic, correlationData, topicAliasUsage, userProperties);
        }
    }

    public static class Default extends Base<Default> implements Mqtt5PublishBuilder.Complete {

        public Default() {}

        public Default(final @NotNull Mqtt5Publish publish) {
            super(publish);
        }

        @Override
        @NotNull Default self() {
            return this;
        }
    }

    public static class Nested<P> extends Base<Nested<P>> implements Mqtt5PublishBuilder.Nested.Complete<P> {

        private final @NotNull Function<? super MqttPublish, P> parentConsumer;

        public Nested(final @NotNull Function<? super MqttPublish, P> parentConsumer) {
            this.parentConsumer = parentConsumer;
        }

        @Override
        @NotNull Nested<P> self() {
            return this;
        }

        @Override
        public @NotNull P applyPublish() {
            return parentConsumer.apply(build());
        }
    }

    public static class Send<P> extends Base<Send<P>> implements Mqtt5PublishBuilder.Send.Complete<P> {

        private final @NotNull Function<? super MqttPublish, P> parentConsumer;

        public Send(final @NotNull Function<? super MqttPublish, P> parentConsumer) {
            this.parentConsumer = parentConsumer;
        }

        @Override
        @NotNull Send<P> self() {
            return this;
        }

        @Override
        public @NotNull P send() {
            return parentConsumer.apply(build());
        }
    }

    private static abstract class WillBase<B extends WillBase<B>> extends MqttPublishBuilder<B> {

        private long delayInterval = DEFAULT_DELAY_INTERVAL;

        WillBase() {}

        WillBase(final @NotNull Mqtt5Publish publish) {
            super(publish);
            if (publish instanceof Mqtt5WillPublish) {
                delayInterval =
                        MustNotBeImplementedUtil.checkNotImplemented(publish, MqttWillPublish.class).getDelayInterval();
            } else {
                payload(payload);
            }
        }

        public @NotNull B payload(final @Nullable byte[] payload) {
            this.payload = MqttBuilderUtil.binaryDataOrNull(payload);
            return self();
        }

        public @NotNull B payload(final @Nullable ByteBuffer payload) {
            this.payload = MqttBuilderUtil.binaryDataOrNull(payload);
            return self();
        }

        public @NotNull B delayInterval(final long delayInterval, final @NotNull TimeUnit timeUnit) {
            final long delayIntervalSeconds = timeUnit.toSeconds(delayInterval);
            Preconditions.checkArgument(UnsignedDataTypes.isUnsignedInt(delayIntervalSeconds),
                    "The value of delay interval converted in seconds must not exceed the value range of unsigned int. Found: %s which is bigger than %s (max unsigned int).",
                    delayIntervalSeconds, UnsignedDataTypes.UNSIGNED_INT_MAX_VALUE);

            this.delayInterval = delayIntervalSeconds;
            return self();
        }

        public @NotNull MqttWillPublish build() {
            Preconditions.checkNotNull(topic, "Topic must be given.");
            return new MqttWillPublish(topic, payload, qos, retain, messageExpiryIntervalSeconds,
                    payloadFormatIndicator, contentType, responseTopic, correlationData, userProperties, delayInterval);
        }
    }

    public static class WillDefault extends WillBase<WillDefault> implements Mqtt5WillPublishBuilder.Complete {

        public WillDefault() {}

        public WillDefault(final @NotNull Mqtt5Publish publish) {
            super(publish);
        }

        @Override
        @NotNull WillDefault self() {
            return this;
        }
    }

    public static class WillNested<P> extends WillBase<WillNested<P>>
            implements Mqtt5WillPublishBuilder.Nested.Complete<P> {

        private final @NotNull Function<? super MqttWillPublish, P> parentConsumer;

        public WillNested(final @NotNull Function<? super MqttWillPublish, P> parentConsumer) {
            this.parentConsumer = parentConsumer;
        }

        @Override
        @NotNull WillNested<P> self() {
            return this;
        }

        @Override
        public @NotNull P applyWillPublish() {
            return parentConsumer.apply(build());
        }
    }
}
