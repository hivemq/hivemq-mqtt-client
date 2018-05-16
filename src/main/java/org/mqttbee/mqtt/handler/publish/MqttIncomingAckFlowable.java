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
import org.mqttbee.api.mqtt.mqtt5.message.publish.Mqtt5PublishResult;
import org.mqttbee.mqtt.MqttClientConnectionData;
import org.mqttbee.mqtt.MqttClientData;
import org.mqttbee.mqtt.MqttServerConnectionData;
import org.mqttbee.mqtt.ioc.ChannelComponent;
import org.mqttbee.mqtt.message.publish.MqttPublish;
import org.reactivestreams.Subscriber;

/**
 * @author Silvio Giebl
 */
public class MqttIncomingAckFlowable extends Flowable<Mqtt5PublishResult> {

    private final Flowable<MqttPublish> publishFlowable;
    private final MqttClientData clientData;

    public MqttIncomingAckFlowable(
            @NotNull final Flowable<MqttPublish> publishFlowable, @NotNull final MqttClientData clientData) {

        this.publishFlowable = publishFlowable;
        this.clientData = clientData;
    }

    @Override
    protected void subscribeActual(final Subscriber<? super Mqtt5PublishResult> s) {
        final MqttClientConnectionData clientConnectionData = clientData.getRawClientConnectionData(); // TODO temp
        final MqttServerConnectionData serverConnectionData = clientData.getRawServerConnectionData(); // TODO temp
        if ((clientConnectionData == null) || (serverConnectionData == null)) {
            EmptySubscription.error(new NotConnectedException(), s);
        } else {
            final ChannelComponent channelComponent = ChannelComponent.get(clientConnectionData.getChannel());
            final MqttOutgoingPublishService outgoingPublishService = channelComponent.outgoingPublishService();
            final MqttPublishFlowables publishFlowables = channelComponent.publishFlowables();

            final MqttIncomingAckFlow incomingAckFlow = new MqttIncomingAckFlow(s, outgoingPublishService);
            s.onSubscribe(incomingAckFlow);
            publishFlowables.add(publishFlowable.map(publish -> new MqttPublishWithFlow(publish, incomingAckFlow)));
        }
    }

}
