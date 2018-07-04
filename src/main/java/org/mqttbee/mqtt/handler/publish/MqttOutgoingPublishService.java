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

package org.mqttbee.mqtt.handler.publish;

import io.netty.channel.EventLoop;
import io.reactivex.FlowableSubscriber;
import javax.inject.Inject;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt.MqttClientConnectionData;
import org.mqttbee.mqtt.MqttClientData;
import org.mqttbee.mqtt.MqttServerConnectionData;
import org.mqttbee.mqtt.ioc.ChannelScope;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Silvio Giebl */
@ChannelScope
public class MqttOutgoingPublishService implements FlowableSubscriber<MqttPublishWithFlow> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MqttOutgoingPublishService.class);

    private static final int MAX_CONCURRENT_PUBLISH_FLOWABLES = 64;

    private final MqttOutgoingQoSHandler outgoingQoSHandler;
    private final EventLoop nettyEventLoop;

    private Subscription subscription;

    private final int receiveMaximum;

    @Inject
    MqttOutgoingPublishService(
            final MqttOutgoingQoSHandler outgoingQoSHandler,
            final MqttPublishFlowables publishFlowables,
            final MqttClientData clientData) {

        final MqttServerConnectionData serverConnectionData =
                clientData.getRawServerConnectionData();
        assert serverConnectionData != null;
        final MqttClientConnectionData clientConnectionData =
                clientData.getRawClientConnectionData();
        assert clientConnectionData != null;

        this.outgoingQoSHandler = outgoingQoSHandler;
        nettyEventLoop = clientConnectionData.getChannel().eventLoop();

        receiveMaximum =
                MqttOutgoingQoSHandler.getPubReceiveMaximum(
                        serverConnectionData.getReceiveMaximum());

        publishFlowables.flatMap(f -> f, true, MAX_CONCURRENT_PUBLISH_FLOWABLES).subscribe(this);
    }

    @Override
    public void onSubscribe(final Subscription s) {
        subscription = s;
        s.request(receiveMaximum);
    }

    @Override
    public void onNext(final MqttPublishWithFlow publishWithFlow) {
        outgoingQoSHandler.publish(publishWithFlow);
    }

    @Override
    public void onComplete() {
        LOGGER.error("MqttPublishFlowables is global and should never complete.");
    }

    @Override
    public void onError(final Throwable t) {
        LOGGER.error("MqttPublishFlowables is global and should never error.");
    }

    void request(final long amount) {
        subscription.request(amount);
    }

    @NotNull
    EventLoop getNettyEventLoop() {
        return nettyEventLoop;
    }
}
