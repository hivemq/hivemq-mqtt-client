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

package org.mqttbee.internal.mqtt.message.auth;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.annotations.Immutable;
import org.mqttbee.internal.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.internal.mqtt.datatypes.MqttUtf8StringImpl;
import org.mqttbee.internal.mqtt.message.MqttMessageWithUserProperties;
import org.mqttbee.internal.util.ByteBufferUtil;
import org.mqttbee.internal.util.StringUtil;
import org.mqttbee.mqtt.mqtt5.message.auth.Mqtt5Auth;
import org.mqttbee.mqtt.mqtt5.message.auth.Mqtt5AuthReasonCode;

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

    @Override
    protected @NotNull String toAttributeString() {
        return "reasonCode= " + getReasonCode() + ", method=" + method +
                ((data == null) ? "" : ", data=" + data.remaining() + "byte") +
                StringUtil.prepend(", ", super.toAttributeString());
    }

    @Override
    public @NotNull String toString() {
        return "MqttAuth{" + toAttributeString() + '}';
    }
}
