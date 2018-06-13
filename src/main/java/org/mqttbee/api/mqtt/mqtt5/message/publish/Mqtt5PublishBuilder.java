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
import org.mqttbee.mqtt.datatypes.MqttTopicImpl;
import org.mqttbee.mqtt.datatypes.MqttUTF8StringImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.message.publish.MqttPublish;
import org.mqttbee.mqtt.util.MqttBuilderUtil;
import org.mqttbee.util.ByteBufferUtil;
import org.mqttbee.util.MustNotBeImplementedUtil;
import org.mqttbee.util.UnsignedDataTypes;

import java.nio.ByteBuffer;

import static org.mqttbee.mqtt.message.publish.MqttPublish.DEFAULT_TOPIC_ALIAS_USAGE;
import static org.mqttbee.mqtt.message.publish.MqttPublish.MESSAGE_EXPIRY_INTERVAL_INFINITY;

/**
 * @author Silvio Giebl
 */
public class Mqtt5PublishBuilder {

    MqttTopicImpl topic;
    ByteBuffer payload;
    MqttQoS qos;
    boolean retain;
    long messageExpiryInterval = MESSAGE_EXPIRY_INTERVAL_INFINITY;
    Mqtt5PayloadFormatIndicator payloadFormatIndicator;
    MqttUTF8StringImpl contentType;
    MqttTopicImpl responseTopic;
    ByteBuffer correlationData;
    private TopicAliasUsage topicAliasUsage = DEFAULT_TOPIC_ALIAS_USAGE;
    MqttUserPropertiesImpl userProperties = MqttUserPropertiesImpl.NO_USER_PROPERTIES;

    Mqtt5PublishBuilder() {
    }

    Mqtt5PublishBuilder(@NotNull final Mqtt5Publish publish) {
        final MqttPublish publishImpl = MustNotBeImplementedUtil.checkNotImplemented(publish, MqttPublish.class);
        topic = publishImpl.getTopic();
        payload = publishImpl.getRawPayload();
        qos = publishImpl.getQos();
        retain = publishImpl.isRetain();
        messageExpiryInterval = publishImpl.getRawMessageExpiryInterval();
        payloadFormatIndicator = publishImpl.getRawPayloadFormatIndicator();
        contentType = publishImpl.getRawContentType();
        responseTopic = publishImpl.getRawResponseTopic();
        correlationData = publishImpl.getRawCorrelationData();
        topicAliasUsage = publishImpl.getTopicAliasUsage();
        userProperties = publishImpl.getUserProperties();
    }

    @NotNull
    public Mqtt5PublishBuilder topic(@NotNull final String topic) {
        this.topic = MqttBuilderUtil.topic(topic);
        return this;
    }

    @NotNull
    public Mqtt5PublishBuilder topic(@NotNull final MqttTopic topic) {
        this.topic = MqttBuilderUtil.topic(topic);
        return this;
    }

    @NotNull
    public Mqtt5PublishBuilder payload(@Nullable final byte[] payload) {
        this.payload = (payload == null) ? null : ByteBufferUtil.wrap(payload);
        return this;
    }

    @NotNull
    public Mqtt5PublishBuilder payload(@Nullable final ByteBuffer payload) {
        this.payload = (payload == null) ? null : ByteBufferUtil.slice(payload);
        return this;
    }

    @NotNull
    public Mqtt5PublishBuilder qos(@NotNull final MqttQoS qos) {
        this.qos = Preconditions.checkNotNull(qos);
        return this;
    }

    @NotNull
    public Mqtt5PublishBuilder retain(final boolean retain) {
        this.retain = retain;
        return this;
    }

    @NotNull
    public Mqtt5PublishBuilder messageExpiryInterval(final long messageExpiryInterval) {
        Preconditions.checkArgument(UnsignedDataTypes.isUnsignedInt(messageExpiryInterval));
        this.messageExpiryInterval = messageExpiryInterval;
        return this;
    }

    @NotNull
    public Mqtt5PublishBuilder payloadFormatIndicator(
            @Nullable final Mqtt5PayloadFormatIndicator payloadFormatIndicator) {

        this.payloadFormatIndicator = payloadFormatIndicator;
        return this;
    }

    @NotNull
    public Mqtt5PublishBuilder contentType(@Nullable final String contentType) {
        this.contentType = MqttBuilderUtil.stringOrNull(contentType);
        return this;
    }

    @NotNull
    public Mqtt5PublishBuilder contentType(@Nullable final MqttUTF8String contentType) {
        this.contentType = MqttBuilderUtil.stringOrNull(contentType);
        return this;
    }

    @NotNull
    public Mqtt5PublishBuilder responseTopic(@Nullable final String responseTopic) {
        this.responseTopic = MqttBuilderUtil.topicOrNull(responseTopic);
        return this;
    }

    @NotNull
    public Mqtt5PublishBuilder responseTopic(@Nullable final MqttTopic responseTopic) {
        this.responseTopic = MqttBuilderUtil.topicOrNull(responseTopic);
        return this;
    }

    @NotNull
    public Mqtt5PublishBuilder correlationData(@Nullable final byte[] correlationData) {
        this.correlationData = MqttBuilderUtil.binaryDataOrNull(correlationData);
        return this;
    }

    @NotNull
    public Mqtt5PublishBuilder correlationData(@Nullable final ByteBuffer correlationData) {
        this.correlationData = MqttBuilderUtil.binaryDataOrNull(correlationData);
        return this;
    }

    @NotNull
    public Mqtt5PublishBuilder topicAliasUsage(@NotNull final TopicAliasUsage topicAliasUsage) {
        Preconditions.checkNotNull(topicAliasUsage);
        this.topicAliasUsage = topicAliasUsage;
        return this;
    }

    @NotNull
    public Mqtt5PublishBuilder userProperties(@NotNull final Mqtt5UserProperties userProperties) {
        this.userProperties = MqttBuilderUtil.userProperties(userProperties);
        return this;
    }

    @NotNull
    public Mqtt5Publish build() {
        Preconditions.checkNotNull(topic);
        Preconditions.checkNotNull(qos);
        return new MqttPublish(topic, payload, qos, retain, messageExpiryInterval, payloadFormatIndicator, contentType,
                responseTopic, correlationData, topicAliasUsage, userProperties);
    }

}
