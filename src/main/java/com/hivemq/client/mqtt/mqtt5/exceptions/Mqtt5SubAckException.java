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

package com.hivemq.client.mqtt.mqtt5.exceptions;

import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5SubAck;
import org.jetbrains.annotations.NotNull;

/**
 * @author Silvio Giebl
 * @since 1.0
 */
public class Mqtt5SubAckException extends Mqtt5MessageException {

    private final @NotNull Mqtt5SubAck subAck;

    public Mqtt5SubAckException(final @NotNull Mqtt5SubAck subAck, final @NotNull String message) {
        super(message);
        this.subAck = subAck;
    }

    private Mqtt5SubAckException(final @NotNull Mqtt5SubAckException e) {
        super(e);
        subAck = e.subAck;
    }

    @Override
    protected @NotNull Mqtt5SubAckException copy() {
        return new Mqtt5SubAckException(this);
    }

    @Override
    public @NotNull Mqtt5SubAck getMqttMessage() {
        return subAck;
    }
}
