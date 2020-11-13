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

package com.hivemq.client.internal.mqtt.handler.subscribe;

import com.hivemq.client.internal.mqtt.MqttClientConfig;
import com.hivemq.client.internal.mqtt.ioc.ClientComponent;
import com.hivemq.client.internal.mqtt.message.unsubscribe.MqttUnsubAck;
import com.hivemq.client.internal.mqtt.message.unsubscribe.MqttUnsubscribe;
import com.hivemq.client.mqtt.mqtt5.message.unsubscribe.Mqtt5UnsubAck;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import org.jetbrains.annotations.NotNull;

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
        final ClientComponent clientComponent = clientConfig.getClientComponent();
        final MqttSubscriptionHandler subscriptionHandler = clientComponent.subscriptionHandler();

        final MqttSubOrUnsubAckFlow<MqttUnsubAck> flow = new MqttSubOrUnsubAckFlow<>(observer, clientConfig);
        observer.onSubscribe(flow);
        subscriptionHandler.unsubscribe(unsubscribe, flow);
    }
}
