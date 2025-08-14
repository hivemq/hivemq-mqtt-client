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

package com.hivemq.client.internal.mqtt.handler.disconnect;

import com.hivemq.client.internal.mqtt.MqttClientConfig;
import com.hivemq.client.internal.mqtt.MqttClientConnectionConfig;
import com.hivemq.client.internal.mqtt.exceptions.MqttClientStateExceptions;
import com.hivemq.client.internal.mqtt.message.disconnect.MqttDisconnect;
import com.hivemq.client.internal.rx.CompletableFlow;
import io.netty.channel.Channel;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.CompletableObserver;
import io.reactivex.rxjava3.internal.disposables.EmptyDisposable;
import org.jetbrains.annotations.NotNull;

/**
 * @author Silvio Giebl
 */
public class MqttDisconnectCompletable extends Completable {

    private final @NotNull MqttClientConfig clientConfig;
    private final @NotNull MqttDisconnect disconnect;

    public MqttDisconnectCompletable(
            final @NotNull MqttClientConfig clientConfig, final @NotNull MqttDisconnect disconnect) {

        this.clientConfig = clientConfig;
        this.disconnect = disconnect;
    }

    @Override
    protected void subscribeActual(final @NotNull CompletableObserver s) {
        final MqttClientConnectionConfig connectionConfig = clientConfig.getRawConnectionConfig();
        if (connectionConfig == null) {
            EmptyDisposable.error(MqttClientStateExceptions.notConnected(), s);
            return;
        }
        final Channel channel = connectionConfig.getChannel();
        final MqttDisconnectHandler disconnectHandler =
                (MqttDisconnectHandler) channel.pipeline().get(MqttDisconnectHandler.NAME);
        if (disconnectHandler == null) {
            EmptyDisposable.error(MqttClientStateExceptions.notConnected(), s);
            return;
        }
        final CompletableFlow flow = new CompletableFlow(s);
        s.onSubscribe(flow);
        disconnectHandler.disconnect(disconnect, flow);
    }
}
