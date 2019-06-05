/*
 * Copyright 2018 dc-square and the HiveMQ MQTT Client Project
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

package com.hivemq.client.mqtt;

import com.hivemq.client.annotations.DoNotImplement;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;

/**
 * Connection configuration of a {@link MqttClient}.
 *
 * @author Silvio Giebl
 * @since 1.0
 */
@DoNotImplement
public interface MqttClientConnectionConfig {

    /**
     * @return the server address the client is currently connected to.
     * @since 1.1
     */
    @NotNull InetSocketAddress getServerAddress();

    /**
     * @return the keep alive in seconds that is used while the client is connected.
     */
    int getKeepAlive();

    /**
     * @return whether the client connected with a Will Publish message.
     */
    boolean hasWillPublish();
}
