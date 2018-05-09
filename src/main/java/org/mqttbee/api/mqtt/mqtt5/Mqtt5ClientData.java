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

import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.MqttClientData;
import org.mqttbee.api.mqtt.mqtt5.advanced.Mqtt5AdvancedClientData;
import org.mqttbee.api.mqtt.MqttClientSslData;

import java.util.Optional;

/**
 * @author Silvio Giebl
 */
public interface Mqtt5ClientData extends MqttClientData {

    boolean followsRedirects();

    boolean allowsServerReAuth();

    @NotNull
    Optional<Mqtt5AdvancedClientData> getAdvancedClientData();

    @NotNull
    Optional<Mqtt5ClientConnectionData> getClientConnectionData();

    @NotNull
    Optional<Mqtt5ServerConnectionData> getServerConnectionData();

    @NotNull
    Optional<MqttClientSslData> getSslData();

}
