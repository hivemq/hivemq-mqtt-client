/*
 * Copyright 2018 dc-square and the HiveMQ MQTT Client Project
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

package com.hivemq.client.internal.mqtt.message.auth;

import com.hivemq.client.internal.mqtt.datatypes.MqttUserPropertiesImpl;
import com.hivemq.client.internal.mqtt.datatypes.MqttUserPropertiesImplBuilder;
import com.hivemq.client.internal.mqtt.datatypes.MqttUtf8StringImpl;
import com.hivemq.client.internal.mqtt.util.MqttChecks;
import com.hivemq.client.internal.util.Checks;
import com.hivemq.client.mqtt.datatypes.MqttUtf8String;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import com.hivemq.client.mqtt.mqtt5.message.auth.Mqtt5AuthBuilder;
import com.hivemq.client.mqtt.mqtt5.message.auth.Mqtt5AuthReasonCode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;

/**
 * @author Silvio Giebl
 */
public class MqttAuthBuilder implements Mqtt5AuthBuilder {

    private final @NotNull MqttUtf8StringImpl method;
    private @Nullable ByteBuffer data;
    private final @NotNull Mqtt5AuthReasonCode reasonCode;
    private @Nullable MqttUtf8StringImpl reasonString;
    private @NotNull MqttUserPropertiesImpl userProperties = MqttUserPropertiesImpl.NO_USER_PROPERTIES;

    public MqttAuthBuilder(final @NotNull Mqtt5AuthReasonCode reasonCode, final @NotNull MqttUtf8StringImpl method) {
        Checks.notNull(reasonCode, "Reason code");
        Checks.notNull(method, "Method");
        this.reasonCode = reasonCode;
        this.method = method;
    }

    @Override
    public @NotNull MqttAuthBuilder data(final byte @Nullable [] data) {
        this.data = MqttChecks.binaryDataOrNull(data, "Auth data");
        return this;
    }

    @Override
    public @NotNull MqttAuthBuilder data(final @Nullable ByteBuffer data) {
        this.data = MqttChecks.binaryDataOrNull(data, "Auth data");
        return this;
    }

    @Override
    public @NotNull MqttAuthBuilder reasonString(final @Nullable String reasonString) {
        this.reasonString = MqttChecks.reasonString(reasonString);
        return this;
    }

    @Override
    public @NotNull MqttAuthBuilder reasonString(final @Nullable MqttUtf8String reasonString) {
        this.reasonString = MqttChecks.reasonString(reasonString);
        return this;
    }

    @Override
    public @NotNull MqttAuthBuilder userProperties(final @Nullable Mqtt5UserProperties userProperties) {
        this.userProperties = MqttChecks.userProperties(userProperties);
        return this;
    }

    @Override
    public @NotNull MqttUserPropertiesImplBuilder.Nested<MqttAuthBuilder> userProperties() {
        return new MqttUserPropertiesImplBuilder.Nested<>(userProperties, this::userProperties);
    }

    public @NotNull MqttAuth build() {
        return new MqttAuth(reasonCode, method, data, reasonString, userProperties);
    }
}
