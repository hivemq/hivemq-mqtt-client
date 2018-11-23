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
import org.mqttbee.annotations.Immutable;
import org.mqttbee.api.mqtt.datatypes.MqttQos;
import org.mqttbee.api.mqtt.mqtt5.message.publish.Mqtt5PayloadFormatIndicator;
import org.mqttbee.api.mqtt.mqtt5.message.publish.Mqtt5WillPublish;
import org.mqttbee.api.mqtt.mqtt5.message.publish.TopicAliasUsage;
import org.mqttbee.mqtt.datatypes.MqttTopicImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.datatypes.MqttUtf8StringImpl;

import java.nio.ByteBuffer;

/**
 * @author Silvio Giebl
 */
@Immutable
public class MqttWillPublish extends MqttPublish implements Mqtt5WillPublish {

    private final long delayInterval;

    public MqttWillPublish(
            final @NotNull MqttTopicImpl topic, final @Nullable ByteBuffer payload, final @NotNull MqttQos qos,
            final boolean isRetain, final long messageExpiryInterval,
            final @Nullable Mqtt5PayloadFormatIndicator payloadFormatIndicator,
            final @Nullable MqttUtf8StringImpl contentType, final @Nullable MqttTopicImpl responseTopic,
            final @Nullable ByteBuffer correlationData, final @NotNull MqttUserPropertiesImpl userProperties,
            final long delayInterval) {

        super(topic, payload, qos, isRetain, messageExpiryInterval, payloadFormatIndicator, contentType, responseTopic,
                correlationData, TopicAliasUsage.NO, userProperties);
        this.delayInterval = delayInterval;
    }

    @Override
    public long getDelayInterval() {
        return delayInterval;
    }

    @Override
    public @NotNull MqttPublishBuilder.WillDefault extendAsWill() {
        return new MqttPublishBuilder.WillDefault(this);
    }
}
