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

package com.hivemq.client2.mqtt.exceptions;

import com.hivemq.client2.internal.util.AsyncRuntimeException;
import org.jetbrains.annotations.NotNull;

/**
 * Exception that is used if a MQTT connection is closed without a Disconnect message.
 *
 * @author Silvio Giebl
 * @since 1.0
 */
public class ConnectionClosedException extends AsyncRuntimeException {

    public ConnectionClosedException(final @NotNull String message) {
        super(message);
    }

    public ConnectionClosedException(final @NotNull Throwable cause) {
        super(cause);
    }

    private ConnectionClosedException(final @NotNull ConnectionClosedException e) {
        super(e);
    }

    @Override
    protected @NotNull ConnectionClosedException copy() {
        return new ConnectionClosedException(this);
    }
}
