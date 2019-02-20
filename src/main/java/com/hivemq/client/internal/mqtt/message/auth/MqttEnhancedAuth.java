/*
 * Copyright 2018 The HiveMQ MQTT Client Project
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

import com.hivemq.client.annotations.Immutable;
import com.hivemq.client.internal.mqtt.datatypes.MqttUtf8StringImpl;
import com.hivemq.client.internal.util.ByteBufferUtil;
import com.hivemq.client.mqtt.mqtt5.message.auth.Mqtt5EnhancedAuth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.Optional;

/**
 * @author Silvio Giebl
 */
@Immutable
public class MqttEnhancedAuth implements Mqtt5EnhancedAuth {

    private final @NotNull MqttUtf8StringImpl method;
    private final @Nullable ByteBuffer data;

    public MqttEnhancedAuth(final @NotNull MqttUtf8StringImpl method, final @Nullable ByteBuffer data) {
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

    private @NotNull String toAttributeString() {
        return "method=" + method + ((data == null) ? "" : ", data=" + data.remaining() + "byte");
    }

    @Override
    public @NotNull String toString() {
        return "MqttEnhancedAuth{" + toAttributeString() + '}';
    }
}
