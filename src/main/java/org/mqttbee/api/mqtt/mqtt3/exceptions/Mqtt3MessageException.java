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

import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.mqtt3.message.Mqtt3Message;
import org.mqttbee.api.mqtt.mqtt5.exceptions.Mqtt5MessageException;
import org.mqttbee.api.mqtt.mqtt5.message.Mqtt5Message;

/**
 * @author David Katz
 */
public class Mqtt3MessageException extends Exception {

    private final Mqtt5MessageException mqttCause;

    public Mqtt3MessageException(@NotNull Mqtt5MessageException mqttCause) {
        super(mqttCause);
        this.mqttCause = mqttCause;
    }

    @NotNull
    public Mqtt3Message getMqttMessage() {
        return wrap(mqttCause.getMqttMessage());
    }

    @Override
    public String getMessage() {
        return mqttCause.getMessage();
    }

    private Mqtt3Message wrap(Mqtt5Message mqttMessage) {
        throw new UnsupportedOperationException("not implemented yet");
    }

}
