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

import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.mqtt.datatypes.MqttClientIdentifierImpl;
import org.mqttbee.mqtt.message.MqttStatefulMessage;
import org.mqttbee.mqtt.message.auth.MqttEnhancedAuth;

import javax.annotation.concurrent.Immutable;

/**
 * @author Silvio Giebl
 */
@Immutable
public class MqttStatefulConnect extends MqttStatefulMessage<MqttConnect> {

    private final MqttClientIdentifierImpl clientIdentifier;
    private final MqttEnhancedAuth enhancedAuth;

    MqttStatefulConnect(
            @NotNull final MqttConnect connect, @NotNull final MqttClientIdentifierImpl clientIdentifier,
            @Nullable final MqttEnhancedAuth enhancedAuth) {

        super(connect);
        this.clientIdentifier = clientIdentifier;
        this.enhancedAuth = enhancedAuth;
    }

    @NotNull
    public MqttClientIdentifierImpl getClientIdentifier() {
        return clientIdentifier;
    }

    @Nullable
    public MqttEnhancedAuth getEnhancedAuth() {
        return enhancedAuth;
    }

}
