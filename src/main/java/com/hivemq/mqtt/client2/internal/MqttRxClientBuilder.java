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

package com.hivemq.mqtt.client2.internal;

import com.hivemq.mqtt.client2.MqttVersion;
import com.hivemq.mqtt.client2.internal.advanced.MqttAdvancedConfig;
import com.hivemq.mqtt.client2.internal.advanced.MqttAdvancedConfigBuilder;
import com.hivemq.mqtt.client2.internal.collections.ImmutableList;
import com.hivemq.mqtt.client2.internal.lifecycle.MqttConnectedContextImpl;
import com.hivemq.mqtt.client2.internal.message.auth.MqttSimpleAuth;
import com.hivemq.mqtt.client2.internal.message.auth.MqttSimpleAuthBuilder;
import com.hivemq.mqtt.client2.internal.message.publish.MqttPublish;
import com.hivemq.mqtt.client2.internal.message.publish.MqttPublishBuilder;
import com.hivemq.mqtt.client2.internal.message.publish.MqttWillPublish;
import com.hivemq.mqtt.client2.internal.util.Checks;
import com.hivemq.mqtt.client2.lifecycle.MqttConnectedContext;
import com.hivemq.mqtt.client2.lifecycle.MqttConnectedListener;
import com.hivemq.mqtt.client2.mqtt5.Mqtt5ClientBuilder;
import com.hivemq.mqtt.client2.mqtt5.advanced.Mqtt5AdvancedConfig;
import com.hivemq.mqtt.client2.mqtt5.auth.Mqtt5EnhancedAuthMechanism;
import com.hivemq.mqtt.client2.mqtt5.lifecycle.Mqtt5ConnectedContext;
import com.hivemq.mqtt.client2.mqtt5.message.auth.Mqtt5SimpleAuth;
import com.hivemq.mqtt.client2.mqtt5.message.publish.Mqtt5Publish;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Silvio Giebl
 */
public class MqttRxClientBuilder extends MqttRxClientBuilderBase<MqttRxClientBuilder> implements Mqtt5ClientBuilder {

    private ImmutableList.@Nullable Builder<MqttConnectedListener<? super MqttConnectedContextImpl>>
            connectedListenersBuilder;
    private @NotNull MqttAdvancedConfig advancedConfig = MqttAdvancedConfig.DEFAULT;
    private @Nullable MqttSimpleAuth simpleAuth;
    private @Nullable Mqtt5EnhancedAuthMechanism enhancedAuthMechanism;
    private @Nullable MqttWillPublish willPublish;

    public MqttRxClientBuilder() {}

    MqttRxClientBuilder(
            final @NotNull MqttRxClientBuilderBase<?> clientBuilder,
            final @NotNull ImmutableList<MqttConnectedListener<? super MqttConnectedContext>> connectedListeners) {
        super(clientBuilder);
        if (!connectedListeners.isEmpty()) {
            connectedListenersBuilder = ImmutableList.builder(connectedListeners.size());
            connectedListenersBuilder.addAll(connectedListeners);
        }
    }

    @Override
    protected @NotNull MqttRxClientBuilder self() {
        return this;
    }

    @Override
    public @NotNull MqttRxClientBuilder addConnectedListener(
            final @Nullable MqttConnectedListener<? super Mqtt5ConnectedContext> connectedListener) {
        Checks.notNull(connectedListener, "Connected listener");
        if (connectedListenersBuilder == null) {
            connectedListenersBuilder = ImmutableList.builder();
        }
        connectedListenersBuilder.add(connectedListener);
        return this;
    }

    @Override
    public @NotNull MqttRxClientBuilder advancedConfig(final @NotNull Mqtt5AdvancedConfig advancedConfig) {
        this.advancedConfig = Checks.notImplemented(advancedConfig, MqttAdvancedConfig.class, "Advanced config");
        return this;
    }

    @Override
    public MqttAdvancedConfigBuilder.@NotNull Nested<MqttRxClientBuilder> advancedConfigWith() {
        return new MqttAdvancedConfigBuilder.Nested<>(advancedConfig, this::advancedConfig);
    }

    @Override
    public @NotNull MqttRxClientBuilder simpleAuth(final @Nullable Mqtt5SimpleAuth simpleAuth) {
        this.simpleAuth = Checks.notImplementedOrNull(simpleAuth, MqttSimpleAuth.class, "Simple auth");
        return this;
    }

    @Override
    public MqttSimpleAuthBuilder.@NotNull Nested<MqttRxClientBuilder> simpleAuthWith() {
        return new MqttSimpleAuthBuilder.Nested<>(this::simpleAuth);
    }

    @Override
    public @NotNull MqttRxClientBuilder enhancedAuth(final @Nullable Mqtt5EnhancedAuthMechanism enhancedAuthMechanism) {
        this.enhancedAuthMechanism = enhancedAuthMechanism;
        return this;
    }

    @Override
    public @NotNull MqttRxClientBuilder willPublish(final @Nullable Mqtt5Publish willPublish) {
        this.willPublish = (willPublish == null) ? null :
                Checks.notImplemented(willPublish, MqttPublish.class, "Will publish").asWill();
        return this;
    }

    @Override
    public MqttPublishBuilder.@NotNull WillNested<MqttRxClientBuilder> willPublishWith() {
        return new MqttPublishBuilder.WillNested<>(this::willPublish);
    }

    @Override
    public @NotNull MqttRxClient build() {
        return buildRx();
    }

    @Override
    public @NotNull MqttRxClient buildRx() {
        return new MqttRxClient(buildClientConfig());
    }

    @Override
    public @NotNull MqttAsyncClient buildAsync() {
        return buildRx().toAsync();
    }

    @Override
    public @NotNull MqttBlockingClient buildBlocking() {
        return buildRx().toBlocking();
    }

    private @NotNull ImmutableList<MqttConnectedListener<? super MqttConnectedContextImpl>> buildConnectedListeners() {
        return (connectedListenersBuilder == null) ? ImmutableList.of() : connectedListenersBuilder.build();
    }

    private @NotNull MqttClientConfig buildClientConfig() {
        return buildClientConfig(MqttVersion.MQTT_5_0, advancedConfig,
                MqttClientConfig.ConnectDefaults.of(simpleAuth, enhancedAuthMechanism, willPublish),
                buildConnectedListeners());
    }
}
