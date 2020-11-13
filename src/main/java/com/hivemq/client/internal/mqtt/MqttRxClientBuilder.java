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

import com.hivemq.client.internal.mqtt.advanced.MqttAdvancedConfig;
import com.hivemq.client.internal.mqtt.advanced.MqttAdvancedConfigBuilder;
import com.hivemq.client.internal.mqtt.message.auth.MqttSimpleAuth;
import com.hivemq.client.internal.mqtt.message.auth.MqttSimpleAuthBuilder;
import com.hivemq.client.internal.mqtt.message.publish.MqttPublish;
import com.hivemq.client.internal.mqtt.message.publish.MqttPublishBuilder;
import com.hivemq.client.internal.mqtt.message.publish.MqttWillPublish;
import com.hivemq.client.internal.util.Checks;
import com.hivemq.client.mqtt.MqttVersion;
import com.hivemq.client.mqtt.mqtt5.Mqtt5ClientBuilder;
import com.hivemq.client.mqtt.mqtt5.advanced.Mqtt5AdvancedConfig;
import com.hivemq.client.mqtt.mqtt5.auth.Mqtt5EnhancedAuthMechanism;
import com.hivemq.client.mqtt.mqtt5.message.auth.Mqtt5SimpleAuth;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Silvio Giebl
 */
public class MqttRxClientBuilder extends MqttRxClientBuilderBase<MqttRxClientBuilder> implements Mqtt5ClientBuilder {

    private @NotNull MqttAdvancedConfig advancedConfig = MqttAdvancedConfig.DEFAULT;
    private @Nullable MqttSimpleAuth simpleAuth;
    private @Nullable Mqtt5EnhancedAuthMechanism enhancedAuthMechanism;
    private @Nullable MqttWillPublish willPublish;

    public MqttRxClientBuilder() {}

    MqttRxClientBuilder(final @NotNull MqttRxClientBuilderBase<?> clientBuilder) {
        super(clientBuilder);
    }

    @Override
    protected @NotNull MqttRxClientBuilder self() {
        return this;
    }

    @Override
    public @NotNull MqttRxClientBuilder advancedConfig(final @NotNull Mqtt5AdvancedConfig advancedConfig) {
        this.advancedConfig = Checks.notImplemented(advancedConfig, MqttAdvancedConfig.class, "Advanced config");
        return this;
    }

    @Override
    public MqttAdvancedConfigBuilder.@NotNull Nested<MqttRxClientBuilder> advancedConfig() {
        return new MqttAdvancedConfigBuilder.Nested<>(advancedConfig, this::advancedConfig);
    }

    @Override
    public @NotNull MqttRxClientBuilder simpleAuth(final @Nullable Mqtt5SimpleAuth simpleAuth) {
        this.simpleAuth = Checks.notImplementedOrNull(simpleAuth, MqttSimpleAuth.class, "Simple auth");
        return this;
    }

    @Override
    public MqttSimpleAuthBuilder.@NotNull Nested<MqttRxClientBuilder> simpleAuth() {
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
    public MqttPublishBuilder.@NotNull WillNested<MqttRxClientBuilder> willPublish() {
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

    private @NotNull MqttClientConfig buildClientConfig() {
        return buildClientConfig(MqttVersion.MQTT_5_0, advancedConfig,
                MqttClientConfig.ConnectDefaults.of(simpleAuth, enhancedAuthMechanism, willPublish));
    }
}
