/*
 * Copyright 2018-present HiveMQ and the HiveMQ Community
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
 */

package com.hivemq.client.internal.mqtt.message.publish;

import com.hivemq.client.internal.mqtt.datatypes.*;
import com.hivemq.client.internal.mqtt.util.MqttChecks;
import com.hivemq.client.internal.util.ByteBufferUtil;
import com.hivemq.client.internal.util.Checks;
import com.hivemq.client.internal.util.UnsignedDataTypes;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.datatypes.MqttTopic;
import com.hivemq.client.mqtt.datatypes.MqttUtf8String;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PayloadFormatIndicator;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PublishBuilder;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5WillPublishBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

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
    @Nullable MqttUtf8StringImpl contentType;
    @Nullable MqttTopicImpl responseTopic;
    @Nullable ByteBuffer correlationData;
    @NotNull MqttUserPropertiesImpl userProperties = MqttUserPropertiesImpl.NO_USER_PROPERTIES;

    MqttPublishBuilder() {}

    MqttPublishBuilder(final @NotNull MqttPublish publish) {
        topic = publish.getTopic();
        payload = publish.getRawPayload();
        qos = publish.getQos();
        retain = publish.isRetain();
        messageExpiryInterval = publish.getRawMessageExpiryInterval();
        payloadFormatIndicator = publish.getRawPayloadFormatIndicator();
        contentType = publish.getRawContentType();
        responseTopic = publish.getRawResponseTopic();
        correlationData = publish.getRawCorrelationData();
        userProperties = publish.getUserProperties();
    }

    MqttPublishBuilder(final @NotNull MqttPublishBuilder<?> publishBuilder) {
        topic = publishBuilder.topic;
        payload = publishBuilder.payload;
        qos = publishBuilder.qos;
        retain = publishBuilder.retain;
        messageExpiryInterval = publishBuilder.messageExpiryInterval;
        payloadFormatIndicator = publishBuilder.payloadFormatIndicator;
        contentType = publishBuilder.contentType;
        responseTopic = publishBuilder.responseTopic;
        correlationData = publishBuilder.correlationData;
        userProperties = publishBuilder.userProperties;
    }

    abstract @NotNull B self();

    public @NotNull B topic(final @Nullable String topic) {
        this.topic = MqttTopicImpl.of(topic);
        return self();
    }

    public @NotNull B topic(final @Nullable MqttTopic topic) {
        this.topic = MqttChecks.topic(topic);
        return self();
    }

    public MqttTopicImplBuilder.@NotNull Nested<B> topic() {
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

    public @NotNull B contentType(final @Nullable MqttUtf8String contentType) {
        this.contentType = MqttChecks.stringOrNull(contentType, "Content type");
        return self();
    }

    public @NotNull B responseTopic(final @Nullable String responseTopic) {
        this.responseTopic = (responseTopic == null) ? null : MqttTopicImpl.of(responseTopic, "Response topic");
        return self();
    }

    public @NotNull B responseTopic(final @Nullable MqttTopic responseTopic) {
        this.responseTopic = Checks.notImplementedOrNull(responseTopic, MqttTopicImpl.class, "Response topic");
        return self();
    }

    public MqttTopicImplBuilder.@NotNull Nested<B> responseTopic() {
        return new MqttTopicImplBuilder.Nested<>(this::responseTopic);
    }

    public @NotNull B correlationData(final byte @Nullable [] correlationData) {
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

    public MqttUserPropertiesImplBuilder.@NotNull Nested<B> userProperties() {
        return new MqttUserPropertiesImplBuilder.Nested<>(userProperties, this::userProperties);
    }

    private static abstract class Base<B extends Base<B>> extends MqttPublishBuilder<B> {

        Base() {}

        Base(final @NotNull MqttPublish publish) {
            super(publish);
        }

        public @NotNull B payload(final byte @Nullable [] payload) {
            this.payload = ByteBufferUtil.wrap(payload);
            return self();
        }

        public @NotNull B payload(final @Nullable ByteBuffer payload) {
            this.payload = ByteBufferUtil.slice(payload);
            return self();
        }

        public @NotNull WillDefault asWill() {
            return new WillDefault(this);
        }

        public @NotNull MqttPublish build() {
            Checks.notNull(topic, "Topic");
            return new MqttPublish(topic, payload, qos, retain, messageExpiryInterval, payloadFormatIndicator,
                    contentType, responseTopic, correlationData, userProperties, null);
        }
    }

    public static class Default extends Base<Default> implements Mqtt5PublishBuilder.Complete {

        public Default() {}

        Default(final @NotNull MqttPublish publish) {
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

        private @Range(from = 0, to = UnsignedDataTypes.UNSIGNED_INT_MAX_VALUE) long delayInterval =
                MqttWillPublish.DEFAULT_DELAY_INTERVAL;

        WillBase() {}

        WillBase(final @NotNull MqttPublish publish) {
            super(publish);
            if (publish instanceof MqttWillPublish) {
                delayInterval = ((MqttWillPublish) publish).getDelayInterval();
            } else {
                payload(payload); // check payload size restriction
            }
        }

        WillBase(final @NotNull MqttPublishBuilder<?> publishBuilder) {
            super(publishBuilder);
            if (publishBuilder instanceof WillBase) {
                delayInterval = ((WillBase<?>) publishBuilder).delayInterval;
            } else {
                payload(payload); // check payload size restriction
            }
        }

        public @NotNull B payload(final byte @Nullable [] payload) {
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

        WillDefault(final @NotNull MqttPublish publish) {
            super(publish);
        }

        WillDefault(final @NotNull MqttPublishBuilder<?> publishBuilder) {
            super(publishBuilder);
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
