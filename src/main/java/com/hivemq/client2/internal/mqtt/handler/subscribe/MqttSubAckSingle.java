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

package com.hivemq.client2.internal.mqtt.handler.subscribe;

import com.hivemq.client2.internal.mqtt.MqttClientConfig;
import com.hivemq.client2.internal.mqtt.ioc.ClientComponent;
import com.hivemq.client2.internal.mqtt.message.subscribe.MqttSubAck;
import com.hivemq.client2.internal.mqtt.message.subscribe.MqttSubscribe;
import com.hivemq.client2.mqtt.mqtt5.message.subscribe.Mqtt5SubAck;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleObserver;
import org.jetbrains.annotations.NotNull;

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
        final ClientComponent clientComponent = clientConfig.getClientComponent();
        final MqttSubscriptionHandler subscriptionHandler = clientComponent.subscriptionHandler();

        final MqttSubOrUnsubAckFlow<MqttSubAck> flow = new MqttSubOrUnsubAckFlow<>(observer, clientConfig);
        observer.onSubscribe(flow);
        subscriptionHandler.subscribe(subscribe, flow);
    }
}
