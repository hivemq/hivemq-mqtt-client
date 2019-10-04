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
 */

package com.hivemq.client.internal.mqtt.exceptions.mqtt3;

import com.hivemq.client.internal.mqtt.message.connect.connack.MqttConnAck;
import com.hivemq.client.internal.mqtt.message.connect.connack.mqtt3.Mqtt3ConnAckView;
import com.hivemq.client.internal.mqtt.message.subscribe.suback.MqttSubAck;
import com.hivemq.client.internal.mqtt.message.subscribe.suback.mqtt3.Mqtt3SubAckView;
import com.hivemq.client.mqtt.exceptions.MqttSessionExpiredException;
import com.hivemq.client.mqtt.mqtt3.exceptions.*;
import com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5MessageException;
import com.hivemq.client.mqtt.mqtt5.message.Mqtt5Message;
import io.reactivex.functions.Function;
import org.jetbrains.annotations.NotNull;

/**
 * @author David Katz
 * @author Silvio Giebl
 */
public final class Mqtt3ExceptionFactory {

    public static final @NotNull Function<Throwable, Throwable> MAPPER = Mqtt3ExceptionFactory::map;
    public static final @NotNull java.util.function.Function<Throwable, Throwable> MAPPER_JAVA =
            Mqtt3ExceptionFactory::map;

    public static @NotNull Throwable map(final @NotNull Throwable throwable) {
        if (throwable instanceof RuntimeException) {
            return map((RuntimeException) throwable);
        }
        return throwable;
    }

    public static @NotNull RuntimeException map(final @NotNull RuntimeException e) {
        if (e instanceof Mqtt5MessageException) {
            return map((Mqtt5MessageException) e);
        }
        if (e instanceof MqttSessionExpiredException) {
            final Throwable cause = e.getCause();
            if (cause instanceof Mqtt5MessageException) {
                return new MqttSessionExpiredException(e.getMessage(), map((Mqtt5MessageException) cause));
            }
        }
        return e;
    }

    public static @NotNull RuntimeException mapWithStackTrace(final @NotNull RuntimeException e) {
        final RuntimeException mapped = map(e);
        if (mapped != e) {
            mapped.setStackTrace(e.getStackTrace());
        }
        return mapped;
    }

    public static @NotNull Mqtt3MessageException map(final @NotNull Mqtt5MessageException mqtt5MessageException) {
        final Mqtt5Message mqttMessage = mqtt5MessageException.getMqttMessage();
        final String message = mqtt5MessageException.getMessage();
        final Throwable cause = mqtt5MessageException.getCause();
        switch (mqttMessage.getType()) {
            case CONNACK:
                return new Mqtt3ConnAckException(Mqtt3ConnAckView.of((MqttConnAck) mqttMessage), message, cause);
            case DISCONNECT:
                return new Mqtt3DisconnectException(message, cause);
            case PUBACK:
                return new Mqtt3PubAckException(message, cause);
            case PUBREC:
                return new Mqtt3PubRecException(message, cause);
            case SUBACK:
                return new Mqtt3SubAckException(Mqtt3SubAckView.of((MqttSubAck) mqttMessage), message, cause);
            case UNSUBACK:
                return new Mqtt3UnsubAckException(message, cause);
            default:
                throw new IllegalStateException();
        }
    }

    private Mqtt3ExceptionFactory() {}
}
