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

import io.reactivex.Flowable;
import io.reactivex.internal.subscriptions.EmptySubscription;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.exceptions.NotConnectedException;
import org.mqttbee.api.mqtt.mqtt5.message.publish.Mqtt5Publish;
import org.mqttbee.mqtt.MqttClientConnectionData;
import org.mqttbee.mqtt.MqttClientData;
import org.mqttbee.mqtt.ioc.ChannelComponent;
import org.reactivestreams.Subscriber;

/**
 * @author Silvio Giebl
 */
public class MqttGlobalIncomingPublishFlowable extends Flowable<Mqtt5Publish> {

    private final int type;
    private final MqttClientData clientData;

    public MqttGlobalIncomingPublishFlowable(final int type, @NotNull final MqttClientData clientData) {
        this.type = type;
        this.clientData = clientData;
    }

    @Override
    protected void subscribeActual(final Subscriber<? super Mqtt5Publish> s) {
        final MqttClientConnectionData clientConnectionData = clientData.getRawClientConnectionData(); // TODO temp
        if (clientConnectionData == null) {
            EmptySubscription.error(new NotConnectedException(), s);
        } else {
            final ChannelComponent channelComponent = ChannelComponent.get(clientConnectionData.getChannel());
            final MqttIncomingPublishService incomingPublishService = channelComponent.incomingPublishService();

            final MqttGlobalIncomingPublishFlow flow =
                    new MqttGlobalIncomingPublishFlow(s, incomingPublishService, type);
            incomingPublishService.getNettyEventLoop()
                    .execute(() -> incomingPublishService.getIncomingPublishFlows().subscribeGlobal(flow));
            s.onSubscribe(flow);
        }
    }

}
