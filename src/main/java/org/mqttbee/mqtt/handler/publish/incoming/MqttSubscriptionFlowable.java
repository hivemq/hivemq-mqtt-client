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

package org.mqttbee.mqtt.handler.publish.incoming;

import io.reactivex.Flowable;
import io.reactivex.internal.subscriptions.EmptySubscription;
import org.jetbrains.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt5.message.subscribe.Mqtt5SubscribeResult;
import org.mqttbee.mqtt.MqttClientConfig;
import org.mqttbee.mqtt.exceptions.MqttClientStateExceptions;
import org.mqttbee.mqtt.handler.subscribe.MqttSubscriptionHandler;
import org.mqttbee.mqtt.ioc.ClientComponent;
import org.mqttbee.mqtt.message.subscribe.MqttSubscribe;
import org.reactivestreams.Subscriber;

/**
 * @author Silvio Giebl
 */
public class MqttSubscriptionFlowable extends Flowable<Mqtt5SubscribeResult> {

    private final @NotNull MqttSubscribe subscribe;
    private final @NotNull MqttClientConfig clientConfig;

    public MqttSubscriptionFlowable(
            final @NotNull MqttSubscribe subscribe, final @NotNull MqttClientConfig clientConfig) {

        this.subscribe = subscribe;
        this.clientConfig = clientConfig;
    }

    @Override
    protected void subscribeActual(final @NotNull Subscriber<? super Mqtt5SubscribeResult> subscriber) {
        if (clientConfig.getState().isConnectedOrReconnect()) {
            final ClientComponent clientComponent = clientConfig.getClientComponent();
            final MqttIncomingQosHandler incomingQosHandler = clientComponent.incomingQosHandler();
            final MqttSubscriptionHandler subscriptionHandler = clientComponent.subscriptionHandler();

            final MqttSubscriptionFlow flow = new MqttSubscriptionFlow(subscriber, incomingQosHandler);
            subscriptionHandler.subscribe(subscribe, flow);
            subscriber.onSubscribe(flow);
        } else {
            EmptySubscription.error(MqttClientStateExceptions.notConnected(), subscriber);
        }
    }
}
