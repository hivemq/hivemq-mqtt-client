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

package org.mqttbee.api.mqtt.datatypes;

import org.jetbrains.annotations.NotNull;
import org.mqttbee.annotations.DoNotImplement;
import org.mqttbee.mqtt.util.MqttChecks;

/**
 * MQTT Client Identifier according to the MQTT specification.
 * <p>
 * A Client Identifier has the same restrictions from {@link MqttUtf8String}.
 *
 * @author Silvio Giebl
 */
@DoNotImplement
public interface MqttClientIdentifier extends MqttUtf8String {

    /**
     * Validates and creates a Client Identifier from the given string.
     *
     * @param string the Client Identifier as a UTF-16 encoded Java string.
     * @return the created Client Identifier.
     * @throws IllegalArgumentException if the string is not a valid Client Identifier.
     */
    static @NotNull MqttClientIdentifier from(final @NotNull String string) {
        return MqttChecks.clientIdentifier(string);
    }

    /**
     * Checks whether this Client Identifier must be allowed by a MQTT broker according to the MQTT specification.
     * <p>
     * A Client Identifier must be allowed by a MQTT broker if it is between 1 and 23 characters long and only contains
     * lower or uppercase alphabetical characters or numbers.
     *
     * @return whether this Client Identifier must be allowed by a MQTT broker.
     */
    boolean mustBeAllowedByServer();
}
