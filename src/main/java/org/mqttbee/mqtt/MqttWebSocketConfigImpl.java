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
import org.mqttbee.api.mqtt.MqttWebSocketConfig;

/**
 * @author David Katz
 * @author Christian Hoff
 */
public class MqttWebSocketConfigImpl implements MqttWebSocketConfig {
    public static final String DEFAULT_SERVER_PATH = "";
    // https://www.iana.org/assignments/websocket/websocket.xml#subprotocol-name
    public static final String DEFAULT_MQTT_SUBPROTOCOL = "mqtt";

    public static final MqttWebSocketConfigImpl DEFAULT =
            new MqttWebSocketConfigImpl(DEFAULT_SERVER_PATH, DEFAULT_MQTT_SUBPROTOCOL);

    private final String serverPath;
    private final String subprotocol;

    public MqttWebSocketConfigImpl(@NotNull final String serverPath, @NotNull final String subprotocol) {
        // remove any leading slashes
        this.serverPath = serverPath.replaceAll("^/+", "");
        this.subprotocol = subprotocol;
    }

    @Override
    @NotNull
    public String getServerPath() {
        return serverPath;
    }

    @Override
    @NotNull
    public String getSubprotocol() {
        return subprotocol;
    }

}
