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
public class Mqtt5WillPublishBuilder<P>
        extends AbstractMqtt5PublishBuilder<Mqtt5WillPublishBuilder<P>, Mqtt5WillPublish, P> {

    private long delayIntervalSeconds = DEFAULT_DELAY_INTERVAL;

    public Mqtt5WillPublishBuilder(final @Nullable Function<? super Mqtt5WillPublish, P> parentConsumer) {
        super(parentConsumer);
    }

    Mqtt5WillPublishBuilder(final @NotNull Mqtt5Publish publish) {
        super(publish);
        if (publish instanceof Mqtt5WillPublish) {
            delayIntervalSeconds =
                    MustNotBeImplementedUtil.checkNotImplemented(publish, MqttWillPublish.class).getDelayInterval();
        } else {
            payload(payload);
        }
    }

    @Override
    @NotNull Mqtt5WillPublishBuilder<P> self() {
        return this;
    }

    @Override
    public @NotNull Mqtt5WillPublishBuilder<P> payload(final @Nullable byte[] payload) {
        this.payload = MqttBuilderUtil.binaryDataOrNull(payload);
        return this;
    }

    @Override
    public @NotNull Mqtt5WillPublishBuilder<P> payload(final @Nullable ByteBuffer payload) {
        this.payload = MqttBuilderUtil.binaryDataOrNull(payload);
        return this;
    }

    public @NotNull Mqtt5WillPublishBuilder<P> delayInterval(
            final long delayInterval, final @NotNull TimeUnit timeUnit) {

        final long delayIntervalSeconds = timeUnit.toSeconds(delayInterval);
        Preconditions.checkArgument(UnsignedDataTypes.isUnsignedInt(delayIntervalSeconds),
                "The value of delay interval converted in seconds must not exceed the value range of unsigned int. Found: %s which is bigger than %s (max unsigned int).",
                delayIntervalSeconds, UnsignedDataTypes.UNSIGNED_INT_MAX_VALUE);

        this.delayIntervalSeconds = delayIntervalSeconds;
        return this;
    }

    @Override
    public @NotNull Mqtt5WillPublish build() {
        Preconditions.checkNotNull(topic, "Topic must not be null.");
        return new MqttWillPublish(topic, payload, qos, retain, messageExpiryIntervalSeconds, payloadFormatIndicator,
                contentType, responseTopic, correlationData, userProperties, delayIntervalSeconds);
    }

    public @NotNull P applyWillPublish() {
        return apply();
    }
}
