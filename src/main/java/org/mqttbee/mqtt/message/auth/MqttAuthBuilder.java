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

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.api.mqtt.datatypes.MqttUtf8String;
import org.mqttbee.api.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import org.mqttbee.api.mqtt.mqtt5.datatypes.Mqtt5UserPropertiesBuilder;
import org.mqttbee.api.mqtt.mqtt5.message.auth.Mqtt5AuthBuilder;
import org.mqttbee.api.mqtt.mqtt5.message.auth.Mqtt5AuthReasonCode;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.datatypes.MqttUtf8StringImpl;
import org.mqttbee.mqtt.util.MqttBuilderUtil;

import java.nio.ByteBuffer;

/**
 * @author Silvio Giebl
 */
public class MqttAuthBuilder implements Mqtt5AuthBuilder {

    private final MqttUtf8StringImpl method;
    private ByteBuffer data;
    private final Mqtt5AuthReasonCode reasonCode;
    private MqttUtf8StringImpl reasonString;
    private MqttUserPropertiesImpl userProperties = MqttUserPropertiesImpl.NO_USER_PROPERTIES;

    public MqttAuthBuilder(
            @NotNull final Mqtt5AuthReasonCode reasonCode, @NotNull final MqttUtf8StringImpl method) {

        Preconditions.checkNotNull(reasonCode, "Reason code must not be null.");
        Preconditions.checkNotNull(method, "Method must not be null.");
        this.reasonCode = reasonCode;
        this.method = method;
    }

    @NotNull
    @Override
    public MqttAuthBuilder data(@Nullable final byte[] data) {
        this.data = MqttBuilderUtil.binaryDataOrNull(data);
        return this;
    }

    @NotNull
    @Override
    public MqttAuthBuilder data(@Nullable final ByteBuffer data) {
        this.data = MqttBuilderUtil.binaryDataOrNull(data);
        return this;
    }

    @NotNull
    @Override
    public MqttAuthBuilder reasonString(@Nullable final String reasonString) {
        this.reasonString = MqttBuilderUtil.stringOrNull(reasonString);
        return this;
    }

    @NotNull
    @Override
    public MqttAuthBuilder reasonString(@Nullable final MqttUtf8String reasonString) {
        this.reasonString = MqttBuilderUtil.stringOrNull(reasonString);
        return this;
    }

    @NotNull
    @Override
    public MqttAuthBuilder userProperties(@NotNull final Mqtt5UserProperties userProperties) {
        this.userProperties = MqttBuilderUtil.userProperties(userProperties);
        return this;
    }

    @NotNull
    @Override
    public Mqtt5UserPropertiesBuilder<? extends MqttAuthBuilder> userProperties() {
        return new Mqtt5UserPropertiesBuilder<>(this::userProperties);
    }

    @NotNull
    public MqttAuth build() {
        return new MqttAuth(reasonCode, method, data, reasonString, userProperties);
    }

}
