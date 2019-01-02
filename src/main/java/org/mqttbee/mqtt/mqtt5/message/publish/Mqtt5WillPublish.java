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

package org.mqttbee.mqtt.mqtt5.message.publish;

import org.jetbrains.annotations.NotNull;
import org.mqttbee.annotations.DoNotImplement;
import org.mqttbee.internal.mqtt.message.publish.MqttPublishBuilder;

/**
 * MQTT 5 Will Publish which can be a part of the CONNECT packet.
 *
 * @author Silvio Giebl
 */
@DoNotImplement
public interface Mqtt5WillPublish extends Mqtt5Publish {

    /**
     * The default delay of Will Publishes.
     */
    long DEFAULT_DELAY_INTERVAL = 0;

    static @NotNull Mqtt5WillPublishBuilder builder() {
        return new MqttPublishBuilder.WillDefault();
    }

    /**
     * @return the delay of this Will Publish. The default is {@link #DEFAULT_DELAY_INTERVAL}.
     */
    long getDelayInterval();

    @NotNull Mqtt5WillPublishBuilder.Complete extendAsWill();
}
