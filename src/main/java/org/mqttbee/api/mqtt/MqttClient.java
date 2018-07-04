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

package org.mqttbee.api.mqtt;

import org.mqttbee.annotations.DoNotImplement;
import org.mqttbee.annotations.NotNull;

/**
 * Common interface for MQTT clients.
 *
 * @author Silvio Giebl
 */
@DoNotImplement
public interface MqttClient {

    String DEFAULT_SERVER_HOST = "localhost";
    int DEFAULT_SERVER_PORT = 1883;
    int DEFAULT_SERVER_PORT_SSL = 8883;
    int DEFAULT_SERVER_PORT_WEBSOCKET = 80;
    int DEFAULT_SERVER_PORT_WEBSOCKET_SSL = 443;

    @NotNull
    static MqttClientBuilder builder() {
        return new MqttClientBuilder();
    }

    /** @return the client specific data. */
    @NotNull
    MqttClientData getClientData();
}
