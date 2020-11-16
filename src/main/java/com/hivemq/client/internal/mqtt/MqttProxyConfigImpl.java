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

package com.hivemq.client.internal.mqtt;

import com.hivemq.client.mqtt.MqttProxyConfig;
import com.hivemq.client.mqtt.MqttProxyProtocol;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Silvio Giebl
 */
@Unmodifiable
public class MqttProxyConfigImpl implements MqttProxyConfig {

    private final @NotNull MqttProxyProtocol protocol;
    private final @NotNull InetSocketAddress address;
    private final @Nullable String username;
    private final @Nullable String password;
    private final int handshakeTimeoutMs;

    MqttProxyConfigImpl(
            final @NotNull MqttProxyProtocol protocol,
            final @NotNull InetSocketAddress address,
            final @Nullable String username,
            final @Nullable String password,
            final int handshakeTimeoutMs) {

        this.protocol = protocol;
        this.address = address;
        this.username = username;
        this.password = password;
        this.handshakeTimeoutMs = handshakeTimeoutMs;
    }

    @Override
    public @NotNull MqttProxyProtocol getProtocol() {
        return protocol;
    }

    @Override
    public @NotNull InetSocketAddress getAddress() {
        return address;
    }

    @Override
    public @NotNull Optional<String> getUsername() {
        return Optional.ofNullable(username);
    }

    public @Nullable String getRawUsername() {
        return username;
    }

    @Override
    public @NotNull Optional<String> getPassword() {
        return Optional.ofNullable(password);
    }

    public @Nullable String getRawPassword() {
        return password;
    }

    @Override
    public int getHandshakeTimeoutMs() {
        return handshakeTimeoutMs;
    }

    @Override
    public MqttProxyConfigImplBuilder.@NotNull Default extend() {
        return new MqttProxyConfigImplBuilder.Default(this);
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MqttProxyConfigImpl)) {
            return false;
        }
        final MqttProxyConfigImpl that = (MqttProxyConfigImpl) o;

        return (protocol == that.protocol) && address.equals(that.address) && Objects.equals(username, that.username) &&
                Objects.equals(password, that.password) && (handshakeTimeoutMs == that.handshakeTimeoutMs);
    }

    @Override
    public int hashCode() {
        int result = protocol.hashCode();
        result = 31 * result + address.hashCode();
        result = 31 * result + Objects.hashCode(username);
        result = 31 * result + Objects.hashCode(password);
        result = 31 * result + Integer.hashCode(handshakeTimeoutMs);
        return result;
    }
}
