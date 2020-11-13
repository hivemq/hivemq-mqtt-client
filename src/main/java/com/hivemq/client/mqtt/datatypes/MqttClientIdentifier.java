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

package com.hivemq.client.mqtt.datatypes;

import com.hivemq.client.internal.mqtt.datatypes.MqttClientIdentifierImpl;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * MQTT Client Identifier according to the MQTT specification.
 * <p>
 * A Client Identifier has the same requirements as an {@link MqttUtf8String UTF-8 encoded string}.
 *
 * @author Silvio Giebl
 * @since 1.0
 */
@ApiStatus.NonExtendable
public interface MqttClientIdentifier extends MqttUtf8String {

    /**
     * Validates and creates a Client Identifier of the given string.
     *
     * @param string the string representation of the Client Identifier.
     * @return the created Client Identifier.
     * @throws IllegalArgumentException if the string is not a valid Client Identifier.
     */
    static @NotNull MqttClientIdentifier of(final @NotNull String string) {
        return MqttClientIdentifierImpl.of(string);
    }

    /**
     * Checks whether this Client Identifier must be allowed by every MQTT broker according to the MQTT specification.
     * <p>
     * A Client Identifier must be allowed by every MQTT broker if it is between 1 and 23 characters long and only
     * contains lower or uppercase alphabetical characters or numbers.
     *
     * @return whether this Client Identifier must be allowed by every MQTT broker.
     */
    boolean mustBeAllowedByServer();
}
