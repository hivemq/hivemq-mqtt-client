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

package org.mqttbee.internal.mqtt;

import org.jetbrains.annotations.NotNull;
import org.mqttbee.internal.mqtt.advanced.MqttClientAdvancedConfig;
import org.mqttbee.internal.mqtt.advanced.MqttClientAdvancedConfigBuilder;
import org.mqttbee.internal.util.Checks;
import org.mqttbee.mqtt.MqttVersion;
import org.mqttbee.mqtt.mqtt5.Mqtt5ClientBuilder;
import org.mqttbee.mqtt.mqtt5.advanced.Mqtt5ClientAdvancedConfig;

/**
 * @author Silvio Giebl
 */
public class MqttRxClientBuilder extends MqttRxClientBuilderBase<MqttRxClientBuilder> implements Mqtt5ClientBuilder {

    private @NotNull MqttClientAdvancedConfig advancedConfig = MqttClientAdvancedConfig.DEFAULT;

    public MqttRxClientBuilder() {}

    MqttRxClientBuilder(final @NotNull MqttRxClientBuilderBase clientBuilder) {
        super(clientBuilder);
    }

    @Override
    protected @NotNull MqttRxClientBuilder self() {
        return this;
    }

    @Override
    public @NotNull MqttRxClientBuilder advancedConfig(final @NotNull Mqtt5ClientAdvancedConfig advancedConfig) {
        this.advancedConfig = Checks.notImplemented(advancedConfig, MqttClientAdvancedConfig.class, "Advanced config");
        return this;
    }

    @Override
    public @NotNull MqttClientAdvancedConfigBuilder.Nested<MqttRxClientBuilder> advancedConfig() {
        return new MqttClientAdvancedConfigBuilder.Nested<>(advancedConfig, this::advancedConfig);
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
        return new MqttClientConfig(MqttVersion.MQTT_5_0, identifier, serverHost, serverPort, executorConfig, sslConfig,
                webSocketConfig, advancedConfig);
    }
}
