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

package org.mqttbee.api.mqtt.mqtt3.exceptions;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.api.mqtt.mqtt3.message.Mqtt3Message;

import java.util.function.Consumer;

/**
 * @author David Katz
 * @author Silvio Giebl
 */
public class Mqtt3MessageException extends RuntimeException {

    @SuppressWarnings("unchecked")
    public static <M extends Mqtt3Message> void when(
            @NotNull final Throwable throwable, @NotNull final Class<M> type, @NotNull final Consumer<M> consumer) {

        if (throwable instanceof Mqtt3MessageException) {
            final Mqtt3MessageException messageException = (Mqtt3MessageException) throwable;
            final Mqtt3Message message = messageException.getMqttMessage();
            if (type.isInstance(message)) {
                consumer.accept((M) message);
            }
        }
    }

    private final @NotNull Mqtt3Message mqtt3Message;

    public Mqtt3MessageException(
            final @NotNull Mqtt3Message mqtt3Message, final @Nullable String message, final @Nullable Throwable cause) {

        super(message, cause);
        this.mqtt3Message = mqtt3Message;
    }

    public @NotNull Mqtt3Message getMqttMessage() {
        return mqtt3Message;
    }
}
