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

package org.mqttbee.mqtt;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.api.mqtt.mqtt5.Mqtt5ClientBuilder;
import org.mqttbee.api.mqtt.mqtt5.advanced.Mqtt5AdvancedClientData;
import org.mqttbee.mqtt.advanced.MqttAdvancedClientData;
import org.mqttbee.mqtt.advanced.MqttAdvancedClientDataBuilder;
import org.mqttbee.util.Checks;

/**
 * @author Silvio Giebl
 */
public class MqttRxClientBuilder extends MqttRxClientBuilderBase<MqttRxClientBuilder> implements Mqtt5ClientBuilder {

    private boolean followRedirects = false;
    private boolean allowServerReAuth = false;
    private @Nullable MqttAdvancedClientData advancedClientData;

    public MqttRxClientBuilder() {}

    MqttRxClientBuilder(final @NotNull MqttRxClientBuilderBase clientBuilder) {
        super(clientBuilder);
    }

    @Override
    protected @NotNull MqttRxClientBuilder self() {
        return this;
    }

    @Override
    public @NotNull MqttRxClientBuilder followRedirects(final boolean followRedirects) {
        this.followRedirects = followRedirects;
        return this;
    }

    @Override
    public @NotNull MqttRxClientBuilder allowServerReAuth(final boolean allowServerReAuth) {
        this.allowServerReAuth = allowServerReAuth;
        return this;
    }

    @Override
    public @NotNull MqttRxClientBuilder advanced(final @Nullable Mqtt5AdvancedClientData advancedClientData) {
        this.advancedClientData =
                Checks.notImplementedOrNull(advancedClientData, MqttAdvancedClientData.class, "Advanced client data");
        return this;
    }

    @Override
    public @NotNull MqttAdvancedClientDataBuilder.Nested<MqttRxClientBuilder> advanced() {
        return new MqttAdvancedClientDataBuilder.Nested<>(this::advanced);
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
        return new MqttClientConfig(MqttVersion.MQTT_5_0, identifier, serverHost, serverPort, sslConfig,
                webSocketConfig,
                followRedirects, allowServerReAuth, executorConfig, advancedClientData);
    }
}
