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

package com.hivemq.client2.internal.mqtt.handler.auth;

import com.hivemq.client2.internal.mqtt.MqttClientConfig;
import com.hivemq.client2.internal.mqtt.MqttClientConnectionConfig;
import com.hivemq.client2.internal.mqtt.exceptions.MqttClientStateExceptions;
import com.hivemq.client2.internal.rx.CompletableFlow;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.CompletableObserver;
import io.reactivex.rxjava3.internal.disposables.EmptyDisposable;
import org.jetbrains.annotations.NotNull;

/**
 * @author Silvio Giebl
 */
public class MqttReAuthCompletable extends Completable {

    private final @NotNull MqttClientConfig clientConfig;

    public MqttReAuthCompletable(final @NotNull MqttClientConfig clientConfig) {
        this.clientConfig = clientConfig;
    }

    @Override
    protected void subscribeActual(final @NotNull CompletableObserver s) {
        final MqttClientConnectionConfig connectionConfig = clientConfig.getRawConnectionConfig();
        if (connectionConfig == null) {
            EmptyDisposable.error(MqttClientStateExceptions.notConnected(), s);
            return;
        }
        if (connectionConfig.getRawEnhancedAuthMechanism() == null) {
            EmptyDisposable.error(new UnsupportedOperationException(
                    "Reauth is not available if enhanced auth was not used during connect"), s);
            return;
        }
        final Channel channel = connectionConfig.getChannel();
        final ChannelHandler authHandler = channel.pipeline().get(MqttAuthHandler.NAME);
        if (authHandler == null) {
            EmptyDisposable.error(MqttClientStateExceptions.notConnected(), s);
            return;
        }
        if (!(authHandler instanceof MqttReAuthHandler)) {
            EmptyDisposable.error(new UnsupportedOperationException("Auth is still pending"), s);
            return;
        }
        final MqttReAuthHandler reAuthHandler = (MqttReAuthHandler) authHandler;
        final CompletableFlow flow = new CompletableFlow(s);
        s.onSubscribe(flow);
        reAuthHandler.reauth(flow);
    }
}
