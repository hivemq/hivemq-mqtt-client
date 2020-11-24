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

package com.hivemq.client.internal.mqtt.message.publish.mqtt3;

import com.hivemq.client.internal.mqtt.datatypes.MqttTopicImpl;
import com.hivemq.client.internal.mqtt.datatypes.MqttTopicImplBuilder;
import com.hivemq.client.internal.mqtt.message.publish.MqttPublish;
import com.hivemq.client.internal.mqtt.message.publish.MqttWillPublish;
import com.hivemq.client.internal.mqtt.util.MqttChecks;
import com.hivemq.client.internal.util.ByteBufferUtil;
import com.hivemq.client.internal.util.Checks;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.datatypes.MqttTopic;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3PublishBuilder;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3WillPublishBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.function.Function;

/**
 * @author Silvio Giebl
 */
public abstract class Mqtt3PublishViewBuilder<B extends Mqtt3PublishViewBuilder<B>> {

    @Nullable MqttTopicImpl topic;
    @Nullable ByteBuffer payload;
    @NotNull MqttQos qos = Mqtt3PublishView.DEFAULT_QOS;
    boolean retain;

    Mqtt3PublishViewBuilder() {}

    Mqtt3PublishViewBuilder(final @NotNull Mqtt3PublishView publish) {
        final MqttPublish delegate = publish.getDelegate();
        topic = delegate.getTopic();
        payload = delegate.getRawPayload();
        qos = delegate.getQos();
        retain = delegate.isRetain();
    }

    protected abstract @NotNull B self();

    public @NotNull B topic(final @Nullable String topic) {
        this.topic = MqttTopicImpl.of(topic);
        return self();
    }

    public @NotNull B topic(final @Nullable MqttTopic topic) {
        this.topic = MqttChecks.topic(topic);
        return self();
    }

    public MqttTopicImplBuilder.@NotNull Nested<B> topicWith() {
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

    private static abstract class Base<B extends Base<B>> extends Mqtt3PublishViewBuilder<B> {

        Base() {}

        Base(final @NotNull Mqtt3PublishView publish) {
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

        public @NotNull Mqtt3PublishView build() {
            Checks.notNull(topic, "Topic");
            return Mqtt3PublishView.of(topic, payload, qos, retain);
        }
    }

    public static class Default extends Base<Default> implements Mqtt3PublishBuilder.Complete {

        public Default() {}

        Default(final @NotNull Mqtt3PublishView publish) {
            super(publish);
        }

        @Override
        protected @NotNull Default self() {
            return this;
        }
    }

    public static class Nested<P> extends Base<Nested<P>> implements Mqtt3PublishBuilder.Nested.Complete<P> {

        private final @NotNull Function<? super Mqtt3PublishView, P> parentConsumer;

        public Nested(final @NotNull Function<? super Mqtt3PublishView, P> parentConsumer) {
            this.parentConsumer = parentConsumer;
        }

        @Override
        protected @NotNull Nested<P> self() {
            return this;
        }

        @Override
        public @NotNull P applyPublish() {
            return parentConsumer.apply(build());
        }
    }

    public static class Send<P> extends Base<Send<P>> implements Mqtt3PublishBuilder.Send.Complete<P> {

        private final @NotNull Function<? super Mqtt3PublishView, P> parentConsumer;

        public Send(final @NotNull Function<? super Mqtt3PublishView, P> parentConsumer) {
            this.parentConsumer = parentConsumer;
        }

        @Override
        protected @NotNull Send<P> self() {
            return this;
        }

        @Override
        public @NotNull P send() {
            return parentConsumer.apply(build());
        }
    }

    private static abstract class WillBase<B extends WillBase<B>> extends Mqtt3PublishViewBuilder<B> {

        WillBase() {}

        WillBase(final @NotNull Mqtt3PublishView publish) {
            super(publish);
            if (!(publish.getDelegate() instanceof MqttWillPublish)) {
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

        public @NotNull Mqtt3PublishView build() {
            Checks.notNull(topic, "Topic");
            return Mqtt3PublishView.willOf(topic, payload, qos, retain);
        }
    }

    public static class WillDefault extends WillBase<WillDefault> implements Mqtt3WillPublishBuilder.Complete {

        public WillDefault() {}

        WillDefault(final @NotNull Mqtt3PublishView publish) {
            super(publish);
        }

        @Override
        protected @NotNull WillDefault self() {
            return this;
        }
    }

    public static class WillNested<P> extends WillBase<WillNested<P>>
            implements Mqtt3WillPublishBuilder.Nested.Complete<P> {

        private final @NotNull Function<? super Mqtt3PublishView, P> parentConsumer;

        public WillNested(final @NotNull Function<? super Mqtt3PublishView, P> parentConsumer) {
            this.parentConsumer = parentConsumer;
        }

        @Override
        protected @NotNull WillNested<P> self() {
            return this;
        }

        @Override
        public @NotNull P applyWillPublish() {
            return parentConsumer.apply(build());
        }
    }
}
