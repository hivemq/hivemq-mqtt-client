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

package org.mqttbee.api.mqtt.mqtt3;

import com.google.common.base.Preconditions;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.MqttClientBuilder;
import org.mqttbee.api.mqtt.MqttClientExecutorConfig;
import org.mqttbee.api.mqtt.MqttClientSslConfig;
import org.mqttbee.api.mqtt.MqttWebsocketConfig;
import org.mqttbee.api.mqtt.datatypes.MqttClientIdentifier;
import org.mqttbee.mqtt.MqttClientData;
import org.mqttbee.mqtt.MqttClientExecutorConfigImpl;
import org.mqttbee.mqtt.MqttVersion;
import org.mqttbee.mqtt.datatypes.MqttClientIdentifierImpl;
import org.mqttbee.mqtt.mqtt3.Mqtt3ClientView;
import org.mqttbee.mqtt5.Mqtt5ClientImpl;

/**
 * @author Silvio Giebl
 */
public class Mqtt3ClientBuilder extends MqttClientBuilder {

    public Mqtt3ClientBuilder(
            @NotNull final MqttClientIdentifierImpl identifier, @NotNull final String serverHost, final int serverPort,
            @Nullable final MqttClientSslConfig sslConfig, @Nullable final MqttWebsocketConfig websocketConfig,
            @NotNull final MqttClientExecutorConfigImpl executorConfig) {

        Preconditions.checkNotNull(identifier);
        Preconditions.checkNotNull(serverHost);
        Preconditions.checkNotNull(executorConfig);

        this.identifier = identifier;
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.sslConfig = sslConfig;
        this.websocketConfig = websocketConfig;
        this.executorConfig = executorConfig;
    }

    @NotNull
    @Override
    public Mqtt3ClientBuilder identifier(@NotNull final String identifier) {
        super.identifier(identifier);
        return this;
    }

    @NotNull
    @Override
    public Mqtt3ClientBuilder identifier(@NotNull final MqttClientIdentifier identifier) {
        super.identifier(identifier);
        return this;
    }

    @NotNull
    @Override
    public Mqtt3ClientBuilder serverHost(@NotNull final String host) {
        super.serverHost(host);
        return this;
    }

    @NotNull
    @Override
    public Mqtt3ClientBuilder serverPort(final int port) {
        super.serverPort(port);
        return this;
    }

    @NotNull
    @Override
    public Mqtt3ClientBuilder useSsl() {
        super.useSsl();
        return this;
    }

    @NotNull
    @Override
    public Mqtt3ClientBuilder useSsl(@NotNull final MqttClientSslConfig sslConfig) {
        super.useSsl(sslConfig);
        return this;
    }

    @NotNull
    @Override
    public Mqtt3ClientBuilder useWebSockets() {
        super.useWebSockets();
        return this;
    }

    @NotNull
    @Override
    public Mqtt3ClientBuilder useWebSockets(@NotNull final MqttWebsocketConfig websocketConfig) {
        super.useWebSockets(websocketConfig);
        return this;
    }

    @NotNull
    @Override
    public Mqtt3ClientBuilder executorConfig(@NotNull final MqttClientExecutorConfig executorConfig) {
        super.executorConfig(executorConfig);
        return this;
    }

    @NotNull
    public Mqtt3Client buildReactive() {
        return new Mqtt3ClientView(new Mqtt5ClientImpl(buildClientData()));
    }

    private MqttClientData buildClientData() {
        return new MqttClientData(MqttVersion.MQTT_3_1_1, identifier, serverHost, serverPort, sslConfig,
                websocketConfig, false, false, executorConfig, null);
    }

}
