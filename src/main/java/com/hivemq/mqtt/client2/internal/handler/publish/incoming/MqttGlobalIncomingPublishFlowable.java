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

package com.hivemq.mqtt.client2.internal.handler.publish.incoming;

import com.hivemq.mqtt.client2.MqttGlobalPublishFilter;
import com.hivemq.mqtt.client2.internal.MqttClientConfig;
import com.hivemq.mqtt.client2.internal.handler.subscribe.MqttSubscriptionHandler;
import com.hivemq.mqtt.client2.mqtt5.message.publish.Mqtt5Publish;
import io.reactivex.rxjava3.core.Flowable;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Subscriber;

/**
 * @author Silvio Giebl
 */
public class MqttGlobalIncomingPublishFlowable extends Flowable<Mqtt5Publish> {

    private final @NotNull MqttGlobalPublishFilter filter;
    private final @NotNull MqttClientConfig clientConfig;
    private final boolean manualAcknowledgement;

    public MqttGlobalIncomingPublishFlowable(
            final @NotNull MqttGlobalPublishFilter filter,
            final @NotNull MqttClientConfig clientConfig,
            final boolean manualAcknowledgement) {
        this.filter = filter;
        this.clientConfig = clientConfig;
        this.manualAcknowledgement = manualAcknowledgement;
    }

    @Override
    protected void subscribeActual(final @NotNull Subscriber<? super Mqtt5Publish> subscriber) {
        final MqttIncomingQosHandler incomingQosHandler = clientConfig.getIncomingQosHandler();
        final MqttSubscriptionHandler subscriptionHandler = clientConfig.getSubscriptionHandler();

        final MqttGlobalIncomingPublishFlow flow =
                new MqttGlobalIncomingPublishFlow(subscriber, clientConfig, incomingQosHandler, filter,
                        manualAcknowledgement);
        subscriber.onSubscribe(flow);
        subscriptionHandler.subscribeGlobal(flow);
    }
}
