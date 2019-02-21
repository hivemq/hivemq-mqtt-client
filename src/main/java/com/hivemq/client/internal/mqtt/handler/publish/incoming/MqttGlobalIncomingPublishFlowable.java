/*
 * Copyright 2018 dc-square and the HiveMQ MQTT Client Project
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

package com.hivemq.client.internal.mqtt.handler.publish.incoming;

import com.hivemq.client.internal.mqtt.MqttClientConfig;
import com.hivemq.client.internal.mqtt.ioc.ClientComponent;
import com.hivemq.client.mqtt.MqttGlobalPublishFilter;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import io.reactivex.Flowable;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Subscriber;

/**
 * @author Silvio Giebl
 */
public class MqttGlobalIncomingPublishFlowable extends Flowable<Mqtt5Publish> {

    private final @NotNull MqttGlobalPublishFilter filter;
    private final @NotNull MqttClientConfig clientConfig;

    public MqttGlobalIncomingPublishFlowable(
            final @NotNull MqttGlobalPublishFilter filter, final @NotNull MqttClientConfig clientConfig) {

        this.filter = filter;
        this.clientConfig = clientConfig;
    }

    @Override
    protected void subscribeActual(final @NotNull Subscriber<? super Mqtt5Publish> subscriber) {
        final ClientComponent clientComponent = clientConfig.getClientComponent();
        final MqttIncomingQosHandler incomingQosHandler = clientComponent.incomingQosHandler();
        final MqttIncomingPublishFlows incomingPublishFlows = incomingQosHandler.getIncomingPublishFlows();

        final MqttGlobalIncomingPublishFlow flow =
                new MqttGlobalIncomingPublishFlow(subscriber, clientConfig, incomingQosHandler, filter);
        subscriber.onSubscribe(flow);
        flow.getEventLoop().execute(() -> {
            if (flow.init()) {
                incomingPublishFlows.subscribeGlobal(flow);
            }
        });
    }
}
