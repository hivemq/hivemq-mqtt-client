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

package com.hivemq.client2.internal.mqtt.handler.publish.outgoing;

import com.hivemq.client2.internal.mqtt.MqttClientConfig;
import com.hivemq.client2.internal.mqtt.exceptions.MqttClientStateExceptions;
import com.hivemq.client2.internal.mqtt.ioc.ClientComponent;
import com.hivemq.client2.internal.mqtt.message.publish.MqttPublish;
import com.hivemq.client2.mqtt.mqtt5.message.publish.Mqtt5PublishResult;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.internal.subscriptions.EmptySubscription;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Subscriber;

/**
 * @author Silvio Giebl
 */
public class MqttAckFlowable extends Flowable<Mqtt5PublishResult> {

    private final @NotNull MqttClientConfig clientConfig;
    private final @NotNull Flowable<MqttPublish> publishFlowable;

    public MqttAckFlowable(
            final @NotNull MqttClientConfig clientConfig, final @NotNull Flowable<MqttPublish> publishFlowable) {

        this.clientConfig = clientConfig;
        this.publishFlowable = publishFlowable;
    }

    @Override
    protected void subscribeActual(final @NotNull Subscriber<? super Mqtt5PublishResult> subscriber) {
        if (clientConfig.getState().isConnectedOrReconnect()) {
            final ClientComponent clientComponent = clientConfig.getClientComponent();
            final MqttOutgoingQosHandler outgoingQosHandler = clientComponent.outgoingQosHandler();
            final MqttPublishFlowables publishFlowables = outgoingQosHandler.getPublishFlowables();

            final MqttAckFlowableFlow flow = new MqttAckFlowableFlow(subscriber, clientConfig, outgoingQosHandler);
            subscriber.onSubscribe(flow);
            publishFlowables.add(new MqttPublishFlowableAckLink(publishFlowable, flow));
        } else {
            EmptySubscription.error(MqttClientStateExceptions.notConnected(), subscriber);
        }
    }
}
