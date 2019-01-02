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

package org.mqttbee.internal.mqtt.handler.connect;

import io.netty.bootstrap.Bootstrap;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.internal.disposables.EmptyDisposable;
import org.jetbrains.annotations.NotNull;
import org.mqttbee.internal.mqtt.MqttClientConfig;
import org.mqttbee.internal.mqtt.exceptions.MqttClientStateExceptions;
import org.mqttbee.internal.mqtt.message.connect.MqttConnect;
import org.mqttbee.internal.rx.SingleFlow;
import org.mqttbee.mqtt.MqttClientState;
import org.mqttbee.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;

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

        final SingleFlow.Default<Mqtt5ConnAck> flow = new SingleFlow.Default<>(observer);
        observer.onSubscribe(flow);

        final Bootstrap bootstrap = clientConfig.getClientComponent()
                .connectionComponentBuilder()
                .connect(connect)
                .connAckFlow(flow)
                .build()
                .bootstrap();

        bootstrap.connect(clientConfig.getServerHost(), clientConfig.getServerPort()).addListener(future -> {
            if (!future.isSuccess()) {
                onError(clientConfig, flow, future.cause());
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
