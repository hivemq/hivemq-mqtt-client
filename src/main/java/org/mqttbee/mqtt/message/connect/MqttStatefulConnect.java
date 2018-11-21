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

package org.mqttbee.mqtt.message.connect;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.annotations.Immutable;
import org.mqttbee.mqtt.datatypes.MqttClientIdentifierImpl;
import org.mqttbee.mqtt.message.MqttStatefulMessage;
import org.mqttbee.mqtt.message.auth.MqttEnhancedAuth;

/**
 * @author Silvio Giebl
 */
@Immutable
public class MqttStatefulConnect extends MqttStatefulMessage<MqttConnect> {

    private final @NotNull MqttClientIdentifierImpl clientIdentifier;
    private final @Nullable MqttEnhancedAuth enhancedAuth;

    MqttStatefulConnect(
            final @NotNull MqttConnect connect, final @NotNull MqttClientIdentifierImpl clientIdentifier,
            final @Nullable MqttEnhancedAuth enhancedAuth) {

        super(connect);
        this.clientIdentifier = clientIdentifier;
        this.enhancedAuth = enhancedAuth;
    }

    public @NotNull MqttClientIdentifierImpl getClientIdentifier() {
        return clientIdentifier;
    }

    public @Nullable MqttEnhancedAuth getEnhancedAuth() {
        return enhancedAuth;
    }
}
