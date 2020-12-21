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

package com.hivemq.client2.internal.mqtt.message.publish;

import com.hivemq.client2.internal.mqtt.datatypes.MqttTopicImpl;
import com.hivemq.client2.internal.mqtt.datatypes.MqttUserPropertiesImpl;
import com.hivemq.client2.internal.mqtt.datatypes.MqttUtf8StringImpl;
import com.hivemq.client2.internal.util.UnsignedDataTypes;
import com.hivemq.client2.mqtt.datatypes.MqttQos;
import com.hivemq.client2.mqtt.mqtt5.message.publish.Mqtt5PayloadFormatIndicator;
import com.hivemq.client2.mqtt.mqtt5.message.publish.Mqtt5WillPublish;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.jetbrains.annotations.Unmodifiable;

import java.nio.ByteBuffer;

/**
 * @author Silvio Giebl
 */
@Unmodifiable
public class MqttWillPublish extends MqttPublish implements Mqtt5WillPublish {

    private final @Range(from = 0, to = UnsignedDataTypes.UNSIGNED_INT_MAX_VALUE) long delayInterval;

    public MqttWillPublish(
            final @NotNull MqttTopicImpl topic,
            final @Nullable ByteBuffer payload,
            final @NotNull MqttQos qos,
            final boolean isRetain,
            final @Range(from = -1, to = UnsignedDataTypes.UNSIGNED_INT_MAX_VALUE) long messageExpiryInterval,
            final @Nullable Mqtt5PayloadFormatIndicator payloadFormatIndicator,
            final @Nullable MqttUtf8StringImpl contentType,
            final @Nullable MqttTopicImpl responseTopic,
            final @Nullable ByteBuffer correlationData,
            final @NotNull MqttUserPropertiesImpl userProperties,
            final @Range(from = 0, to = UnsignedDataTypes.UNSIGNED_INT_MAX_VALUE) long delayInterval) {

        super(topic, payload, qos, isRetain, messageExpiryInterval, payloadFormatIndicator, contentType, responseTopic,
                correlationData, userProperties, null);
        this.delayInterval = delayInterval;
    }

    @Override
    public @Range(from = 0, to = UnsignedDataTypes.UNSIGNED_INT_MAX_VALUE) long getDelayInterval() {
        return delayInterval;
    }

    @Override
    public @NotNull MqttWillPublish asWill() {
        return this;
    }

    @Override
    public MqttPublishBuilder.@NotNull WillDefault extendAsWill() {
        return new MqttPublishBuilder.WillDefault(this);
    }

    @Override
    protected @NotNull String toAttributeString() {
        return super.toAttributeString() + ", delayInterval=" + delayInterval;
    }

    @Override
    public @NotNull String toString() {
        return "MqttWillPublish{" + toAttributeString() + '}';
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MqttWillPublish) || !super.equals(o)) {
            return false;
        }
        final MqttWillPublish that = (MqttWillPublish) o;

        return delayInterval == that.delayInterval;
    }

    @Override
    protected boolean canEqual(final @Nullable Object o) {
        return o instanceof MqttWillPublish;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Long.hashCode(delayInterval);
        return result;
    }
}
