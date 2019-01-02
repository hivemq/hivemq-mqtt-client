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
 */

package org.mqttbee.internal.mqtt.mqtt3.exceptions;

import io.reactivex.functions.Function;
import org.jetbrains.annotations.NotNull;
import org.mqttbee.internal.mqtt.message.connect.connack.MqttConnAck;
import org.mqttbee.internal.mqtt.message.connect.connack.mqtt3.Mqtt3ConnAckView;
import org.mqttbee.internal.mqtt.message.subscribe.suback.MqttSubAck;
import org.mqttbee.internal.mqtt.message.subscribe.suback.mqtt3.Mqtt3SubAckView;
import org.mqttbee.mqtt.mqtt3.exceptions.*;
import org.mqttbee.mqtt.mqtt5.exceptions.Mqtt5MessageException;
import org.mqttbee.mqtt.mqtt5.message.Mqtt5Message;

/**
 * @author David Katz
 * @author Silvio Giebl
 */
public class Mqtt3ExceptionFactory {

    public static final @NotNull Function<Throwable, Throwable> MAPPER = Mqtt3ExceptionFactory::map;
    public static final @NotNull java.util.function.Function<Throwable, Throwable> MAPPER_JAVA =
            Mqtt3ExceptionFactory::map;

    public static @NotNull Throwable map(final @NotNull Throwable throwable) {
        if (throwable instanceof Mqtt5MessageException) {
            return map((Mqtt5MessageException) throwable);
        }
        return throwable;
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
}
