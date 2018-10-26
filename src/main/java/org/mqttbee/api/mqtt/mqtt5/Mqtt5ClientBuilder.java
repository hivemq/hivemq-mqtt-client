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

package org.mqttbee.api.mqtt.mqtt5;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.api.mqtt.AbstractMqttClientBuilder;
import org.mqttbee.api.mqtt.mqtt5.advanced.Mqtt5AdvancedClientData;
import org.mqttbee.mqtt.MqttClientData;
import org.mqttbee.mqtt.MqttRxClient;
import org.mqttbee.mqtt.MqttVersion;
import org.mqttbee.mqtt.advanced.MqttAdvancedClientData;
import org.mqttbee.util.MustNotBeImplementedUtil;

/**
 * @author Silvio Giebl
 */
public class Mqtt5ClientBuilder extends AbstractMqttClientBuilder<Mqtt5ClientBuilder> {

    private boolean followRedirects = false;
    private boolean allowServerReAuth = false;
    private @Nullable MqttAdvancedClientData advancedClientData;

    Mqtt5ClientBuilder() {}

    @Override
    protected @NotNull Mqtt5ClientBuilder self() {
        return this;
    }

    public @NotNull Mqtt5ClientBuilder followRedirects(final boolean followRedirects) {
        this.followRedirects = followRedirects;
        return this;
    }

    public @NotNull Mqtt5ClientBuilder allowServerReAuth(final boolean allowServerReAuth) {
        this.allowServerReAuth = allowServerReAuth;
        return this;
    }

    public @NotNull Mqtt5ClientBuilder advancedClientData(final @Nullable Mqtt5AdvancedClientData advancedClientData) {
        this.advancedClientData =
                MustNotBeImplementedUtil.checkNullOrNotImplemented(advancedClientData, MqttAdvancedClientData.class);
        return this;
    }

    public @NotNull Mqtt5RxClient buildRx() {
        return new MqttRxClient(buildClientData());
    }

    public @NotNull Mqtt5AsyncClient buildAsync() {
        throw new UnsupportedOperationException("not implemented yet");
    }

    public @NotNull Mqtt5BlockingClient buildBlocking() {
        throw new UnsupportedOperationException("not implemented yet");
    }

    private @NotNull MqttClientData buildClientData() {
        return new MqttClientData(MqttVersion.MQTT_5_0, identifier, serverHost, serverPort, sslConfig, webSocketConfig,
                followRedirects, allowServerReAuth, executorConfig, advancedClientData);
    }
}
