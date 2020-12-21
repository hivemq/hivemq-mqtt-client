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

package com.hivemq.client2.mqtt.mqtt3.exceptions;

import com.hivemq.client2.internal.util.AsyncRuntimeException;
import com.hivemq.client2.mqtt.mqtt3.message.Mqtt3Message;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author David Katz
 * @author Silvio Giebl
 * @since 1.0
 */
public abstract class Mqtt3MessageException extends AsyncRuntimeException {

    Mqtt3MessageException(final @Nullable String message, final @Nullable Throwable cause) {
        super(message, cause);
    }

    Mqtt3MessageException(final @NotNull Mqtt3MessageException e) {
        super(e);
    }

    public abstract @NotNull Mqtt3Message getMqttMessage();
}
