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

package com.hivemq.client2.internal.mqtt.handler.publish.incoming;

import com.hivemq.client2.internal.mqtt.MqttClientConfig;
import com.hivemq.client2.internal.mqtt.handler.subscribe.MqttSubscriptionHandler;
import com.hivemq.client2.internal.mqtt.ioc.ClientComponent;
import com.hivemq.client2.internal.mqtt.message.subscribe.MqttSubscribe;
import com.hivemq.client2.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.hivemq.client2.mqtt.mqtt5.message.subscribe.Mqtt5SubAck;
import com.hivemq.client2.rx.FlowableWithSingle;
import com.hivemq.client2.rx.reactivestreams.WithSingleSubscriber;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Subscriber;

/**
 * @author Silvio Giebl
 */
public class MqttSubscribedPublishFlowable extends FlowableWithSingle<Mqtt5Publish, Mqtt5SubAck> {

    private final @NotNull MqttSubscribe subscribe;
    private final @NotNull MqttClientConfig clientConfig;
    private final boolean manualAcknowledgement;

    public MqttSubscribedPublishFlowable(
            final @NotNull MqttSubscribe subscribe,
            final @NotNull MqttClientConfig clientConfig,
            final boolean manualAcknowledgement) {

        this.subscribe = subscribe;
        this.clientConfig = clientConfig;
        this.manualAcknowledgement = manualAcknowledgement;
    }

    @Override
    protected void subscribeActual(final @NotNull Subscriber<? super Mqtt5Publish> subscriber) {
        final ClientComponent clientComponent = clientConfig.getClientComponent();
        final MqttIncomingQosHandler incomingQosHandler = clientComponent.incomingQosHandler();
        final MqttSubscriptionHandler subscriptionHandler = clientComponent.subscriptionHandler();

        final MqttSubscribedPublishFlow flow =
                new MqttSubscribedPublishFlow(subscriber, clientConfig, incomingQosHandler, manualAcknowledgement);
        subscriber.onSubscribe(flow);
        subscriptionHandler.subscribe(subscribe, flow);
    }

    @Override
    protected void subscribeBothActual(
            final @NotNull WithSingleSubscriber<? super Mqtt5Publish, ? super Mqtt5SubAck> subscriber) {

        subscribeActual(subscriber);
    }
}
