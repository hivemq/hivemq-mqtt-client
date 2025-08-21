/*
 * Copyright 2018-present HiveMQ and the HiveMQ Community
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

package com.hivemq.mqtt.client2.internal.handler.auth;

import com.hivemq.mqtt.client2.internal.MqttClientConfig;
import com.hivemq.mqtt.client2.mqtt5.auth.Mqtt5EnhancedAuthMechanism;
import io.netty.channel.ChannelHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Silvio Giebl
 */
public interface MqttAuthHandler extends ChannelHandler {

    static @NotNull MqttAuthHandler create(
            final @NotNull MqttClientConfig clientConfig,
            final @Nullable Mqtt5EnhancedAuthMechanism authMechanism) {
        return (authMechanism == null) ? MqttDisconnectOnAuthHandler.INSTANCE :
                new MqttConnectAuthHandler(clientConfig, authMechanism);
    }

    @NotNull String NAME = "auth";
}
