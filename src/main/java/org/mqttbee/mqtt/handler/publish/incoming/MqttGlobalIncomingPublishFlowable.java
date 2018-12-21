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

import io.netty.channel.EventLoop;
import io.reactivex.Flowable;
import org.jetbrains.annotations.NotNull;
import org.mqttbee.api.mqtt.MqttGlobalPublishFilter;
import org.mqttbee.api.mqtt.mqtt5.message.publish.Mqtt5Publish;
import org.mqttbee.mqtt.MqttClientData;
import org.mqttbee.mqtt.ioc.ClientComponent;
import org.reactivestreams.Subscriber;

/**
 * @author Silvio Giebl
 */
public class MqttGlobalIncomingPublishFlowable extends Flowable<Mqtt5Publish> {

    private final @NotNull MqttGlobalPublishFilter filter;
    private final @NotNull MqttClientData clientData;

    public MqttGlobalIncomingPublishFlowable(
            final @NotNull MqttGlobalPublishFilter filter, final @NotNull MqttClientData clientData) {

        this.filter = filter;
        this.clientData = clientData;
    }

    @Override
    protected void subscribeActual(final @NotNull Subscriber<? super Mqtt5Publish> subscriber) {
        final ClientComponent clientComponent = clientData.getClientComponent();
        final MqttIncomingQosHandler incomingQosHandler = clientComponent.incomingQosHandler();
        final MqttIncomingPublishFlows incomingPublishFlows = incomingQosHandler.getIncomingPublishFlows();

        final EventLoop eventLoop = clientData.acquireEventLoop();

        final MqttGlobalIncomingPublishFlow flow =
                new MqttGlobalIncomingPublishFlow(subscriber, incomingQosHandler, filter, eventLoop);
        subscriber.onSubscribe(flow);
        eventLoop.execute(() -> incomingPublishFlows.subscribeGlobal(flow));
    }
}
