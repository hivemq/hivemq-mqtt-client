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
 */

package org.mqttbee.mqtt;

import org.jetbrains.annotations.NotNull;
import org.mqttbee.annotations.DoNotImplement;
import org.mqttbee.annotations.Immutable;
import org.mqttbee.internal.mqtt.MqttClientSslConfigImplBuilder;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.util.List;
import java.util.Optional;

/**
 * Configuration for the tls transport to use by {@link MqttClient MQTT clients}.
 *
 * @author Christoph Schäbel
 * @author Silvio Giebl
 */
@DoNotImplement
public interface MqttClientSslConfig {

    long DEFAULT_HANDSHAKE_TIMEOUT_MS = 10_000;

    static @NotNull MqttClientSslConfigBuilder builder() {
        return new MqttClientSslConfigImplBuilder.Default();
    }

    @NotNull Optional<KeyManagerFactory> getKeyManagerFactory();

    @NotNull Optional<TrustManagerFactory> getTrustManagerFactory();

    @NotNull Optional<@Immutable List<@NotNull String>> getCipherSuites();

    @NotNull Optional<@Immutable List<@NotNull String>> getProtocols();

    long getHandshakeTimeoutMs();
}
