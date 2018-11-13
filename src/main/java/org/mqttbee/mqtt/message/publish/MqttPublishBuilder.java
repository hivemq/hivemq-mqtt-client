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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.api.mqtt.datatypes.MqttQos;
import org.mqttbee.api.mqtt.datatypes.MqttTopic;
import org.mqttbee.api.mqtt.datatypes.MqttUTF8String;
import org.mqttbee.api.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import org.mqttbee.api.mqtt.mqtt5.message.publish.*;
import org.mqttbee.mqtt.datatypes.*;
import org.mqttbee.mqtt.util.MqttChecks;
import org.mqttbee.util.ByteBufferUtil;
import org.mqttbee.util.Checks;

import java.nio.ByteBuffer;
import java.util.function.Function;

/**
 * @author Silvio Giebl
 */
public abstract class MqttPublishBuilder<B extends MqttPublishBuilder<B>> {

    @Nullable MqttTopicImpl topic;
    @Nullable ByteBuffer payload;
    @NotNull MqttQos qos = MqttPublish.DEFAULT_QOS;
    boolean retain;
    long messageExpiryInterval = MqttPublish.NO_MESSAGE_EXPIRY;
    @Nullable Mqtt5PayloadFormatIndicator payloadFormatIndicator;
    @Nullable MqttUTF8StringImpl contentType;
    @Nullable MqttTopicImpl responseTopic;
    @Nullable ByteBuffer correlationData;
    @NotNull MqttUserPropertiesImpl userProperties = MqttUserPropertiesImpl.NO_USER_PROPERTIES;

    MqttPublishBuilder() {}

    MqttPublishBuilder(final @Nullable Mqtt5Publish publish) {
        final MqttPublish mqttPublish = MqttChecks.publish(publish);
        topic = mqttPublish.getTopic();
        payload = mqttPublish.getRawPayload();
        qos = mqttPublish.getQos();
        retain = mqttPublish.isRetain();
        messageExpiryInterval = mqttPublish.getRawMessageExpiryInterval();
        payloadFormatIndicator = mqttPublish.getRawPayloadFormatIndicator();
        contentType = mqttPublish.getRawContentType();
        responseTopic = mqttPublish.getRawResponseTopic();
        correlationData = mqttPublish.getRawCorrelationData();
        userProperties = mqttPublish.getUserProperties();
    }

    abstract @NotNull B self();

    public @NotNull B topic(final @Nullable String topic) {
        this.topic = MqttChecks.topicNotNull(topic);
        return self();
    }

    public @NotNull B topic(final @Nullable MqttTopic topic) {
        this.topic = MqttChecks.topicNotNull(topic);
        return self();
    }

    public @NotNull MqttTopicImplBuilder.Nested<B> topic() {
        return new MqttTopicImplBuilder.Nested<>(this::topic);
    }

    public @NotNull B qos(final @Nullable MqttQos qos) {
        this.qos = Checks.notNull(qos, "QoS");
        return self();
    }

    public @NotNull B retain(final boolean retain) {
        this.retain = retain;
        return self();
    }

    public @NotNull B messageExpiryInterval(final long messageExpiryInterval) {
        this.messageExpiryInterval = Checks.unsignedInt(messageExpiryInterval, "Message expiry interval");
        return self();
    }

    public @NotNull B noMessageExpiry() {
        this.messageExpiryInterval = MqttPublish.NO_MESSAGE_EXPIRY;
        return self();
    }

    public @NotNull B payloadFormatIndicator(final @Nullable Mqtt5PayloadFormatIndicator payloadFormatIndicator) {
        this.payloadFormatIndicator = payloadFormatIndicator;
        return self();
    }

    public @NotNull B contentType(final @Nullable String contentType) {
        this.contentType = MqttChecks.stringOrNull(contentType, "Content type");
        return self();
    }

    public @NotNull B contentType(final @Nullable MqttUTF8String contentType) {
        this.contentType = MqttChecks.stringOrNull(contentType, "Content type");
        return self();
    }

    public @NotNull B responseTopic(final @Nullable String responseTopic) {
        this.responseTopic = (responseTopic == null) ? null : MqttChecks.topic(responseTopic, "Response topic");
        return self();
    }

    public @NotNull B responseTopic(final @Nullable MqttTopic responseTopic) {
        this.responseTopic = Checks.notImplementedOrNull(responseTopic, MqttTopicImpl.class, "Response topic");
        return self();
    }

    public @NotNull MqttTopicImplBuilder.Nested<B> responseTopic() {
        return new MqttTopicImplBuilder.Nested<>(this::responseTopic);
    }

    public @NotNull B correlationData(final @Nullable byte[] correlationData) {
        this.correlationData = MqttChecks.binaryDataOrNull(correlationData, "Correlation data");
        return self();
    }

    public @NotNull B correlationData(final @Nullable ByteBuffer correlationData) {
        this.correlationData = MqttChecks.binaryDataOrNull(correlationData, "Correlation data");
        return self();
    }

    public @NotNull B userProperties(final @Nullable Mqtt5UserProperties userProperties) {
        this.userProperties = MqttChecks.userProperties(userProperties);
        return self();
    }

    public @NotNull MqttUserPropertiesImplBuilder.Nested<B> userProperties() {
        return new MqttUserPropertiesImplBuilder.Nested<>(this::userProperties);
    }

    private static abstract class Base<B extends Base<B>> extends MqttPublishBuilder<B> {

        private @NotNull TopicAliasUsage topicAliasUsage = MqttPublish.DEFAULT_TOPIC_ALIAS_USAGE;

        Base() {}

        Base(final @Nullable Mqtt5Publish publish) {
            super(publish);
            topicAliasUsage = MqttChecks.publish(publish).usesTopicAlias();
        }

        public @NotNull B payload(final @Nullable byte[] payload) {
            this.payload = ByteBufferUtil.wrap(payload);
            return self();
        }

        public @NotNull B payload(final @Nullable ByteBuffer payload) {
            this.payload = ByteBufferUtil.slice(payload);
            return self();
        }

        public @NotNull B useTopicAlias(final @Nullable TopicAliasUsage topicAliasUsage) {
            this.topicAliasUsage = Checks.notNull(topicAliasUsage, "Topic alias usage");
            return self();
        }

        public @NotNull MqttPublish build() {
            Checks.notNull(topic, "Topic");
            return new MqttPublish(topic, payload, qos, retain, messageExpiryInterval, payloadFormatIndicator,
                    contentType, responseTopic, correlationData, topicAliasUsage, userProperties);
        }
    }

    public static class Default extends Base<Default> implements Mqtt5PublishBuilder.Complete {

        public Default() {}

        public Default(final @Nullable Mqtt5Publish publish) {
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

        private long delayInterval = MqttWillPublish.DEFAULT_DELAY_INTERVAL;

        WillBase() {}

        WillBase(final @Nullable Mqtt5Publish publish) {
            super(publish);
            if (publish instanceof Mqtt5WillPublish) {
                delayInterval =
                        Checks.notImplemented(publish, MqttWillPublish.class, "Will publish").getDelayInterval();
            } else {
                payload(payload); // check payload size restriction
            }
        }

        public @NotNull B payload(final @Nullable byte[] payload) {
            this.payload = MqttChecks.binaryDataOrNull(payload, "Payload");
            return self();
        }

        public @NotNull B payload(final @Nullable ByteBuffer payload) {
            this.payload = MqttChecks.binaryDataOrNull(payload, "Payload");
            return self();
        }

        public @NotNull B delayInterval(final long delayInterval) {
            this.delayInterval = Checks.unsignedInt(delayInterval, "Will delay interval");
            return self();
        }

        public @NotNull MqttWillPublish build() {
            Checks.notNull(topic, "Topic");
            return new MqttWillPublish(topic, payload, qos, retain, messageExpiryInterval, payloadFormatIndicator,
                    contentType, responseTopic, correlationData, userProperties, delayInterval);
        }
    }

    public static class WillDefault extends WillBase<WillDefault> implements Mqtt5WillPublishBuilder.Complete {

        public WillDefault() {}

        public WillDefault(final @Nullable Mqtt5Publish publish) {
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
