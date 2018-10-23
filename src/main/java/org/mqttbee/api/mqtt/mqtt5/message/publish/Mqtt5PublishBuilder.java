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
import org.mqttbee.mqtt.message.publish.MqttPublish;

import java.util.function.Function;

import static org.mqttbee.mqtt.message.publish.MqttPublish.DEFAULT_TOPIC_ALIAS_USAGE;

/**
 * @author Silvio Giebl
 */
public class Mqtt5PublishBuilder<P> extends AbstractMqtt5PublishBuilder<Mqtt5PublishBuilder<P>, Mqtt5Publish, P> {

    private @NotNull TopicAliasUsage topicAliasUsage = DEFAULT_TOPIC_ALIAS_USAGE;

    public Mqtt5PublishBuilder(final @Nullable Function<? super Mqtt5Publish, P> parentConsumer) {
        super(parentConsumer);
    }

    Mqtt5PublishBuilder(final @NotNull Mqtt5Publish publish) {
        super(publish);
        topicAliasUsage = publish.usesTopicAlias();
    }

    @Override
    @NotNull Mqtt5PublishBuilder<P> self() {
        return this;
    }

    public @NotNull Mqtt5PublishBuilder<P> useTopicAlias(final @NotNull TopicAliasUsage topicAliasUsage) {
        this.topicAliasUsage = topicAliasUsage;
        return this;
    }

    @Override
    public @NotNull Mqtt5Publish build() {
        Preconditions.checkNotNull(topic, "Topic must not be null.");
        return new MqttPublish(topic, payload, qos, retain, messageExpiryIntervalSeconds, payloadFormatIndicator,
                contentType, responseTopic, correlationData, topicAliasUsage, userProperties);
    }

    public @NotNull P applyPublish() {
        return apply();
    }
}
