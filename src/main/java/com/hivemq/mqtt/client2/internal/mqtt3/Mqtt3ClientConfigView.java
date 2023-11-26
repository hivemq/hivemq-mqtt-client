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

package com.hivemq.mqtt.client2.internal.mqtt3;

import com.hivemq.mqtt.client2.MqttClientState;
import com.hivemq.mqtt.client2.MqttExecutorConfig;
import com.hivemq.mqtt.client2.MqttTransportConfig;
import com.hivemq.mqtt.client2.MqttVersion;
import com.hivemq.mqtt.client2.datatypes.MqttClientIdentifier;
import com.hivemq.mqtt.client2.internal.MqttClientConfig;
import com.hivemq.mqtt.client2.internal.message.auth.MqttSimpleAuth;
import com.hivemq.mqtt.client2.internal.message.auth.mqtt3.Mqtt3SimpleAuthView;
import com.hivemq.mqtt.client2.internal.message.publish.MqttWillPublish;
import com.hivemq.mqtt.client2.internal.message.publish.mqtt3.Mqtt3PublishView;
import com.hivemq.mqtt.client2.lifecycle.MqttAutoReconnect;
import com.hivemq.mqtt.client2.mqtt3.Mqtt3ClientConfig;
import com.hivemq.mqtt.client2.mqtt3.Mqtt3ClientConnectionConfig;
import com.hivemq.mqtt.client2.mqtt3.message.auth.Mqtt3SimpleAuth;
import com.hivemq.mqtt.client2.mqtt3.message.publish.Mqtt3Publish;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * @author Silvio Giebl
 */
public class Mqtt3ClientConfigView implements Mqtt3ClientConfig {

    private final @NotNull MqttClientConfig delegate;

    public Mqtt3ClientConfigView(final @NotNull MqttClientConfig delegate) {
        this.delegate = delegate;
    }

    @Override
    public @NotNull MqttVersion getMqttVersion() {
        return delegate.getMqttVersion();
    }

    @Override
    public @NotNull Optional<MqttClientIdentifier> getIdentifier() {
        return delegate.getIdentifier();
    }

    @Override
    public @NotNull MqttTransportConfig getTransportConfig() {
        return delegate.getTransportConfig();
    }

    @Override
    public @NotNull MqttExecutorConfig getExecutorConfig() {
        return delegate.getExecutorConfig();
    }

    @Override
    public @NotNull Optional<MqttAutoReconnect> getAutomaticReconnect() {
        return delegate.getAutomaticReconnect();
    }

    @Override
    public @NotNull Optional<Mqtt3SimpleAuth> getSimpleAuth() {
        return Optional.ofNullable(getRawSimpleAuth());
    }

    private @Nullable Mqtt3SimpleAuth getRawSimpleAuth() {
        final MqttSimpleAuth simpleAuth = delegate.getConnectDefaults().getSimpleAuth();
        return (simpleAuth == null) ? null : Mqtt3SimpleAuthView.of(simpleAuth);
    }

    @Override
    public @NotNull Optional<Mqtt3Publish> getWillPublish() {
        return Optional.ofNullable(getRawWillPublish());
    }

    private @Nullable Mqtt3Publish getRawWillPublish() {
        final MqttWillPublish willPublish = delegate.getConnectDefaults().getWillPublish();
        return (willPublish == null) ? null : Mqtt3PublishView.of(willPublish);
    }

    @Override
    public @NotNull MqttClientState getState() {
        return delegate.getState();
    }

    @Override
    public @NotNull Optional<Mqtt3ClientConnectionConfig> getConnectionConfig() {
        return Optional.ofNullable(delegate.getRawConnectionConfig());
    }
}
