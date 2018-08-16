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

package org.mqttbee.api.mqtt.mqtt5.exceptions;

import org.jetbrains.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt5.message.Mqtt5Message;

import java.util.function.Consumer;

/**
 * @author Silvio Giebl
 */
public class Mqtt5MessageException extends RuntimeException {

    @SuppressWarnings("unchecked")
    public static <M extends Mqtt5Message> void when(
            @NotNull final Throwable throwable, @NotNull final Class<M> type, @NotNull final Consumer<M> consumer) {

        if (throwable instanceof Mqtt5MessageException) {
            final Mqtt5MessageException messageException = (Mqtt5MessageException) throwable;
            final Mqtt5Message message = messageException.getMqttMessage();
            if (type.isInstance(message)) {
                consumer.accept((M) message);
            }
        }
    }

    private final @NotNull Mqtt5Message mqtt5Message;

    public Mqtt5MessageException(final @NotNull Mqtt5Message mqtt5Message, final @NotNull String message) {
        super(message);
        this.mqtt5Message = mqtt5Message;
    }

    public Mqtt5MessageException(final @NotNull Mqtt5Message mqtt5Message, final @NotNull Throwable cause) {
        super(cause.getMessage(), cause);
        this.mqtt5Message = mqtt5Message;
    }

    public @NotNull Mqtt5Message getMqttMessage() {
        return mqtt5Message;
    }
}
