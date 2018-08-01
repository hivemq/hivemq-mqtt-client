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
import org.mqttbee.mqtt.message.publish.MqttWillPublish;
import org.mqttbee.mqtt.util.MqttBuilderUtil;
import org.mqttbee.util.MustNotBeImplementedUtil;
import org.mqttbee.util.UnsignedDataTypes;

import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static org.mqttbee.mqtt.message.publish.MqttWillPublish.DEFAULT_DELAY_INTERVAL;

/**
 * @author Silvio Giebl
 */
public class Mqtt5WillPublishBuilder<P> extends Mqtt5PublishBuilder<P> {

    @NotNull
    public static <P> Mqtt5WillPublishBuilder<P> create(
            @Nullable final Function<? super Mqtt5WillPublish, P> parentConsumer) {

        return new Mqtt5WillPublishBuilder<>(
                (parentConsumer == null) ? null : publish -> parentConsumer.apply((Mqtt5WillPublish) publish));
    }

    private long delayIntervalSeconds = DEFAULT_DELAY_INTERVAL;

    public Mqtt5WillPublishBuilder(@Nullable final Function<? super Mqtt5Publish, P> parentConsumer) {
        super(parentConsumer);
    }

    Mqtt5WillPublishBuilder(@NotNull final Mqtt5Publish publish) {
        super(publish);
        if (publish instanceof Mqtt5WillPublish) {
            delayIntervalSeconds =
                    MustNotBeImplementedUtil.checkNotImplemented(publish, MqttWillPublish.class).getDelayInterval();
        }
    }

    @NotNull
    @Override
    public Mqtt5WillPublishBuilder<P> topic(@NotNull final String topic) {
        super.topic(topic);
        return this;
    }

    @NotNull
    @Override
    public Mqtt5WillPublishBuilder<P> topic(@NotNull final MqttTopic topic) {
        super.topic(topic);
        return this;
    }

    @NotNull
    @Override
    public MqttTopicBuilder<? extends Mqtt5WillPublishBuilder<P>> topic() {
        return new MqttTopicBuilder<>("", this::topic);
    }

    @NotNull
    @Override
    public Mqtt5WillPublishBuilder<P> payload(@Nullable final byte[] payload) {
        this.payload = MqttBuilderUtil.binaryDataOrNull(payload);
        return this;
    }

    @NotNull
    @Override
    public Mqtt5WillPublishBuilder<P> payload(@Nullable final ByteBuffer payload) {
        this.payload = MqttBuilderUtil.binaryDataOrNull(payload);
        return this;
    }

    @NotNull
    @Override
    public Mqtt5WillPublishBuilder<P> qos(@NotNull final MqttQos qos) {
        super.qos(qos);
        return this;
    }

    @NotNull
    @Override
    public Mqtt5WillPublishBuilder<P> retain(final boolean retain) {
        super.retain(retain);
        return this;
    }

    @NotNull
    @Override
    public Mqtt5WillPublishBuilder<P> messageExpiryInterval(
            final long messageExpiryInterval, @NotNull final TimeUnit timeUnit) {

        super.messageExpiryInterval(messageExpiryInterval, timeUnit);
        return this;
    }

    @NotNull
    @Override
    public Mqtt5WillPublishBuilder<P> payloadFormatIndicator(
            @Nullable final Mqtt5PayloadFormatIndicator payloadFormatIndicator) {

        super.payloadFormatIndicator(payloadFormatIndicator);
        return this;
    }

    @NotNull
    @Override
    public Mqtt5WillPublishBuilder<P> contentType(@Nullable final String contentType) {
        super.contentType(contentType);
        return this;
    }

    @NotNull
    @Override
    public Mqtt5WillPublishBuilder<P> contentType(@Nullable final MqttUTF8String contentType) {
        super.contentType(contentType);
        return this;
    }

    @NotNull
    @Override
    public Mqtt5WillPublishBuilder<P> responseTopic(@Nullable final String responseTopic) {
        super.responseTopic(responseTopic);
        return this;
    }

    @NotNull
    @Override
    public Mqtt5WillPublishBuilder<P> responseTopic(@Nullable final MqttTopic responseTopic) {
        super.responseTopic(responseTopic);
        return this;
    }

    @NotNull
    @Override
    public MqttTopicBuilder<? extends Mqtt5WillPublishBuilder<P>> responseTopic() {
        return new MqttTopicBuilder<>("", this::responseTopic);
    }

    @NotNull
    @Override
    public Mqtt5WillPublishBuilder<P> correlationData(@Nullable final byte[] correlationData) {
        super.correlationData(correlationData);
        return this;
    }

    @NotNull
    @Override
    public Mqtt5WillPublishBuilder<P> correlationData(@Nullable final ByteBuffer correlationData) {
        super.correlationData(correlationData);
        return this;
    }

    @NotNull
    @Override
    @Deprecated
    public Mqtt5WillPublishBuilder<P> useTopicAlias(@NotNull final TopicAliasUsage topicAliasUsage) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public Mqtt5WillPublishBuilder<P> userProperties(@NotNull final Mqtt5UserProperties userProperties) {
        super.userProperties(userProperties);
        return this;
    }

    @NotNull
    @Override
    public Mqtt5UserPropertiesBuilder<? extends Mqtt5WillPublishBuilder<P>> userProperties() {
        return new Mqtt5UserPropertiesBuilder<>(this::userProperties);
    }

    @NotNull
    public Mqtt5WillPublishBuilder<P> delayInterval(final long delayInterval, @NotNull final TimeUnit timeUnit) {
        final long delayIntervalSeconds = timeUnit.toSeconds(delayInterval);
        Preconditions.checkArgument(UnsignedDataTypes.isUnsignedInt(delayIntervalSeconds),
                "The value of delay interval converted in seconds must not exceed the value range of unsigned int. Found: %s which is bigger than %s (max unsigned int).",
                delayIntervalSeconds, UnsignedDataTypes.UNSIGNED_INT_MAX_VALUE);

        this.delayIntervalSeconds = delayIntervalSeconds;
        return this;
    }

    @NotNull
    @Override
    public Mqtt5WillPublish build() {
        Preconditions.checkNotNull(topic, "Topic must not be null.");
        Preconditions.checkNotNull(qos, "QoS must not be null.");
        return new MqttWillPublish(topic, payload, qos, retain, messageExpiryIntervalSeconds, payloadFormatIndicator,
                contentType, responseTopic, correlationData, userProperties, delayIntervalSeconds);
    }

}
