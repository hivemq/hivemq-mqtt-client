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
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.datatypes.MqttQoS;
import org.mqttbee.api.mqtt.datatypes.MqttTopic;
import org.mqttbee.api.mqtt.datatypes.MqttUTF8String;
import org.mqttbee.api.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import org.mqttbee.mqtt.message.publish.MqttWillPublish;
import org.mqttbee.mqtt.util.MqttBuilderUtil;
import org.mqttbee.util.MustNotBeImplementedUtil;
import org.mqttbee.util.UnsignedDataTypes;

import java.nio.ByteBuffer;

import static org.mqttbee.mqtt.message.publish.MqttWillPublish.DEFAULT_DELAY_INTERVAL;

/**
 * @author Silvio Giebl
 */
public class Mqtt5WillPublishBuilder extends Mqtt5PublishBuilder {

    private long delayInterval = DEFAULT_DELAY_INTERVAL;

    Mqtt5WillPublishBuilder() {
    }

    Mqtt5WillPublishBuilder(@NotNull final Mqtt5Publish publish) {
        super(publish);
        if (publish instanceof Mqtt5WillPublish) {
            delayInterval =
                    MustNotBeImplementedUtil.checkNotImplemented(publish, MqttWillPublish.class).getDelayInterval();
        }
    }

    @NotNull
    @Override
    public Mqtt5WillPublishBuilder topic(@NotNull final String topic) {
        super.topic(topic);
        return this;
    }

    @NotNull
    @Override
    public Mqtt5WillPublishBuilder topic(@NotNull final MqttTopic topic) {
        super.topic(topic);
        return this;
    }

    @NotNull
    @Override
    public Mqtt5WillPublishBuilder payload(@Nullable final byte[] payload) {
        this.payload = MqttBuilderUtil.binaryDataOrNull(payload);
        return this;
    }

    @NotNull
    @Override
    public Mqtt5WillPublishBuilder payload(@Nullable final ByteBuffer payload) {
        this.payload = MqttBuilderUtil.binaryDataOrNull(payload);
        return this;
    }

    @NotNull
    @Override
    public Mqtt5WillPublishBuilder qos(@NotNull final MqttQoS qos) {
        super.qos(qos);
        return this;
    }

    @NotNull
    @Override
    public Mqtt5WillPublishBuilder retain(final boolean retain) {
        super.retain(retain);
        return this;
    }

    @NotNull
    @Override
    public Mqtt5WillPublishBuilder payloadFormatIndicator(
            @Nullable final Mqtt5PayloadFormatIndicator payloadFormatIndicator) {

        super.payloadFormatIndicator(payloadFormatIndicator);
        return this;
    }

    @NotNull
    @Override
    public Mqtt5WillPublishBuilder contentType(@Nullable final String contentType) {
        super.contentType(contentType);
        return this;
    }

    @NotNull
    @Override
    public Mqtt5WillPublishBuilder contentType(@Nullable final MqttUTF8String contentType) {
        super.contentType(contentType);
        return this;
    }

    @NotNull
    @Override
    public Mqtt5WillPublishBuilder responseTopic(@Nullable final String responseTopic) {
        super.responseTopic(responseTopic);
        return this;
    }

    @NotNull
    @Override
    public Mqtt5WillPublishBuilder responseTopic(@Nullable final MqttTopic responseTopic) {
        super.responseTopic(responseTopic);
        return this;
    }

    @NotNull
    @Override
    public Mqtt5WillPublishBuilder correlationData(@Nullable final byte[] correlationData) {
        super.correlationData(correlationData);
        return this;
    }

    @NotNull
    @Override
    public Mqtt5WillPublishBuilder correlationData(@Nullable final ByteBuffer correlationData) {
        super.correlationData(correlationData);
        return this;
    }

    @NotNull
    @Override
    @Deprecated
    public Mqtt5WillPublishBuilder topicAliasUsage(@NotNull final TopicAliasUsage topicAliasUsage) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public Mqtt5WillPublishBuilder userProperties(@NotNull final Mqtt5UserProperties userProperties) {
        super.userProperties(userProperties);
        return this;
    }

    @NotNull
    public Mqtt5WillPublishBuilder withDelayInterval(final long delayInterval) {
        Preconditions.checkArgument(UnsignedDataTypes.isUnsignedInt(delayInterval));
        this.delayInterval = delayInterval;
        return this;
    }

    @NotNull
    @Override
    public Mqtt5WillPublish build() {
        Preconditions.checkNotNull(topic);
        Preconditions.checkNotNull(qos);
        return new MqttWillPublish(topic, payload, qos, retain, messageExpiryInterval, payloadFormatIndicator,
                contentType, responseTopic, correlationData, userProperties, delayInterval);
    }

}
