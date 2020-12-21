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

package com.hivemq.client2.internal.mqtt.message.auth;

import com.hivemq.client2.internal.mqtt.datatypes.MqttUtf8StringImpl;
import com.hivemq.client2.internal.mqtt.util.MqttChecks;
import com.hivemq.client2.internal.util.Checks;
import com.hivemq.client2.mqtt.mqtt5.message.auth.Mqtt5EnhancedAuthBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;

/**
 * @author Silvio Giebl
 */
public class MqttEnhancedAuthBuilder implements Mqtt5EnhancedAuthBuilder {

    private final @NotNull MqttUtf8StringImpl method;
    private @Nullable ByteBuffer data;

    public MqttEnhancedAuthBuilder(final @NotNull MqttUtf8StringImpl method) {
        Checks.notNull(method, "Method");
        this.method = method;
    }

    @Override
    public @NotNull MqttEnhancedAuthBuilder data(final byte @Nullable [] data) {
        this.data = MqttChecks.binaryDataOrNull(data, "Auth data");
        return this;
    }

    @Override
    public @NotNull MqttEnhancedAuthBuilder data(final @Nullable ByteBuffer data) {
        this.data = MqttChecks.binaryDataOrNull(data, "Auth data");
        return this;
    }

    public @NotNull MqttEnhancedAuth build() {
        return new MqttEnhancedAuth(method, data);
    }
}
