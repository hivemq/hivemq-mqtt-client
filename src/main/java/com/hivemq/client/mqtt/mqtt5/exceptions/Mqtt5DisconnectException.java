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

import com.hivemq.client.mqtt.mqtt5.message.disconnect.Mqtt5Disconnect;
import org.jetbrains.annotations.NotNull;

/**
 * @author Silvio Giebl
 * @since 1.0
 */
public class Mqtt5DisconnectException extends Mqtt5MessageException {

    private final @NotNull Mqtt5Disconnect disconnect;

    public Mqtt5DisconnectException(final @NotNull Mqtt5Disconnect disconnect, final @NotNull String message) {
        super(message);
        this.disconnect = disconnect;
    }

    public Mqtt5DisconnectException(final @NotNull Mqtt5Disconnect disconnect, final @NotNull Throwable cause) {
        super(cause);
        this.disconnect = disconnect;
    }

    private Mqtt5DisconnectException(final @NotNull Mqtt5DisconnectException e) {
        super(e);
        disconnect = e.disconnect;
    }

    @Override
    protected @NotNull Mqtt5DisconnectException copy() {
        return new Mqtt5DisconnectException(this);
    }

    @Override
    public @NotNull Mqtt5Disconnect getMqttMessage() {
        return disconnect;
    }
}
