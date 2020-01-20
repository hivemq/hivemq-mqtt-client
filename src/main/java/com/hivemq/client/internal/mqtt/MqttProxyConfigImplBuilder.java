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

package com.hivemq.client.internal.mqtt;

import com.hivemq.client.internal.util.Checks;
import com.hivemq.client.mqtt.MqttProxyConfigBuilder;
import com.hivemq.client.mqtt.MqttProxyType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.function.Function;

/**
 * @author Silvio Giebl
 */
public abstract class MqttProxyConfigImplBuilder<B extends MqttProxyConfigImplBuilder<B>> {

    private @NotNull MqttProxyType type = MqttProxyConfigImpl.DEFAULT_PROXY_TYPE;
    private @Nullable InetSocketAddress address;
    private @NotNull Object host = MqttProxyConfigImpl.DEFAULT_PROXY_HOST; // String or InetAddress
    private int port = -1;
    private @Nullable String username;
    private @Nullable String password;

    MqttProxyConfigImplBuilder() {}

    MqttProxyConfigImplBuilder(final @Nullable MqttProxyConfigImpl proxyConfig) {
        if (proxyConfig != null) {
            address = proxyConfig.getProxyAddress();
            username = proxyConfig.getRawProxyUsername();
            password = proxyConfig.getRawProxyPassword();
        }
    }

    abstract @NotNull B self();

    public @NotNull B proxyType(final @Nullable MqttProxyType type) {
        this.type = Checks.notNull(type, "Proxy type");
        return self();
    }

    public @NotNull B proxyAddress(final @Nullable InetSocketAddress address) {
        this.address = Checks.notNull(address, "Proxy address");
        return self();
    }

    public @NotNull B proxyHost(final @Nullable String host) {
        setProxyHost(Checks.notEmpty(host, "Proxy host"));
        return self();
    }

    public @NotNull B proxyHost(final @Nullable InetAddress host) {
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

    public @NotNull B proxyPort(final int port) {
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

    public @NotNull B proxyUsername(final @Nullable String username) {
        this.username = username;
        return self();
    }

    public @NotNull B proxyPassword(final @Nullable String password) {
        this.password = password;
        return self();
    }

    private @NotNull InetSocketAddress getProxyAddress() {
        if (address != null) {
            return address;
        }
        if (host instanceof InetAddress) {
            return new InetSocketAddress((InetAddress) host, getProxyPort());
        }
        return InetSocketAddress.createUnresolved((String) host, getProxyPort());
    }

    private int getProxyPort() {
        if (port != -1) {
            return port;
        }
        switch (type) {
            case SOCKS_4:
            case SOCKS_5:
                return MqttProxyConfigImpl.DEFAULT_SOCKS_PROXY_PORT;
            case HTTP:
            default:
                return MqttProxyConfigImpl.DEFAULT_HTTP_PROXY_PORT;
        }
    }

    public @NotNull MqttProxyConfigImpl build() {
        return new MqttProxyConfigImpl(type, getProxyAddress(), username, password);
    }

    public static class Default extends MqttProxyConfigImplBuilder<Default> implements MqttProxyConfigBuilder {

        public Default() {}

        Default(final @NotNull MqttProxyConfigImpl proxyConfig) {
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
