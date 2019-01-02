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

package org.mqttbee.internal.mqtt.handler.subscribe;

import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.internal.disposables.EmptyDisposable;
import org.jetbrains.annotations.NotNull;
import org.mqttbee.internal.mqtt.MqttClientConfig;
import org.mqttbee.internal.mqtt.exceptions.MqttClientStateExceptions;
import org.mqttbee.internal.mqtt.ioc.ClientComponent;
import org.mqttbee.internal.mqtt.message.unsubscribe.MqttUnsubscribe;
import org.mqttbee.internal.mqtt.message.unsubscribe.unsuback.MqttUnsubAck;
import org.mqttbee.mqtt.mqtt5.message.unsubscribe.unsuback.Mqtt5UnsubAck;

/**
 * @author Silvio Giebl
 */
public class MqttUnsubAckSingle extends Single<Mqtt5UnsubAck> {

    private final @NotNull MqttUnsubscribe unsubscribe;
    private final @NotNull MqttClientConfig clientConfig;

    public MqttUnsubAckSingle(
            final @NotNull MqttUnsubscribe unsubscribe, final @NotNull MqttClientConfig clientConfig) {

        this.unsubscribe = unsubscribe;
        this.clientConfig = clientConfig;
    }

    @Override
    protected void subscribeActual(final @NotNull SingleObserver<? super Mqtt5UnsubAck> observer) {
        if (clientConfig.getState().isConnectedOrReconnect()) {
            final ClientComponent clientComponent = clientConfig.getClientComponent();
            final MqttSubscriptionHandler subscriptionHandler = clientComponent.subscriptionHandler();

            final MqttSubOrUnsubAckFlow<MqttUnsubAck> flow = new MqttSubOrUnsubAckFlow<>(observer, clientConfig);
            observer.onSubscribe(flow);
            subscriptionHandler.unsubscribe(unsubscribe, flow);
        } else {
            EmptyDisposable.error(MqttClientStateExceptions.notConnected(), observer);
        }
    }
}
