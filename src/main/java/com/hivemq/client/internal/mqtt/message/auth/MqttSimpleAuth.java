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

package com.hivemq.client.internal.mqtt.message.auth;

import com.hivemq.client.internal.mqtt.datatypes.MqttUtf8StringImpl;
import com.hivemq.client.internal.util.ByteBufferUtil;
import com.hivemq.client.mqtt.datatypes.MqttUtf8String;
import com.hivemq.client.mqtt.mqtt5.message.auth.Mqtt5SimpleAuth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Silvio Giebl
 */
@Unmodifiable
public class MqttSimpleAuth implements Mqtt5SimpleAuth {

    private final @Nullable MqttUtf8StringImpl username;
    private final @Nullable ByteBuffer password;

    public MqttSimpleAuth(final @Nullable MqttUtf8StringImpl username, final @Nullable ByteBuffer password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public @NotNull Optional<MqttUtf8String> getUsername() {
        return Optional.ofNullable(username);
    }

    public @Nullable MqttUtf8StringImpl getRawUsername() {
        return username;
    }

    @Override
    public @NotNull Optional<ByteBuffer> getPassword() {
        return ByteBufferUtil.optionalReadOnly(password);
    }

    public @Nullable ByteBuffer getRawPassword() {
        return password;
    }

    private @NotNull String toAttributeString() {
        return ((username == null) ? (password == null ? "" : "password") :
                (password == null) ? "username" : "username and password");
    }

    @Override
    public @NotNull String toString() {
        return "MqttSimpleAuth{" + toAttributeString() + '}';
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MqttSimpleAuth)) {
            return false;
        }
        final MqttSimpleAuth that = (MqttSimpleAuth) o;

        return Objects.equals(username, that.username) && Objects.equals(password, that.password);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(username);
        result = 31 * result + Objects.hashCode(password);
        return result;
    }
}
