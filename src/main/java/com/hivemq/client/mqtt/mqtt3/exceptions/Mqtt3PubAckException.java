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

import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3PubAck;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Silvio Giebl
 * @since 1.0
 */
public class Mqtt3PubAckException extends Mqtt3MessageException {

    private final @NotNull Mqtt3PubAck pubAck;

    public Mqtt3PubAckException(
            final @NotNull Mqtt3PubAck pubAck, final @Nullable String message, final @Nullable Throwable cause) {

        super(message, cause);
        this.pubAck = pubAck;
    }

    private Mqtt3PubAckException(final @NotNull Mqtt3PubAckException e) {
        super(e);
        pubAck = e.pubAck;
    }

    @Override
    protected @NotNull Mqtt3PubAckException copy() {
        return new Mqtt3PubAckException(this);
    }

    @Override
    public @NotNull Mqtt3PubAck getMqttMessage() {
        return pubAck;
    }
}
