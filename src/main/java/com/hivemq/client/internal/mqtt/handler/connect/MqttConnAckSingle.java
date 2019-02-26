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

package com.hivemq.client.internal.mqtt.handler.connect;

import com.hivemq.client.internal.mqtt.MqttClientConfig;
import com.hivemq.client.internal.mqtt.exceptions.MqttClientStateExceptions;
import com.hivemq.client.internal.mqtt.message.connect.MqttConnect;
import com.hivemq.client.internal.rx.SingleFlow;
import com.hivemq.client.mqtt.MqttClientState;
import com.hivemq.client.mqtt.exceptions.ConnectionFailedException;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import io.netty.bootstrap.Bootstrap;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.internal.disposables.EmptyDisposable;
import org.jetbrains.annotations.NotNull;

/**
 * @author Silvio Giebl
 */
public class MqttConnAckSingle extends Single<Mqtt5ConnAck> {

    private final @NotNull MqttClientConfig clientConfig;
    private final @NotNull MqttConnect connect;

    public MqttConnAckSingle(final @NotNull MqttClientConfig clientConfig, final @NotNull MqttConnect connect) {
        this.clientConfig = clientConfig;
        this.connect = connect;
    }

    @Override
    protected void subscribeActual(final @NotNull SingleObserver<? super Mqtt5ConnAck> observer) {
        if (!clientConfig.getRawState().compareAndSet(MqttClientState.DISCONNECTED, MqttClientState.CONNECTING)) {
            EmptyDisposable.error(MqttClientStateExceptions.alreadyConnected(), observer);
            return;
        }

        final SingleFlow<Mqtt5ConnAck> flow = new SingleFlow<>(observer);
        observer.onSubscribe(flow);

        final Bootstrap bootstrap = clientConfig.getClientComponent()
                .connectionComponentBuilder()
                .connect(connect)
                .connAckFlow(flow)
                .build()
                .bootstrap();

        bootstrap.connect(clientConfig.getServerHost(), clientConfig.getServerPort()).addListener(future -> {
            if (!future.isSuccess()) {
                onError(clientConfig, flow, new ConnectionFailedException(future.cause()));
            }
        });
    }

    public static void onError(
            final @NotNull MqttClientConfig clientConfig, final @NotNull SingleFlow<Mqtt5ConnAck> flow,
            final @NotNull Throwable cause) {

        clientConfig.getRawState().set(MqttClientState.DISCONNECTED);
        flow.onError(cause);
        clientConfig.releaseEventLoop();
    }
}
