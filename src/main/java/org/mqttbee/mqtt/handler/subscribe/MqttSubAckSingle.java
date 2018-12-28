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

package org.mqttbee.mqtt.handler.subscribe;

import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.internal.disposables.EmptyDisposable;
import org.jetbrains.annotations.NotNull;
import org.mqttbee.api.mqtt.exceptions.NotConnectedException;
import org.mqttbee.api.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAck;
import org.mqttbee.mqtt.MqttClientConfig;
import org.mqttbee.mqtt.ioc.ClientComponent;
import org.mqttbee.mqtt.message.subscribe.MqttSubscribe;
import org.mqttbee.mqtt.message.subscribe.suback.MqttSubAck;
import org.mqttbee.rx.SingleFlow.DefaultSingleFlow;

/**
 * @author Silvio Giebl
 */
public class MqttSubAckSingle extends Single<Mqtt5SubAck> {

    private final @NotNull MqttSubscribe subscribe;
    private final @NotNull MqttClientConfig clientConfig;

    public MqttSubAckSingle(final @NotNull MqttSubscribe subscribe, final @NotNull MqttClientConfig clientConfig) {
        this.subscribe = subscribe;
        this.clientConfig = clientConfig;
    }

    @Override
    protected void subscribeActual(final @NotNull SingleObserver<? super Mqtt5SubAck> observer) {
        if (clientConfig.getState().isConnectedOrReconnect()) {
            final ClientComponent clientComponent = clientConfig.getClientComponent();
            final MqttSubscriptionHandler subscriptionHandler = clientComponent.subscriptionHandler();

            final DefaultSingleFlow<MqttSubAck> flow = new DefaultSingleFlow<>(observer);
            observer.onSubscribe(flow);
            subscriptionHandler.subscribe(subscribe, flow);
        } else {
            EmptyDisposable.error(new NotConnectedException(), observer);
        }
    }
}
