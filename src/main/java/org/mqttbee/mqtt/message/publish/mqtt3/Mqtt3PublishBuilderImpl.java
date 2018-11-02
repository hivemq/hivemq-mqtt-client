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

package org.mqttbee.mqtt.message.publish.mqtt3;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.api.mqtt.datatypes.MqttQos;
import org.mqttbee.api.mqtt.datatypes.MqttTopic;
import org.mqttbee.api.mqtt.mqtt3.message.publish.Mqtt3Publish;
import org.mqttbee.api.mqtt.mqtt3.message.publish.Mqtt3PublishBuilder;
import org.mqttbee.api.mqtt.mqtt3.message.publish.Mqtt3PublishBuilderBase;
import org.mqttbee.api.mqtt.mqtt3.message.publish.Mqtt3WillPublishBuilder;
import org.mqttbee.mqtt.datatypes.MqttTopicImpl;
import org.mqttbee.mqtt.util.MqttBuilderUtil;
import org.mqttbee.util.ByteBufferUtil;
import org.mqttbee.util.MustNotBeImplementedUtil;

import java.nio.ByteBuffer;
import java.util.function.Function;

/**
 * @author Silvio Giebl
 */
// @formatter:off
public abstract class Mqtt3PublishBuilderImpl<
            B extends Mqtt3PublishBuilderBase<B, C>,
            C extends B>
        implements Mqtt3PublishBuilderBase<B, C>,
                   Mqtt3PublishBuilderBase.Complete<B, C> {
// @formatter:on

    @Nullable MqttTopicImpl topic;
    @Nullable ByteBuffer payload;
    @NotNull MqttQos qos = Mqtt3Publish.DEFAULT_QOS;
    boolean retain;

    Mqtt3PublishBuilderImpl() {}

    Mqtt3PublishBuilderImpl(final @NotNull Mqtt3Publish publish) {
        final Mqtt3PublishView publishView =
                MustNotBeImplementedUtil.checkNotImplemented(publish, Mqtt3PublishView.class);
        topic = publishView.getDelegate().getTopic();
        payload = publishView.getDelegate().getRawPayload();
        qos = publishView.getQos();
        retain = publishView.isRetain();
    }

    protected abstract @NotNull C self();

    public @NotNull C topic(final @NotNull String topic) {
        this.topic = MqttBuilderUtil.topic(topic);
        return self();
    }

    public @NotNull C topic(final @NotNull MqttTopic topic) {
        this.topic = MqttBuilderUtil.topic(topic);
        return self();
    }

    public @NotNull C payload(final @Nullable byte[] payload) {
        this.payload = ByteBufferUtil.wrap(payload);
        return self();
    }

    public @NotNull C payload(final @Nullable ByteBuffer payload) {
        this.payload = ByteBufferUtil.slice(payload);
        return self();
    }

    public @NotNull C qos(final @NotNull MqttQos qos) {
        this.qos = Preconditions.checkNotNull(qos, "QoS must not be null.");
        return self();
    }

    public @NotNull C retain(final boolean retain) {
        this.retain = retain;
        return self();
    }

    // @formatter:off
    private static abstract class Base<
                B extends Mqtt3PublishBuilderBase<B, C>,
                C extends B>
            extends Mqtt3PublishBuilderImpl<B, C> {
    // @formatter:on

        Base() {}

        Base(final @NotNull Mqtt3Publish publish) {
            super(publish);
        }

        public @NotNull Mqtt3Publish build() {
            Preconditions.checkNotNull(topic, "Topic must not be null.");
            return Mqtt3PublishView.of(topic, payload, qos, retain);
        }
    }

    // @formatter:off
    public static class Impl
            extends Base<
                        Mqtt3PublishBuilder,
                        Mqtt3PublishBuilder.Complete>
            implements Mqtt3PublishBuilder,
                       Mqtt3PublishBuilder.Complete {
    // @formatter:on

        public Impl() {}

        public Impl(final @NotNull Mqtt3Publish publish) {
            super(publish);
        }

        @Override
        protected @NotNull Mqtt3PublishBuilder.Complete self() {
            return this;
        }
    }

    // @formatter:off
    public static class NestedImpl<P>
            extends Base<
                        Mqtt3PublishBuilder.Nested<P>,
                        Mqtt3PublishBuilder.Nested.Complete<P>>
            implements Mqtt3PublishBuilder.Nested<P>,
                       Mqtt3PublishBuilder.Nested.Complete<P> {
    // @formatter:on

        private final @NotNull Function<? super Mqtt3Publish, P> parentConsumer;

        public NestedImpl(final @NotNull Function<? super Mqtt3Publish, P> parentConsumer) {
            this.parentConsumer = parentConsumer;
        }

        @Override
        protected @NotNull Mqtt3PublishBuilder.Nested.Complete<P> self() {
            return this;
        }

        @Override
        public @NotNull P applyPublish() {
            return parentConsumer.apply(build());
        }
    }

    // @formatter:off
    public static class SendImpl<P>
            extends Base<
                        Mqtt3PublishBuilder.Send<P>,
                        Mqtt3PublishBuilder.Send.Complete<P>>
            implements Mqtt3PublishBuilder.Send<P>,
                       Mqtt3PublishBuilder.Send.Complete<P> {
    // @formatter:on

        private final @NotNull Function<? super Mqtt3Publish, P> parentConsumer;

        public SendImpl(final @NotNull Function<? super Mqtt3Publish, P> parentConsumer) {
            this.parentConsumer = parentConsumer;
        }

        @Override
        protected @NotNull Mqtt3PublishBuilder.Send.Complete<P> self() {
            return this;
        }

        @Override
        public @NotNull P send() {
            return parentConsumer.apply(build());
        }
    }

    // @formatter:off
    private static abstract class WillBase<
                B extends Mqtt3PublishBuilderBase<B, C>,
                C extends B>
            extends Mqtt3PublishBuilderImpl<B, C> {
    // @formatter:on

        WillBase() {}

        WillBase(final @NotNull Mqtt3Publish publish) {
            super(publish);
        }

        public @NotNull Mqtt3Publish build() {
            Preconditions.checkNotNull(topic, "Topic must not be null.");
            return Mqtt3PublishView.willOf(topic, payload, qos, retain);
        }
    }

    // @formatter:off
    public static class WillImpl
            extends WillBase<
                        Mqtt3WillPublishBuilder,
                        Mqtt3WillPublishBuilder.Complete>
            implements Mqtt3WillPublishBuilder,
                       Mqtt3WillPublishBuilder.Complete {
    // @formatter:on

        public WillImpl() {}

        public WillImpl(final @NotNull Mqtt3Publish publish) {
            super(publish);
        }

        @Override
        protected @NotNull Mqtt3WillPublishBuilder.Complete self() {
            return this;
        }
    }

    // @formatter:off
    public static class WillNestedImpl<P>
            extends WillBase<
                        Mqtt3WillPublishBuilder.Nested<P>,
                        Mqtt3WillPublishBuilder.Nested.Complete<P>>
            implements Mqtt3WillPublishBuilder.Nested<P>,
                       Mqtt3WillPublishBuilder.Nested.Complete<P> {
    // @formatter:on

        private final @NotNull Function<? super Mqtt3Publish, P> parentConsumer;

        public WillNestedImpl(final @NotNull Function<? super Mqtt3Publish, P> parentConsumer) {
            this.parentConsumer = parentConsumer;
        }

        public WillNestedImpl(
                final @NotNull Mqtt3Publish publish, final @NotNull Function<? super Mqtt3Publish, P> parentConsumer) {

            super(publish);
            this.parentConsumer = parentConsumer;
        }

        @Override
        protected @NotNull Mqtt3WillPublishBuilder.Nested.Complete<P> self() {
            return this;
        }

        @Override
        public @NotNull P applyWillPublish() {
            return parentConsumer.apply(build());
        }
    }
}
