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

package org.mqttbee.mqtt.handler.connect;

import io.netty.bootstrap.Bootstrap;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.internal.disposables.EmptyDisposable;
import org.jetbrains.annotations.NotNull;
import org.mqttbee.api.mqtt.exceptions.AlreadyConnectedException;
import org.mqttbee.api.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import org.mqttbee.mqtt.MqttClientConnectionState;
import org.mqttbee.mqtt.MqttClientData;
import org.mqttbee.mqtt.message.connect.MqttConnect;
import org.mqttbee.rx.SingleFlow;

/**
 * @author Silvio Giebl
 */
public class MqttConnAckSingle extends Single<Mqtt5ConnAck> {

    private final @NotNull MqttClientData clientData;
    private final @NotNull MqttConnect connect;

    public MqttConnAckSingle(final @NotNull MqttClientData clientData, final @NotNull MqttConnect connect) {
        this.clientData = clientData;
        this.connect = connect;
    }

    @Override
    protected void subscribeActual(final @NotNull SingleObserver<? super Mqtt5ConnAck> observer) {
        if (!clientData.getRawConnectionState()
                .compareAndSet(MqttClientConnectionState.DISCONNECTED, MqttClientConnectionState.CONNECTING)) {
            EmptyDisposable.error(new AlreadyConnectedException(), observer);
            return;
        }

        final SingleFlow.DefaultSingleFlow<Mqtt5ConnAck> flow = new SingleFlow.DefaultSingleFlow<>(observer);
        observer.onSubscribe(flow);

        final Bootstrap bootstrap = clientData.getClientComponent()
                .connectionComponentBuilder()
                .connect(connect)
                .connAckFlow(flow)
                .build()
                .bootstrap();

        bootstrap.connect(clientData.getServerHost(), clientData.getServerPort()).addListener(future -> {
            if (!future.isSuccess()) {
                flow.onError(future.cause());
            }
        });
    }
}
