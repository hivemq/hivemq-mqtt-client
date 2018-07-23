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

package org.mqttbee.api.mqtt;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.mqtt.MqttWebSocketConfigImpl;
import org.mqttbee.util.FluentBuilder;

import java.util.function.Function;

/**
 * @author Christian Hoff
 */
public class MqttWebSocketConfigBuilder<P> extends FluentBuilder<MqttWebSocketConfig, P> {

    private String serverPath = MqttWebSocketConfigImpl.DEFAULT_SERVER_PATH;

    public MqttWebSocketConfigBuilder(@Nullable final Function<? super MqttWebSocketConfig, P> parentConsumer) {
        super(parentConsumer);
    }

    @NotNull
    public MqttWebSocketConfigBuilder<P> serverPath(@NotNull final String serverPath) {
        // remove any leading slashes
        this.serverPath = serverPath.replaceAll("^/+", "");
        return this;
    }

    @NotNull
    @Override
    public MqttWebSocketConfig build() {
        return new MqttWebSocketConfigImpl(serverPath);
    }

}
