/*
 * Copyright 2018 dc-square and the HiveMQ MQTT Client Project
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

package com.hivemq.client.mqtt.mqtt5.exceptions;

import com.hivemq.client.mqtt.mqtt5.message.publish.puback.Mqtt5PubAck;
import org.jetbrains.annotations.NotNull;

/**
 * @author Silvio Giebl
 * @since 1.0
 */
public class Mqtt5PubAckException extends Mqtt5MessageException {

    private final @NotNull Mqtt5PubAck pubAck;

    public Mqtt5PubAckException(final @NotNull Mqtt5PubAck pubAck, final @NotNull String message) {
        super(message);
        this.pubAck = pubAck;
    }

    @Override
    public @NotNull Mqtt5PubAck getMqttMessage() {
        return pubAck;
    }
}
