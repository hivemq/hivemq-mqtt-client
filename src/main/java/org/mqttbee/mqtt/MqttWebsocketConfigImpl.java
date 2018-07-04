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

import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.MqttWebsocketConfig;

/** @author David Katz */
public class MqttWebsocketConfigImpl implements MqttWebsocketConfig {

    public static final MqttWebsocketConfigImpl DEFAULT = new MqttWebsocketConfigImpl("");

    private final String serverPath;

    public MqttWebsocketConfigImpl(@NotNull final String serverPath) {
        // remove any leading slashes
        this.serverPath = serverPath.replaceAll("^/+", "");
    }

    @Override
    @NotNull
    public String getServerPath() {
        return serverPath;
    }
}
