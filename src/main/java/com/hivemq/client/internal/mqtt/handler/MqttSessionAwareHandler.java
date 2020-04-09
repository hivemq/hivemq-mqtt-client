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

package com.hivemq.client.internal.mqtt.handler;

import com.hivemq.client.internal.mqtt.MqttClientConnectionConfig;
import com.hivemq.client.internal.mqtt.handler.disconnect.MqttDisconnectEvent;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoop;
import org.jetbrains.annotations.NotNull;

/**
 * @author Silvio Giebl
 */
public abstract class MqttSessionAwareHandler extends MqttConnectionAwareHandler {

    protected boolean hasSession;

    public void onSessionStartOrResume(
            final @NotNull MqttClientConnectionConfig connectionConfig, final @NotNull EventLoop eventLoop) {

        hasSession = true;
    }

    public void onSessionEnd(final @NotNull Throwable cause) {
        hasSession = false;
    }

    @Override
    protected void onDisconnectEvent(
            final @NotNull ChannelHandlerContext ctx, final @NotNull MqttDisconnectEvent disconnectEvent) {}

    @Override
    public boolean isSharable() {
        return ctx == null;
    }
}
