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

package com.hivemq.client2.internal.mqtt;

import com.hivemq.client2.internal.util.Checks;
import com.hivemq.client2.internal.util.InetSocketAddressUtil;
import com.hivemq.client2.mqtt.MqttProxyConfigBuilder;
import com.hivemq.client2.mqtt.MqttProxyProtocol;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * @author Silvio Giebl
 */
public abstract class MqttProxyConfigImplBuilder<B extends MqttProxyConfigImplBuilder<B>> {

    private @NotNull MqttProxyProtocol protocol = MqttProxyConfigImpl.DEFAULT_PROXY_PROTOCOL;
    private @Nullable InetSocketAddress address;
    private @NotNull Object host = MqttProxyConfigImpl.DEFAULT_PROXY_HOST; // String or InetAddress
    private int port = -1;
    private @Nullable String username;
    private @Nullable String password;
    private @Range(from = 0, to = Integer.MAX_VALUE) int handshakeTimeoutMs =
            MqttProxyConfigImpl.DEFAULT_HANDSHAKE_TIMEOUT_MS;

    MqttProxyConfigImplBuilder() {}

    MqttProxyConfigImplBuilder(final @Nullable MqttProxyConfigImpl proxyConfig) {
        if (proxyConfig != null) {
            protocol = proxyConfig.getProtocol();
            address = proxyConfig.getAddress();
            username = proxyConfig.getRawUsername();
            password = proxyConfig.getRawPassword();
            handshakeTimeoutMs = proxyConfig.getHandshakeTimeoutMs();
        }
    }

    abstract @NotNull B self();

    public @NotNull B protocol(final @Nullable MqttProxyProtocol protocol) {
        this.protocol = Checks.notNull(protocol, "Proxy protocol");
        return self();
    }

    public @NotNull B address(final @Nullable InetSocketAddress address) {
        this.address = Checks.notNull(address, "Proxy address");
        return self();
    }

    public @NotNull B host(final @Nullable String host) {
        setProxyHost(Checks.notEmpty(host, "Proxy host"));
        return self();
    }

    public @NotNull B host(final @Nullable InetAddress host) {
        setProxyHost(Checks.notNull(host, "Proxy host"));
        return self();
    }

    private void setProxyHost(final @NotNull Object host) {
        this.host = host;
        if (address != null) {
            port = address.getPort();
            address = null;
        }
    }

    public @NotNull B port(final int port) {
        this.port = Checks.unsignedShort(port, "Proxy port");
        if (address != null) {
            final InetAddress inetAddress = address.getAddress();
            if (inetAddress != null) {
                host = inetAddress;
            } else {
                host = address.getHostString();
            }
            address = null;
        }
        return self();
    }

    public @NotNull B username(final @Nullable String username) {
        this.username = username;
        return self();
    }

    public @NotNull B password(final @Nullable String password) {
        this.password = password;
        return self();
    }

    public @NotNull B handshakeTimeout(final long timeout, final @Nullable TimeUnit timeUnit) {
        Checks.notNull(timeUnit, "Time unit");
        this.handshakeTimeoutMs = (int) Checks.range(timeUnit.toMillis(timeout), 0, Integer.MAX_VALUE,
                "Handshake timeout in milliseconds");
        return self();
    }

    private @NotNull InetSocketAddress getAddress() {
        if (address != null) {
            return address;
        }
        if (host instanceof InetAddress) {
            return new InetSocketAddress((InetAddress) host, getPort());
        }
        return InetSocketAddressUtil.create((String) host, getPort());
    }

    private int getPort() {
        if (port != -1) {
            return port;
        }
        switch (protocol) {
            case SOCKS_4:
            case SOCKS_5:
                return MqttProxyConfigImpl.DEFAULT_SOCKS_PROXY_PORT;
            case HTTP:
            default:
                return MqttProxyConfigImpl.DEFAULT_HTTP_PROXY_PORT;
        }
    }

    public @NotNull MqttProxyConfigImpl build() {
        return new MqttProxyConfigImpl(protocol, getAddress(), username, password, handshakeTimeoutMs);
    }

    public static class Default extends MqttProxyConfigImplBuilder<Default> implements MqttProxyConfigBuilder {

        public Default() {}

        Default(final @Nullable MqttProxyConfigImpl proxyConfig) {
            super(proxyConfig);
        }

        @Override
        @NotNull Default self() {
            return this;
        }
    }

    public static class Nested<P> extends MqttProxyConfigImplBuilder<Nested<P>>
            implements MqttProxyConfigBuilder.Nested<P> {

        private final @NotNull Function<? super MqttProxyConfigImpl, P> parentConsumer;

        Nested(
                final @Nullable MqttProxyConfigImpl proxyConfig,
                final @NotNull Function<? super MqttProxyConfigImpl, P> parentConsumer) {

            super(proxyConfig);
            this.parentConsumer = parentConsumer;
        }

        @Override
        @NotNull Nested<P> self() {
            return this;
        }

        @Override
        public @NotNull P applyProxyConfig() {
            return parentConsumer.apply(build());
        }
    }
}
