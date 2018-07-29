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

package org.mqttbee.api.mqtt.exceptions;

import org.jetbrains.annotations.NotNull;

/**
 * Exception to indicate that an integer can not be encoded as a variable byte integer.
 * <p>
 * This exception does not have a stack trace.
 *
 * @author Silvio Giebl
 */
public class MqttVariableByteIntegerExceededException extends RuntimeException {

    /**
     * Creates a new MqttVariableByteIntegerExceededException with the name of the variable byte integer.
     *
     * @param name the name of the variable byte integer.
     */
    public MqttVariableByteIntegerExceededException(@NotNull final String name) {
        super("variable byte integer size exceeded for " + name);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }

}
