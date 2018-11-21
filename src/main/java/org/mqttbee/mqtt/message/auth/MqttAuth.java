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

package org.mqttbee.mqtt.message.auth;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.annotations.Immutable;
import org.mqttbee.api.mqtt.mqtt5.message.auth.Mqtt5Auth;
import org.mqttbee.api.mqtt.mqtt5.message.auth.Mqtt5AuthReasonCode;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.datatypes.MqttUtf8StringImpl;
import org.mqttbee.mqtt.message.MqttMessageWithUserProperties;
import org.mqttbee.util.ByteBufferUtil;

import java.nio.ByteBuffer;
import java.util.Optional;

/**
 * @author Silvio Giebl
 */
@Immutable
public class MqttAuth extends MqttMessageWithUserProperties.WithReason.WithCode<Mqtt5AuthReasonCode>
        implements Mqtt5Auth {

    private final @NotNull MqttUtf8StringImpl method;
    private final @Nullable ByteBuffer data;

    public MqttAuth(
            final @NotNull Mqtt5AuthReasonCode reasonCode, final @NotNull MqttUtf8StringImpl method,
            final @Nullable ByteBuffer data, final @Nullable MqttUtf8StringImpl reasonString,
            final @NotNull MqttUserPropertiesImpl userProperties) {

        super(reasonCode, reasonString, userProperties);
        this.method = method;
        this.data = data;
    }

    @Override
    public @NotNull MqttUtf8StringImpl getMethod() {
        return method;
    }

    @Override
    public @NotNull Optional<ByteBuffer> getData() {
        return ByteBufferUtil.optionalReadOnly(data);
    }

    public @Nullable ByteBuffer getRawData() {
        return data;
    }
}
