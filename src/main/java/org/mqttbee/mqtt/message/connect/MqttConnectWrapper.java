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
import org.mqttbee.mqtt.codec.encoder.provider.MqttMessageEncoderProvider;
import org.mqttbee.mqtt.datatypes.MqttClientIdentifierImpl;
import org.mqttbee.mqtt.message.MqttMessageWrapper;
import org.mqttbee.mqtt.message.auth.MqttEnhancedAuth;

import javax.annotation.concurrent.Immutable;

/**
 * @author Silvio Giebl
 */
@Immutable
public class MqttConnectWrapper
        extends MqttMessageWrapper<MqttConnectWrapper, MqttConnect, MqttMessageEncoderProvider<MqttConnectWrapper>> {

    private final MqttClientIdentifierImpl clientIdentifier;
    private final MqttEnhancedAuth enhancedAuth;

    MqttConnectWrapper(
            @NotNull final MqttConnect wrapped, @NotNull final MqttClientIdentifierImpl clientIdentifier,
            @Nullable final MqttEnhancedAuth enhancedAuth) {

        super(wrapped);
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

    @NotNull
    @Override
    protected MqttConnectWrapper getCodable() {
        return this;
    }

}
