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

package com.hivemq.client2.mqtt.mqtt5.exceptions;

import com.hivemq.client2.internal.util.AsyncRuntimeException;
import com.hivemq.client2.mqtt.mqtt5.message.Mqtt5Message;
import org.jetbrains.annotations.NotNull;

/**
 * @author Silvio Giebl
 * @since 1.0
 */
public abstract class Mqtt5MessageException extends AsyncRuntimeException {

    Mqtt5MessageException(final @NotNull String message) {
        super(message);
    }

    Mqtt5MessageException(final @NotNull Throwable cause) {
        super(cause.getMessage(), cause);
    }

    Mqtt5MessageException(final @NotNull Mqtt5MessageException e) {
        super(e);
    }

    public abstract @NotNull Mqtt5Message getMqttMessage();
}
