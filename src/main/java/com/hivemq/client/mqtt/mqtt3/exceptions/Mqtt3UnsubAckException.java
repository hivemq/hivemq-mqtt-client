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

package com.hivemq.client.mqtt.mqtt3.exceptions;

import com.hivemq.client.internal.mqtt.message.unsubscribe.unsuback.mqtt3.Mqtt3UnsubAckView;
import com.hivemq.client.mqtt.mqtt3.message.unsubscribe.unsuback.Mqtt3UnsubAck;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Silvio Giebl
 * @since 1.0
 */
public class Mqtt3UnsubAckException extends Mqtt3MessageException {

    public Mqtt3UnsubAckException(final @Nullable String message, final @Nullable Throwable cause) {
        super(message, cause);
    }

    private Mqtt3UnsubAckException(final @NotNull Mqtt3UnsubAckException e) {
        super(e);
    }

    @Override
    protected @NotNull Mqtt3UnsubAckException copy() {
        return new Mqtt3UnsubAckException(this);
    }

    @Override
    public @NotNull Mqtt3UnsubAck getMqttMessage() {
        return Mqtt3UnsubAckView.INSTANCE;
    }
}
