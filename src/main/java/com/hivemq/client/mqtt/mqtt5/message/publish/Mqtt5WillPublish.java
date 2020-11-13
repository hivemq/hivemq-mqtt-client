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

package com.hivemq.client.mqtt.mqtt5.message.publish;

import com.hivemq.client.internal.mqtt.message.publish.MqttPublishBuilder;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * MQTT 5 Will Publish which can be a part of an {@link com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5Connect MQTT 5
 * Connect message}.
 *
 * @author Silvio Giebl
 * @since 1.0
 */
@ApiStatus.NonExtendable
public interface Mqtt5WillPublish extends Mqtt5Publish {

    /**
     * The default delay of Will Publishes.
     */
    long DEFAULT_DELAY_INTERVAL = 0;

    /**
     * Creates a builder for a Will Publish.
     *
     * @return the created builder.
     */
    static @NotNull Mqtt5WillPublishBuilder builder() {
        return new MqttPublishBuilder.WillDefault();
    }

    /**
     * @return the delay of this Will Publish. The default is {@link #DEFAULT_DELAY_INTERVAL}.
     */
    long getDelayInterval();

    /**
     * Creates a builder for extending this Will Publish.
     *
     * @return the created builder.
     */
    Mqtt5WillPublishBuilder.@NotNull Complete extendAsWill();
}
